const buscaEndereco = document.getElementById('busca-endereco');
const cadastro = document.getElementById('cadastro');
const mensagemCadastro = document.getElementById('mensagem-cadastro');

if (typeof L === 'undefined') {
    mensagemCadastro.textContent = 'Não foi possível carregar o mapa. Recarregue a página.';
    buscaEndereco.querySelector('button').disabled = true;
    cadastro.querySelector('button').disabled = true;
} else {
    iniciarCadastro();
}

function iniciarCadastro() {
    const mapa = L.map('mapa').setView([-6.89, -38.56], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(mapa);

    const resultados = document.getElementById('resultados-endereco');
    const mensagemBusca = document.getElementById('mensagem-busca');
    const posicao = document.getElementById('posicao');
    const id = new URLSearchParams(window.location.search).get('id');
    const api = '/api/estabelecimentos';
    const recurso = id === null ? api : api + '/' + encodeURIComponent(id);
    const salvar = document.getElementById('salvar-estabelecimento');
    const excluir = document.getElementById('excluir-estabelecimento');
    const cache = new Map();
    let ultimaBusca = 0;
    let enderecos = [];
    let marcador;

    async function carregarEstabelecimento() {
        salvar.disabled = true;
        document.title = 'Editar estabelecimento';
        document.getElementById('titulo-pagina').textContent = 'Editar estabelecimento';
        document.getElementById('titulo-formulario').textContent = 'Dados do estabelecimento';
        salvar.textContent = 'Salvar alterações';
        if (!/^[1-9][0-9]*$/.test(id)) {
            mensagemCadastro.textContent = 'ID do estabelecimento inválido.';
            return;
        }

        mensagemCadastro.textContent = 'Carregando estabelecimento...';
        try {
            const resposta = await fetch(recurso);
            if (!resposta.ok) {
                mensagemCadastro.textContent = resposta.status === 404
                    ? 'Estabelecimento não encontrado.'
                    : 'Não foi possível carregar o estabelecimento (HTTP ' + resposta.status + ').';
                return;
            }
            const dados = await resposta.json();
            const categoria = cadastro.elements.categoria;
            categoria.value = dados.categoria;
            if (!categoria.value && dados.categoria) {
                categoria.add(new Option(dados.categoria, dados.categoria, false, true));
            }
            for (const campo of ['nome', 'descricao', 'telefone', 'endereco']) {
                cadastro.elements[campo].value = dados[campo] ?? '';
            }
            const ponto = L.latLng(dados.latitude, dados.longitude);
            marcarPonto(ponto);
            mapa.setView(ponto, 17);
            mensagemCadastro.textContent = '';
            salvar.disabled = false;
            excluir.hidden = false;
        } catch (erro) {
            mensagemCadastro.textContent = 'Não foi possível carregar o estabelecimento. Recarregue a página.';
        }
    }

    function marcarPonto(ponto) {
        if (marcador) {
            marcador.setLatLng(ponto);
        } else {
            marcador = L.marker(ponto).addTo(mapa);
        }
        posicao.textContent = 'Ponto selecionado: ' + ponto.lat.toFixed(6) + ', ' + ponto.lng.toFixed(6);
    }

    mapa.on('click', function (evento) {
        resultados.value = '';
        marcarPonto(evento.latlng.wrap());
    });

    buscaEndereco.addEventListener('submit', async function (evento) {
        evento.preventDefault();
        const consulta = document.getElementById('consulta-endereco').value.trim();
        if (!consulta) {
            mensagemBusca.textContent = 'Digite um endereço.';
            return;
        }

        const botao = buscaEndereco.querySelector('button');
        if (botao.disabled) {
            return;
        }
        const chave = consulta.toLowerCase();
        if (!cache.has(chave) && Date.now() - ultimaBusca < 1000) {
            mensagemBusca.textContent = 'Aguarde um segundo antes de buscar novamente.';
            return;
        }

        botao.disabled = true;
        resultados.replaceChildren(new Option('Selecione um endereço', ''));
        resultados.disabled = true;
        mensagemBusca.textContent = 'Buscando endereços...';
        try {
            if (!cache.has(chave)) {
                const url = new URL(buscaEndereco.dataset.geocodificador);
                url.search = new URLSearchParams({ q: consulta, format: 'jsonv2', limit: '5' });
                ultimaBusca = Date.now();
                const resposta = await fetch(url);
                if (!resposta.ok) {
                    throw new Error('Falha na busca de endereço');
                }
                cache.set(chave, await resposta.json());
            }
            enderecos = cache.get(chave);
            for (const [indice, endereco] of enderecos.entries()) {
                resultados.add(new Option(endereco.display_name, String(indice)));
            }
            resultados.disabled = enderecos.length === 0;
            mensagemBusca.textContent = enderecos.length ? 'Selecione um dos endereços.' : 'Nenhum endereço encontrado.';
        } catch (erro) {
            mensagemBusca.textContent = 'Não foi possível buscar o endereço. Você pode escolher o ponto no mapa.';
        } finally {
            botao.disabled = false;
        }
    });

    resultados.addEventListener('change', function () {
        if (resultados.value === '') {
            return;
        }
        const endereco = enderecos[Number(resultados.value)];
        const ponto = L.latLng(Number(endereco.lat), Number(endereco.lon));
        marcarPonto(ponto);
        mapa.setView(ponto, 17);
        cadastro.elements.endereco.value = endereco.display_name;
    });

    cadastro.addEventListener('submit', async function (evento) {
        evento.preventDefault();
        if (salvar.disabled) {
            return;
        }
        if (!marcador) {
            mensagemCadastro.textContent = 'Selecione um ponto no mapa antes de salvar.';
            return;
        }

        const dados = Object.fromEntries(new FormData(cadastro));
        dados.nome = dados.nome.trim();
        if (!dados.nome) {
            mensagemCadastro.textContent = 'Informe o nome do estabelecimento.';
            return;
        }
        const ponto = marcador.getLatLng();
        dados.latitude = ponto.lat;
        dados.longitude = ponto.lng;

        salvar.disabled = true;
        excluir.disabled = true;
        mensagemCadastro.textContent = 'Salvando estabelecimento...';
        try {
            const resposta = await fetch(recurso, {
                method: id === null ? 'POST' : 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dados)
            });
            if (!resposta.ok) {
                mensagemCadastro.textContent = 'Não foi possível salvar (HTTP ' + resposta.status + ').';
                return;
            }
            if (id !== null) {
                mensagemCadastro.textContent = 'Alterações salvas.';
                return;
            }
            const criado = await resposta.json();
            mensagemCadastro.textContent = 'Estabelecimento cadastrado. ID: ' + criado.id;
            cadastro.reset();
            mapa.removeLayer(marcador);
            marcador = undefined;
            resultados.value = '';
            posicao.textContent = 'Nenhum ponto selecionado.';
        } catch (erro) {
            mensagemCadastro.textContent = 'Não foi possível confirmar o envio. Confira antes de tentar novamente.';
        } finally {
            salvar.disabled = false;
            excluir.disabled = false;
        }
    });

    excluir.addEventListener('click', async function () {
        if (excluir.hidden || excluir.disabled || !window.confirm('Excluir este estabelecimento?')) {
            return;
        }
        salvar.disabled = true;
        excluir.disabled = true;
        mensagemCadastro.textContent = 'Excluindo estabelecimento...';
        try {
            const resposta = await fetch(recurso, { method: 'DELETE' });
            if (resposta.ok) {
                window.location.assign('index.html');
                return;
            }
            mensagemCadastro.textContent = 'Não foi possível excluir (HTTP ' + resposta.status + ').';
        } catch (erro) {
            mensagemCadastro.textContent = 'Não foi possível confirmar a exclusão. Confira antes de tentar novamente.';
        }
        salvar.disabled = false;
        excluir.disabled = false;
    });

    if (id !== null) {
        carregarEstabelecimento();
    }
}
