package br.com.guiaestabelecimentos.service;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import br.com.guiaestabelecimentos.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstabelecimentoServiceTest {

    private EstabelecimentoRepository repository;
    private EstabelecimentoService service;

    @BeforeEach
    void preparar() {
        repository = mock(EstabelecimentoRepository.class);
        service = new EstabelecimentoService(repository);
    }

    private Estabelecimento dados(String nome, Double latitude, Double longitude) {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setNome(nome);
        estabelecimento.setDescricao("Descrição");
        estabelecimento.setCategoria("Mercado");
        estabelecimento.setLatitude(latitude);
        estabelecimento.setLongitude(longitude);
        return estabelecimento;
    }

    @Test
    void cadastrarSalvaPontoComLongitudeNoXELatitudeNoY() {
        Estabelecimento salvo = dados("Mercado", -6.89, -38.56);
        when(repository.save(any())).thenReturn(salvo);

        Estabelecimento resposta = service.cadastrar(dados("Mercado", -6.89, -38.56));

        ArgumentCaptor<Estabelecimento> captor = ArgumentCaptor.forClass(Estabelecimento.class);
        verify(repository).save(captor.capture());
        Estabelecimento enviado = captor.getValue();
        assertEquals(4326, enviado.getLocalizacao().getSRID());
        assertEquals(-38.56, enviado.getLocalizacao().getX());
        assertEquals(-6.89, enviado.getLocalizacao().getY());
        assertEquals(-6.89, resposta.getLatitude());
        assertEquals(-38.56, resposta.getLongitude());
    }

    @Test
    void consultarRetornaDadosDoEstabelecimento() {
        Long id = 1L;
        Estabelecimento existente = new Estabelecimento();
        existente.setNome("Mercado");
        existente.setLocalizacao(new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new org.locationtech.jts.geom.Coordinate(-38.56, -6.89)));
        when(repository.findById(id)).thenReturn(Optional.of(existente));

        assertEquals("Mercado", service.consultar(id).getNome());
    }

    @Test
    void atualizarAlteraORegistroEncontrado() {
        Long id = 1L;
        Estabelecimento existente = new Estabelecimento();
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Estabelecimento resposta = service.atualizar(id, dados("Novo nome", -7.0, -39.0));

        assertEquals("Novo nome", resposta.getNome());
        assertEquals(-39.0, existente.getLocalizacao().getX());
        verify(repository).save(existente);
    }

    @Test
    void excluirRemoveORegistroEncontrado() {
        Long id = 1L;
        Estabelecimento existente = new Estabelecimento();
        when(repository.findById(id)).thenReturn(Optional.of(existente));

        service.excluir(id);

        verify(repository).delete(existente);
    }

    @Test
    void operacoesComIdInexistenteRetornam404() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        try {
            service.consultar(id);
            fail("Deveria retornar 404 ao consultar");
        } catch (ResponseStatusException erro) {
            assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        }
        try {
            service.atualizar(id, dados("Mercado", 0.0, 0.0));
            fail("Deveria retornar 404 ao atualizar");
        } catch (ResponseStatusException erro) {
            assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        }
        try {
            service.excluir(id);
            fail("Deveria retornar 404 ao excluir");
        } catch (ResponseStatusException erro) {
            assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        }
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(Estabelecimento.class));
    }

    @Test
    void rejeitarCadastroSemNomeOuCoordenadasValidas() {
        for (Estabelecimento invalido : new Estabelecimento[] {
                dados(" ", 0.0, 0.0), dados("Mercado", null, 0.0),
                dados("Mercado", 91.0, 0.0), dados("Mercado", 0.0, -181.0),
                dados("Mercado", Double.NaN, 0.0) }) {
            try {
                service.cadastrar(invalido);
                fail("Deveria rejeitar os dados inválidos");
            } catch (ResponseStatusException erro) {
                assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
            }
        }
        verify(repository, never()).save(any());
    }
}



