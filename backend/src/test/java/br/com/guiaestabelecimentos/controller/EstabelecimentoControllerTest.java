package br.com.guiaestabelecimentos.controller;

import br.com.guiaestabelecimentos.model.Estabelecimento;
import br.com.guiaestabelecimentos.service.EstabelecimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EstabelecimentoControllerTest {

    private EstabelecimentoService service;
    private MockMvc mvc;
    private final Long id = 1L;
    private final String json = """
            {"nome":"Mercado","categoria":"Mercado","latitude":-6.89,"longitude":-38.56}
            """;

    @BeforeEach
    void preparar() {
        service = mock(EstabelecimentoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new EstabelecimentoController(service)).build();
    }

    private Estabelecimento resposta() {
        Estabelecimento estabelecimento = new Estabelecimento();
        org.springframework.test.util.ReflectionTestUtils.setField(estabelecimento, "id", id);
        estabelecimento.setNome("Mercado");
        estabelecimento.setCategoria("Mercado");
        estabelecimento.setLocalizacao(new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new org.locationtech.jts.geom.Coordinate(-38.56, -6.89)));
        return estabelecimento;
    }

    @Test
    void buscarProximosRetornaListaEUsaRotaCorreta() throws Exception {
        when(service.buscarProximos(-6.89, -38.56, 2000)).thenReturn(List.of(resposta()));
        mvc.perform(get("/api/estabelecimentos/proximos")
                        .param("lat", "-6.89").param("lng", "-38.56").param("distancia", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Mercado"));
        verify(service).buscarProximos(-6.89, -38.56, 2000);
    }

    @Test
    void buscarProximosSemResultadosRetornaListaVazia() throws Exception {
        when(service.buscarProximos(0, 0, 100)).thenReturn(List.of());
        mvc.perform(get("/api/estabelecimentos/proximos")
                        .param("lat", "0").param("lng", "0").param("distancia", "100"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void buscarProximosExigeTodosOsParametros() throws Exception {
        mvc.perform(get("/api/estabelecimentos/proximos")
                        .param("lat", "0").param("lng", "0"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void cadastrarRetorna201ComLocalDoNovoRecurso() throws Exception {
        when(service.cadastrar(any())).thenReturn(resposta());
        mvc.perform(post("/api/estabelecimentos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/estabelecimentos/" + id))
                .andExpect(jsonPath("$.id").value(id.intValue()))
                .andExpect(jsonPath("$.latitude").value(-6.89))
                .andExpect(jsonPath("$.longitude").value(-38.56));
        verify(service).cadastrar(any(Estabelecimento.class));
    }

    @Test
    void consultarRetorna200() throws Exception {
        when(service.consultar(id)).thenReturn(resposta());
        mvc.perform(get("/api/estabelecimentos/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Mercado"));
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(id), any())).thenReturn(resposta());
        mvc.perform(put("/api/estabelecimentos/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.intValue()));
    }

    @Test
    void excluirRetorna204SemCorpo() throws Exception {
        mvc.perform(delete("/api/estabelecimentos/" + id))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).excluir(id);
    }

    @Test
    void registroInexistenteRetorna404() throws Exception {
        when(service.consultar(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get("/api/estabelecimentos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void jsonNaoExpoePontoInternoNemAceitaIdDoCliente() throws Exception {
        when(service.cadastrar(any())).thenReturn(resposta());
        String entrada = """
                {"id":%s,"nome":"Mercado","categoria":"Mercado",
                 "latitude":-6.89,"longitude":-38.56,"localizacao":null}
                """.formatted(1L);

        mvc.perform(post("/api/estabelecimentos").contentType(MediaType.APPLICATION_JSON).content(entrada))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.localizacao").doesNotExist())
                .andExpect(jsonPath("$.latitude").value(-6.89))
                .andExpect(jsonPath("$.longitude").value(-38.56));

        ArgumentCaptor<Estabelecimento> captor = ArgumentCaptor.forClass(Estabelecimento.class);
        verify(service).cadastrar(captor.capture());
        Estabelecimento recebido = captor.getValue();
        assertNull(recebido.getId());
        assertNull(recebido.getLocalizacao());
        assertEquals(-6.89, recebido.getLatitude());
        assertEquals(-38.56, recebido.getLongitude());
    }
}



