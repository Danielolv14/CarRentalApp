package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cnpjValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.cpfValido;
import static br.pucminas.aluguelcarros.controller.DocumentosDeTeste.rendimentos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API do CRUD de cliente (UC03): consultar, alterar e excluir o próprio cadastro. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ClienteRepository clientes;
    @Autowired private AutomovelRepository automoveis;
    @Autowired private PedidoRepository pedidos;

    // ---------------------------------------------------------------- consultar

    @Test
    void consultaDevolveOCadastroCompleto() throws Exception {
        String cpf = cpfValido();
        String token = cadastrar("perfil.completo", cpf, rendimentos(2));

        comToken(get("/clientes/me"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("perfil.completo"))
                .andExpect(jsonPath("$.nome").value("Cliente Original"))
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.rg").value("MG-12.345.678"))
                .andExpect(jsonPath("$.endereco.cep").value("30140071"))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andExpect(jsonPath("$.rendimentos.length()").value(2))
                .andExpect(jsonPath("$.rendaTotal").value(3001.00));
    }

    @Test
    void semTokenDevolve401EAgenteDevolve403() throws Exception {
        mvc.perform(get("/clientes/me")).andExpect(status().isUnauthorized());

        String tokenAgente = cadastrarAgente("agente.curioso");
        comToken(get("/clientes/me"), tokenAgente).andExpect(status().isForbidden());
        comToken(delete("/clientes/me"), tokenAgente).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- alterar

    @Test
    void alterarTrocaOsDadosEOsRendimentosMasNaoOCpf() throws Exception {
        String cpf = cpfValido();
        String token = cadastrar("vai.mudar", cpf, rendimentos(1));

        String corpo = """
                {"nome":"Nome Novo","rg":"SP-98.765.432","profissao":"Engenheira","cpf":"%s",
                 "endereco":{"cep":"01310-100","logradouro":"Avenida Paulista","numero":"1000","bairro":"Bela Vista",
                             "cidade":"São Paulo","uf":"sp"},
                 "rendimentos":[{"entidadeEmpregadora":"Nova Empresa","valor":8000},
                                {"entidadeEmpregadora":"Consultoria","valor":1000.25},
                                {"entidadeEmpregadora":"Aulas","valor":500}]}
                """.formatted(cpfValido());
        comToken(put("/clientes/me").contentType(MediaType.APPLICATION_JSON).content(corpo), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.rendimentos.length()").value(3))
                .andExpect(jsonPath("$.rendimentos[0].id").isNumber())
                .andExpect(jsonPath("$.rendaTotal").value(9500.25));

        comToken(get("/clientes/me"), token)
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.rg").value("SP-98.765.432"))
                .andExpect(jsonPath("$.profissao").value("Engenheira"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.uf").value("SP"))
                .andExpect(jsonPath("$.rendimentos[0].entidadeEmpregadora").value("Nova Empresa"));
    }

    @Test
    void alterarPodeTirarTodosOsRendimentos() throws Exception {
        String token = cadastrar("sem.renda", cpfValido(), rendimentos(3));

        comToken(put("/clientes/me").contentType(MediaType.APPLICATION_JSON).content(atualizacao(rendimentos(0), "30140-071")), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rendimentos.length()").value(0))
                .andExpect(jsonPath("$.rendaTotal").value(0));
    }

    @Test
    void alterarValidaOsCampos() throws Exception {
        String token = cadastrar("dados.ruins", cpfValido(), rendimentos(0));

        comToken(put("/clientes/me").contentType(MediaType.APPLICATION_JSON).content(atualizacao(rendimentos(4), "123")), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.rendimentos").value("Informe no máximo 3 rendimentos."))
                .andExpect(jsonPath("$.campos['endereco.cep']").value("CEP inválido."));
    }

    // ---------------------------------------------------------------- excluir

    @Test
    void excluirSemPedidosApagaOCadastro() throws Exception {
        String token = cadastrar("vai.sair", cpfValido(), rendimentos(2));

        comToken(delete("/clientes/me"), token).andExpect(status().isNoContent());

        assertThat(usuarios.findByLogin("vai.sair")).isEmpty();
        comToken(get("/clientes/me"), token).andExpect(status().isUnauthorized());
        login("vai.sair").andExpect(status().isUnauthorized());
    }

    @Test
    void excluirComHistoricoCancelaOsPedidosAbertosEDesativaAConta() throws Exception {
        String token = cadastrar("tem.historico", cpfValido(), rendimentos(1));
        Cliente cliente = clientes.findByLogin("tem.historico").orElseThrow();
        Automovel carro = automoveis.save(new Automovel(String.valueOf(System.nanoTime()).substring(0, 11), 2024,
                "Fiat", "Argo", "TST" + (System.nanoTime() % 10) + "A" + (System.nanoTime() % 90 + 10), new BigDecimal("159.00")));
        Empresa locadora = usuarios.save(new Empresa("locadora.hist", "hash", "Locadora", cnpjValido(), "Locadora Ltda."));

        Pedido aberto = pedidos.save(new Pedido(cliente, carro, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));
        Pedido aprovado = new Pedido(cliente, carro, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
        locadora.avaliarPedido(aprovado, ResultadoParecer.POSITIVO, "Renda compatível.");
        aprovado = pedidos.save(aprovado);

        comToken(delete("/clientes/me"), token).andExpect(status().isNoContent());

        assertThat(pedidos.findById(aberto.getId()).orElseThrow().getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(pedidos.findById(aprovado.getId()).orElseThrow().getStatus()).isEqualTo(StatusPedido.APROVADO);
        assertThat(usuarios.findByLogin("tem.historico").orElseThrow().isAtivo()).isFalse();
        comToken(get("/clientes/me"), token).andExpect(status().isUnauthorized());
        login("tem.historico").andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- apoio

    private String cadastrar(String login, String cpf, String rendimentosJson) throws Exception {
        String corpo = """
                {"nome":"Cliente Original","cpf":"%s","rg":"MG-12.345.678","profissao":"Analista",
                 "endereco":{"cep":"30140-071","logradouro":"Avenida Afonso Pena","numero":"1500","bairro":"Centro",
                             "cidade":"Belo Horizonte","uf":"MG"},
                 "rendimentos":%s,"login":"%s","senha":"senha123"}
                """.formatted(cpf, rendimentosJson, login);
        String resposta = mvc.perform(post("/auth/cadastro/cliente").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private String cadastrarAgente(String login) throws Exception {
        String corpo = """
                {"tipo":"EMPRESA","razaoSocial":"Locadora %s","cnpj":"%s","login":"%s","senha":"senha123"}
                """.formatted(login, cnpjValido(), login);
        String resposta = mvc.perform(post("/auth/cadastro/agente").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private static String atualizacao(String rendimentosJson, String cep) {
        return """
                {"nome":"Fulano","rg":"MG-1","profissao":"","endereco":{"cep":"%s","logradouro":"Rua A","numero":"1",
                 "bairro":"Centro","cidade":"Belo Horizonte","uf":"MG"},"rendimentos":%s}
                """.formatted(cep, rendimentosJson);
    }

    private ResultActions login(String login) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"senha123\"}".formatted(login)));
    }

    private ResultActions comToken(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
                                   String token) throws Exception {
        return mvc.perform(requisicao.header("Authorization", "Bearer " + token));
    }
}
