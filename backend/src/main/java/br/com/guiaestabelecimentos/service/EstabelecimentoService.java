package br.com.guiaestabelecimentos.service;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import br.com.guiaestabelecimentos.repository.EstabelecimentoRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;


@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository repository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public EstabelecimentoService(EstabelecimentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Estabelecimento cadastrar(Estabelecimento dados) {
        Estabelecimento estabelecimento = new Estabelecimento();
        preencher(estabelecimento, dados);
        return repository.save(estabelecimento);
    }

    @Transactional(readOnly = true)
    public Estabelecimento consultar(Long id) {
        return buscar(id);
    }

    @Transactional(readOnly = true)
    public List<Estabelecimento> buscarProximos(double latitude, double longitude, double distancia) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude inválida");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Longitude inválida");
        }
        if (!Double.isFinite(distancia) || distancia <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Distância deve ser maior que zero, em metros");
        }
        return repository.buscarProximos(latitude, longitude, distancia);
    }

    @Transactional
    public Estabelecimento atualizar(Long id, Estabelecimento dados) {
        Estabelecimento estabelecimento = buscar(id);
        preencher(estabelecimento, dados);
        return repository.save(estabelecimento);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    private Estabelecimento buscar(Long id) {
        Optional<Estabelecimento> resultado = repository.findById(id);
        if (resultado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado");
        }
        return resultado.get();
    }

    private void preencher(Estabelecimento estabelecimento, Estabelecimento dados) {
        validar(dados);

        estabelecimento.setNome(dados.getNome());
        estabelecimento.setDescricao(dados.getDescricao());
        estabelecimento.setCategoria(dados.getCategoria());
        estabelecimento.setTelefone(dados.getTelefone());
        estabelecimento.setEndereco(dados.getEndereco());

        Coordinate coordenadas = new Coordinate(dados.getLongitude(), dados.getLatitude());
        estabelecimento.setLocalizacao(geometryFactory.createPoint(coordenadas));
    }

    private void validar(Estabelecimento dados) {
        String nome = dados.getNome();
        String categoria = dados.getCategoria();
        Double latitude = dados.getLatitude();
        Double longitude = dados.getLongitude();

        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria é obrigatória");
        }
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe latitude e longitude");
        }
        if (!Double.isFinite(latitude) || latitude < -90 || latitude >= 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude inválida");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Longitude inválida");
        }
    }
}

