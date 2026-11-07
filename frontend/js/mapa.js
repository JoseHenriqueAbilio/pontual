const pesquisa = document.getElementById('pesquisa');
const categoria = document.getElementById('categoria');
const botaoBuscar = document.getElementById('buscar-area');
const mensagem = document.getElementById('mensagem');
const lista = document.getElementById('estabelecimentos');

if (typeof L === 'undefined') {
    mensagem.textContent = 'Não foi possível carregar o Leaflet. Confira a conexão e recarregue a página.';
    botaoBuscar.disabled = true;
} else {
    iniciarMapa();
}

function iniciarMapa() {
    const mapa = L.map('mapa', {
        minZoom: 3,
        maxBounds: [[-85, -180], [85, 180]]
    }).setView([-6.89, -38.56], 13);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        noWrap: true,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(mapa);

    const marcadores = L.layerGroup().addTo(mapa);
    let estabelecimentos = [];

    function preencherCategorias() {
        const selecionada = categoria.value;
        const categorias = new Set();
        for (const estabelecimento of estabelecimentos) {
            categorias.add(estabelecimento.categoria);
        }

        categoria.replaceChildren(new Option('Todas', ''));
        for (const nome of Array.from(categorias).sort()) {
            categoria.add(new Option(nome, nome));
        }
        if (categorias.has(selecionada)) {
            categoria.value = selecionada;
        }
    }

    function mostrarEstabelecimentos() {
        const termo = pesquisa.value.trim().toLocaleLowerCase('pt-BR');
        marcadores.clearLayers();
        lista.replaceChildren();

        for (const estabelecimento of estabelecimentos) {
            const nome = estabelecimento.nome.toLocaleLowerCase('pt-BR');
            if (!nome.includes(termo) || (categoria.value && categoria.value !== estabelecimento.categoria)) {
                continue;
            }

            const conteudo = document.createElement('div');
            const titulo = document.createElement('strong');
            titulo.textContent = estabelecimento.nome;
            conteudo.append(titulo);
            for (const informacao of [estabelecimento.categoria, estabelecimento.descricao,
                estabelecimento.endereco, estabelecimento.telefone]) {
                if (informacao) {
                    conteudo.append(document.createElement('br'), document.createTextNode(informacao));
                }
            }

            const marcador = L.marker([estabelecimento.latitude, estabelecimento.longitude], {
                alt: estabelecimento.nome
            }).bindPopup(conteudo).addTo(marcadores);

            const item = document.createElement('li');
            const botao = document.createElement('button');
            botao.type = 'button';
            botao.textContent = estabelecimento.nome;
            botao.addEventListener('click', function () {
                mapa.panTo(marcador.getLatLng());
                marcador.openPopup();
            });
            item.append(botao);
            lista.append(item);
        }

        mensagem.textContent = lista.children.length
            ? 'Estabelecimentos encontrados: ' + lista.children.length
            : 'Nenhum estabelecimento encontrado.';
    }

    async function buscarArea() {
        const limites = mapa.getBounds();
        const parametros = new URLSearchParams({
            sul: Math.max(-90, limites.getSouth()),
            norte: Math.min(90, limites.getNorth()),
            oeste: Math.max(-180, limites.getWest()),
            leste: Math.min(180, limites.getEast())
        });

        botaoBuscar.disabled = true;
        mensagem.textContent = 'Carregando estabelecimentos...';
        try {
            const resposta = await fetch('/api/estabelecimentos/area?' + parametros);
            if (!resposta.ok) {
                throw new Error('HTTP ' + resposta.status);
            }
            estabelecimentos = await resposta.json();
            preencherCategorias();
            mostrarEstabelecimentos();
        } catch (erro) {
            estabelecimentos = [];
            preencherCategorias();
            mostrarEstabelecimentos();
            mensagem.textContent = 'Não foi possível carregar os estabelecimentos. Tente buscar nesta área novamente.';
        } finally {
            botaoBuscar.disabled = false;
        }
    }

    function mostrarLocalizacao(posicao) {
        const coordenadas = [posicao.coords.latitude, posicao.coords.longitude];
        mapa.setView(coordenadas, 13);
        L.marker(coordenadas, { alt: 'Sua localização' })
            .bindPopup('Sua localização')
            .addTo(mapa)
            .openPopup();
        buscarArea();
    }

    pesquisa.addEventListener('input', mostrarEstabelecimentos);
    categoria.addEventListener('change', mostrarEstabelecimentos);
    botaoBuscar.addEventListener('click', buscarArea);
    if (navigator.geolocation) {
        botaoBuscar.disabled = true;
        mensagem.textContent = 'Aguardando a localização do navegador...';
        navigator.geolocation.getCurrentPosition(mostrarLocalizacao, buscarArea, { timeout: 10000 });
    } else {
        buscarArea();
    }
}
