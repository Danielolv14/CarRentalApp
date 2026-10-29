package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.controller.ApiDeTeste.Conta;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.pucminas.aluguelcarros.controller.ApiDeTeste.DEVOLUCAO;
import static br.pucminas.aluguelcarros.controller.ApiDeTeste.RETIRADA;
import static br.pucminas.aluguelcarros.controller.ApiDeTeste.corpo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API do lado do agente: fila, análise, parecer, crédito do banco e modificação (UC05, UC06, UC09, UC10). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvaliacaoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private AutomovelRepository automoveis;

    private ApiDeTeste api;
    private Automovel polo;

    @BeforeEach
    void preparar() {
        api = new ApiDeTeste(mvc);
        polo = automoveis.save(new Automovel(ApiDeTeste.matricula(70_000_000_000L), 2024, "Volkswagen", "Polo",
                ApiDeTeste.placa("AVL"), new BigDecimal("189.00")));
    }

    // ---------------------------------------------------------------- fila

    @Test
    void empresaVeTodosOsPedidosEBancoSoOsDeCreditoParaEle() throws Exception {
        Conta cliente = api.cliente("fila.cliente");
        Conta empresa = api.empresa("fila.locadora");
        Conta horizonte = api.banco("fila.horizonte", "Banco Horizonte S.A.");
        Conta outroBanco = api.banco("fila.outro", "Outro Banco S.A.");
        long semCredito = api.pedido(cliente, polo.getId(), null, null);
        long comCredito = api.pedido(cliente, polo.getId(), horizonte.id(), "1323.00");

        String filaEmpresa = corpo(api.obter("/agente/pedidos", empresa.token()).andExpect(status().isOk()));
        List<Number> ids = JsonPath.read(filaEmpresa, "$[*].pedido.id");
        assertThat(ids.stream().map(Number::longValue).toList()).containsSubsequence(semCredito, comCredito);
        assertThat(JsonPath.<List<Boolean>>read(filaEmpresa, "$[?(@.pedido.id == %d)].podeAvaliar".formatted(semCredito))).containsExactly(true);
        assertThat(JsonPath.<List<Boolean>>read(filaEmpresa, "$[?(@.pedido.id == %d)].podeAvaliar".formatted(comCredito))).containsExactly(false);
        assertThat(JsonPath.<List<String>>read(filaEmpresa, "$[?(@.pedido.id == %d)].cliente.nome".formatted(semCredito)))
                .containsExactly("Cliente fila.cliente");

        api.obter("/agente/pedidos", horizonte.token())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pedido.id").value(comCredito))
                .andExpect(jsonPath("$[0].podeAvaliar").value(true))
                .andExpect(jsonPath("$[0].cliente.rendaTotal").value(1500.50));
        api.obter("/agente/pedidos", outroBanco.token()).andExpect(jsonPath("$.length()").value(0));
        api.obter("/agente/pedidos/" + semCredito, horizonte.token())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pedido não encontrado."));
    }

    @Test
    void filtraAFilaPorStatus() throws Exception {
        Conta cliente = api.cliente("filtro.cliente");
        Conta banco = api.banco("filtro.banco", "Banco Filtro S.A.");
        long cancelado = api.pedido(cliente, polo.getId(), banco.id(), "500.00");
        long enviado = api.pedido(cliente, polo.getId(), banco.id(), "500.00");
        api.postar("/pedidos/%d/cancelar".formatted(cancelado), cliente.token(), null).andExpect(status().isOk());

        api.obter("/agente/pedidos?status=ENVIADO&status=EM_ANALISE", banco.token())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pedido.id").value(enviado));
        api.obter("/agente/pedidos", banco.token()).andExpect(jsonPath("$.length()").value(2));
    }

    // ---------------------------------------------------------------- análise e parecer

    @Test
    void abrirParaAnaliseMudaOStatusEOClienteVe() throws Exception {
        Conta cliente = api.cliente("analise.cliente");
        Conta empresa = api.empresa("analise.locadora");
        long id = api.pedido(cliente, polo.getId(), null, null);

        api.postar("/agente/pedidos/%d/analise".formatted(id), empresa.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.status").value("EM_ANALISE"));
        api.obter("/pedidos/" + id, cliente.token()).andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void parecerDaEmpresaAprovaOuReprovaComJustificativa() throws Exception {
        Conta cliente = api.cliente("parecer.cliente");
        Conta empresa = api.empresa("parecer.locadora");
        long aprovado = api.pedido(cliente, polo.getId(), null, null);
        long reprovado = api.pedido(cliente, polo.getId(), null, null);

        api.postar("/agente/pedidos/%d/parecer".formatted(aprovado), empresa.token(), "{\"resultado\":\"POSITIVO\",\"justificativa\":\" \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.justificativa").value("Escreva a justificativa."));
        api.postar("/agente/pedidos/%d/parecer".formatted(aprovado), empresa.token(), parecer("POSITIVO", "Renda compatível."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.status").value("APROVADO"))
                .andExpect(jsonPath("$.podeAvaliar").value(false))
                .andExpect(jsonPath("$.podeExecutarContrato").value(true));
        api.postar("/agente/pedidos/%d/parecer".formatted(aprovado), empresa.token(), parecer("NEGATIVO", "Mudei de ideia."))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O pedido APROVADO não aceita novo parecer."));
        api.postar("/agente/pedidos/%d/parecer".formatted(reprovado), empresa.token(), parecer("NEGATIVO", "Renda insuficiente."))
                .andExpect(jsonPath("$.pedido.status").value("REPROVADO"))
                .andExpect(jsonPath("$.podeExecutarContrato").value(false));

        api.obter("/pedidos/" + aprovado, cliente.token())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.pareceres[0].resultado").value("POSITIVO"))
                .andExpect(jsonPath("$.pareceres[0].justificativa").value("Renda compatível."))
                .andExpect(jsonPath("$.pareceres[0].agente").value("Locadora parecer.locadora"));
    }

    @Test
    void bancoConcedeOCreditoNoParecerPositivo() throws Exception {
        Conta cliente = api.cliente("credito.cliente");
        Conta empresa = api.empresa("credito.locadora");
        Conta banco = api.banco("credito.banco", "Banco Credito S.A.");
        long id = api.pedido(cliente, polo.getId(), banco.id(), "1323.00");
        String url = "/agente/pedidos/%d/parecer".formatted(id);

        api.postar(url, empresa.token(), parecer("POSITIVO", "Ok."))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O pedido pediu crédito ao Banco Credito S.A.: o parecer é do banco."));
        api.postar(url, banco.token(), parecer("POSITIVO", "Crédito aprovado."))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Informe as condições do crédito: valor, parcelas e taxa ao mês."));
        api.postar(url, banco.token(), """
                        {"resultado":"POSITIVO","justificativa":"Crédito aprovado.","credito":{"valor":1323.00,"parcelas":0,"taxaMensal":1.9}}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['credito.parcelas']").value("No mínimo 1 parcela."));
        api.obter("/agente/pedidos/" + id, banco.token()).andExpect(jsonPath("$.pedido.status").value("ENVIADO"));

        api.postar(url, banco.token(), """
                        {"resultado":"POSITIVO","justificativa":"Crédito aprovado.","credito":{"valor":1323.00,"parcelas":3,"taxaMensal":1.9}}
                        """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.status").value("APROVADO"))
                .andExpect(jsonPath("$.pedido.credito.concedido.parcelas").value(3))
                .andExpect(jsonPath("$.pedido.credito.concedido.valorParcela").value(457.86));
        api.obter("/pedidos/" + id, cliente.token()).andExpect(jsonPath("$.status").value("APROVADO"));
    }

    // ---------------------------------------------------------------- modificar

    @Test
    void agenteModificaOPedidoQueAvaliaEOValorERecalculado() throws Exception {
        Conta cliente = api.cliente("modifica.cliente");
        Conta empresa = api.empresa("modifica.locadora");
        Conta banco = api.banco("modifica.banco", "Banco Modifica S.A.");
        Automovel corolla = automoveis.save(new Automovel(ApiDeTeste.matricula(70_000_000_000L), 2024, "Toyota", "Corolla",
                ApiDeTeste.placa("AVL"), new BigDecimal("299.00")));
        long id = api.pedido(cliente, polo.getId(), null, null);
        String url = "/agente/pedidos/" + id;

        api.alterar(url, empresa.token(), alteracao(corolla.getId(), RETIRADA, RETIRADA.plusDays(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedido.automovel.modelo").value("Corolla"))
                .andExpect(jsonPath("$.pedido.diarias").value(3))
                .andExpect(jsonPath("$.pedido.valorEstimado").value(897.00));
        api.alterar(url, empresa.token(), alteracao(corolla.getId(), LocalDate.now().minusDays(1), DEVOLUCAO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("A data de retirada não pode estar no passado."));
        api.alterar(url, banco.token(), alteracao(corolla.getId(), RETIRADA, DEVOLUCAO)).andExpect(status().isNotFound());

        api.postar(url + "/parecer", empresa.token(), parecer("POSITIVO", "Ok.")).andExpect(status().isOk());
        api.alterar(url, empresa.token(), alteracao(polo.getId(), RETIRADA, DEVOLUCAO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Pedidos com status APROVADO não podem ser alterados."));
    }

    @Test
    void clienteNaoUsaAsRotasDoAgente() throws Exception {
        Conta cliente = api.cliente("intruso.cliente");
        long id = api.pedido(cliente, polo.getId(), null, null);

        api.obter("/agente/pedidos", cliente.token()).andExpect(status().isForbidden());
        api.postar("/agente/pedidos/%d/parecer".formatted(id), cliente.token(), parecer("POSITIVO", "Eu mesmo aprovo."))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/agente/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- apoio

    private static String parecer(String resultado, String justificativa) {
        return "{\"resultado\":\"%s\",\"justificativa\":\"%s\"}".formatted(resultado, justificativa);
    }

    private static String alteracao(Long automovelId, LocalDate retirada, LocalDate devolucao) {
        return "{\"automovelId\":%d,\"dataRetirada\":\"%s\",\"dataDevolucao\":\"%s\"}".formatted(automovelId, retirada, devolucao);
    }
}
