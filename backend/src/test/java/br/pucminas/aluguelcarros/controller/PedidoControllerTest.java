package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cnpjValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cpfValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.rendimentos;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API do protótipo de pedido: criar, listar, detalhar e cancelar (UC04, UC05, UC07, UC08). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoControllerTest {

    private static final AtomicInteger PLACAS = new AtomicInteger();
    private static final LocalDate RETIRADA = LocalDate.now().plusDays(7);
    private static final LocalDate DEVOLUCAO = RETIRADA.plusDays(7);

    @Autowired private MockMvc mvc;
    @Autowired private AutomovelRepository automoveis;

    private Automovel polo;

    @BeforeEach
    void cadastrarAutomovel() {
        int n = PLACAS.incrementAndGet();
        polo = automoveis.save(new Automovel("%011d".formatted(90_000_000_000L + n), 2024, "Volkswagen", "Polo",
                "PLO%dA%02d".formatted(n % 10, n % 100), new BigDecimal("189.00")));
    }

    // ---------------------------------------------------------------- listas para montar o pedido

    @Test
    void clienteVeOsAutomoveisEOsBancos() throws Exception {
        String cliente = cadastrarCliente("ve.listas");
        cadastrarBanco("banco.listado", "Banco Listado S.A.");

        comToken(get("/automoveis"), cliente)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].placa", hasItem(polo.getPlaca())));
        comToken(get("/bancos"), cliente)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Banco Listado S.A.")));
        mvc.perform(get("/automoveis")).andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- criar

    @Test
    void criaPedidoEnviadoComValorPelasDiarias() throws Exception {
        String token = cadastrarCliente("faz.pedido");

        criar(token, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.matchesPattern("PED-\\d{4,}")))
                .andExpect(jsonPath("$.status").value("ENVIADO"))
                .andExpect(jsonPath("$.diarias").value(7))
                .andExpect(jsonPath("$.valorEstimado").value(1323.00))
                .andExpect(jsonPath("$.podeSerAlterado").value(true))
                .andExpect(jsonPath("$.automovel.modelo").value("Polo"))
                .andExpect(jsonPath("$.credito").doesNotExist());
    }

    @Test
    void criaPedidoComCreditoBancario() throws Exception {
        String token = cadastrarCliente("pede.credito");
        long bancoId = cadastrarBanco("banco.credito", "Banco Horizonte S.A.");
        String credito = "{\"bancoId\":%d,\"valor\":1323.00}".formatted(bancoId);

        criar(token, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, credito))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.credito.banco").value("Banco Horizonte S.A."))
                .andExpect(jsonPath("$.credito.valor").value(1323.00));
    }

    @Test
    void validaDatasAutomovelEBanco() throws Exception {
        String token = cadastrarCliente("pedido.ruim");

        criar(token, novoPedido(polo.getId(), DEVOLUCAO, RETIRADA, null))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("A data de devolução precisa ser posterior à de retirada."));
        criar(token, novoPedido(polo.getId(), LocalDate.now().minusDays(1), DEVOLUCAO, null))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("A data de retirada não pode estar no passado."));
        criar(token, novoPedido(999_999L, RETIRADA, DEVOLUCAO, null))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Automóvel não encontrado."));
        criar(token, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, "{\"bancoId\":999999,\"valor\":100}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Banco não encontrado."));
        criar(token, "{\"dataRetirada\":\"%s\"}".formatted(RETIRADA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.automovelId").value("Escolha o automóvel."))
                .andExpect(jsonPath("$.campos.dataDevolucao").value("Informe a data de devolução."));
    }

    // ---------------------------------------------------------------- consultar

    @Test
    void clienteSoVeOsProprios() throws Exception {
        String mariana = cadastrarCliente("dona.do.pedido");
        String outro = cadastrarCliente("bisbilhoteiro");
        int id = JsonPath.read(corpo(criar(mariana, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, null))), "$.id");
        criar(mariana, novoPedido(polo.getId(), RETIRADA.plusDays(20), DEVOLUCAO.plusDays(21), null));

        comToken(get("/pedidos"), mariana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].dataRetirada").value(RETIRADA.plusDays(20).toString()));
        comToken(get("/pedidos/" + id), mariana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));

        comToken(get("/pedidos"), outro).andExpect(jsonPath("$.length()").value(0));
        comToken(get("/pedidos/" + id), outro)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pedido não encontrado."));
    }

    // ---------------------------------------------------------------- cancelar

    @Test
    void cancelarMudaOStatusEDepoisNaoDeixaCancelarDeNovo() throws Exception {
        String token = cadastrarCliente("desiste");
        int id = JsonPath.read(corpo(criar(token, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, null))), "$.id");

        comToken(post("/pedidos/%d/cancelar".formatted(id)), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.podeSerAlterado").value(false));
        comToken(post("/pedidos/%d/cancelar".formatted(id)), token)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Pedidos com status CANCELADO não podem ser alterados."));
    }

    @Test
    void agenteNaoUsaAsRotasDePedidoDoCliente() throws Exception {
        String agente = JsonPath.read(corpo(mvc.perform(post("/auth/cadastro/agente").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"EMPRESA\",\"razaoSocial\":\"Locadora X\",\"cnpj\":\"%s\",\"login\":\"locadora.x\",\"senha\":\"senha123\"}"
                        .formatted(cnpjValido())))), "$.token");

        comToken(get("/pedidos"), agente).andExpect(status().isForbidden());
        criar(agente, novoPedido(polo.getId(), RETIRADA, DEVOLUCAO, null)).andExpect(status().isForbidden());
        comToken(get("/automoveis"), agente).andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- apoio

    private static String novoPedido(Long automovelId, LocalDate retirada, LocalDate devolucao, String credito) {
        return """
                {"automovelId":%d,"dataRetirada":"%s","dataDevolucao":"%s","credito":%s}
                """.formatted(automovelId, retirada, devolucao, credito);
    }

    private ResultActions criar(String token, String corpo) throws Exception {
        return comToken(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo), token);
    }

    private String cadastrarCliente(String login) throws Exception {
        String corpo = """
                {"nome":"Cliente %s","cpf":"%s","rg":"MG-1","endereco":{"cep":"30140-071","logradouro":"Rua A","numero":"1",
                 "bairro":"Centro","cidade":"Belo Horizonte","uf":"MG"},"rendimentos":%s,"login":"%s","senha":"senha123"}
                """.formatted(login, cpfValido(), rendimentos(1), login);
        return JsonPath.read(corpo(mvc.perform(post("/auth/cadastro/cliente").contentType(MediaType.APPLICATION_JSON).content(corpo))), "$.token");
    }

    private long cadastrarBanco(String login, String razaoSocial) throws Exception {
        String corpo = """
                {"tipo":"BANCO","razaoSocial":"%s","cnpj":"%s","login":"%s","senha":"senha123"}
                """.formatted(razaoSocial, cnpjValido(), login);
        Number id = JsonPath.read(corpo(mvc.perform(post("/auth/cadastro/agente").contentType(MediaType.APPLICATION_JSON).content(corpo))), "$.usuario.id");
        return id.longValue();
    }

    private ResultActions comToken(MockHttpServletRequestBuilder requisicao, String token) throws Exception {
        return mvc.perform(requisicao.header("Authorization", "Bearer " + token));
    }

    private static String corpo(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString();
    }
}
