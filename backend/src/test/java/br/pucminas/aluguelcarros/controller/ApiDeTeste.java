package br.pucminas.aluguelcarros.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cnpjValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cpfValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.rendimentos;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Atalhos dos testes de API do lado do agente: cria contas e pedidos pela própria API e chama as rotas com o token.
 * O banco H2 é o mesmo para todas as classes de teste, então os logins ganham um número no fim.
 */
final class ApiDeTeste {

    static final LocalDate RETIRADA = LocalDate.now().plusDays(7);
    static final LocalDate DEVOLUCAO = RETIRADA.plusDays(7);

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    private final MockMvc mvc;

    ApiDeTeste(MockMvc mvc) {
        this.mvc = mvc;
    }

    record Conta(String token, long id) {
    }

    /** Cliente com 1 rendimento de R$ 1.500,50. */
    Conta cliente(String login) throws Exception {
        String corpo = """
                {"nome":"Cliente %s","cpf":"%s","rg":"MG-1","profissao":"Analista","endereco":{"cep":"30140-071",
                 "logradouro":"Rua A","numero":"1","bairro":"Centro","cidade":"Belo Horizonte","uf":"MG"},
                 "rendimentos":%s,"login":"%s","senha":"senha123"}
                """.formatted(login, cpfValido(), rendimentos(1), unico(login));
        return conta(mvc.perform(post("/auth/cadastro/cliente").contentType(MediaType.APPLICATION_JSON).content(corpo)));
    }

    Conta empresa(String login) throws Exception {
        return agente("EMPRESA", login, "Locadora " + login);
    }

    Conta banco(String login, String razaoSocial) throws Exception {
        return agente("BANCO", login, razaoSocial);
    }

    /** Pedido feito pelo cliente; com bancoId, pede crédito do valor informado. */
    long pedido(Conta cliente, long automovelId, Long bancoId, String valorCredito) throws Exception {
        String credito = bancoId == null ? "null" : "{\"bancoId\":%d,\"valor\":%s}".formatted(bancoId, valorCredito);
        String corpo = """
                {"automovelId":%d,"dataRetirada":"%s","dataDevolucao":"%s","credito":%s}
                """.formatted(automovelId, RETIRADA, DEVOLUCAO, credito);
        return numero(corpo(enviar(post("/pedidos"), cliente.token(), corpo)), "$.id");
    }

    /** Placa Mercosul diferente a cada chamada (ABC1D23). */
    static String placa(String prefixo) {
        int n = SEQUENCIA.incrementAndGet();
        return "%s%d%c%02d".formatted(prefixo, n % 10, (char) ('A' + n % 26), n % 100);
    }

    static String matricula(long base) {
        return "%011d".formatted(base + SEQUENCIA.incrementAndGet());
    }

    ResultActions obter(String url, String token) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + token));
    }

    ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, String json) throws Exception {
        if (json != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(requisicao.header("Authorization", "Bearer " + token));
    }

    ResultActions postar(String url, String token, String json) throws Exception {
        return enviar(post(url), token, json);
    }

    ResultActions alterar(String url, String token, String json) throws Exception {
        return enviar(put(url), token, json);
    }

    ResultActions apagar(String url, String token) throws Exception {
        return enviar(delete(url), token, null);
    }

    static String corpo(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString();
    }

    static long numero(String json, String caminho) {
        Number n = JsonPath.read(json, caminho);
        return n.longValue();
    }

    private Conta agente(String tipo, String login, String razaoSocial) throws Exception {
        String corpo = """
                {"tipo":"%s","razaoSocial":"%s","cnpj":"%s","login":"%s","senha":"senha123"}
                """.formatted(tipo, razaoSocial, cnpjValido(), unico(login));
        return conta(mvc.perform(post("/auth/cadastro/agente").contentType(MediaType.APPLICATION_JSON).content(corpo)));
    }

    private static Conta conta(ResultActions cadastro) throws Exception {
        String json = corpo(cadastro);
        return new Conta(JsonPath.read(json, "$.token"), numero(json, "$.usuario.id"));
    }

    private static String unico(String login) {
        return login + "." + SEQUENCIA.incrementAndGet();
    }
}
