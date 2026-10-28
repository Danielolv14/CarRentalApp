package br.pucminas.aluguelcarros.controller;

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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API do login e do cadastro (UC01 e UC02), com o Spring Security ligado e banco H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(100_000_000);

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarios;

    // ---------------------------------------------------------------- cadastro de cliente

    @Test
    void cadastroDeClienteDevolveTokenEPerfilCliente() throws Exception {
        cadastrarCliente("ana.ribeiro", cpfValido(), "senha123", rendimentos(2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.login").value("ana.ribeiro"))
                .andExpect(jsonPath("$.usuario.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.tipoAgente").doesNotExist());
    }

    @Test
    void senhaEhGuardadaComBCrypt() throws Exception {
        cadastrarCliente("carlos.mendes", cpfValido(), "minhaSenha1", rendimentos(0)).andExpect(status().isCreated());

        String hash = usuarios.findByLogin("carlos.mendes").orElseThrow().getSenha();
        assertThat(hash).startsWith("$2").isNotEqualTo("minhaSenha1");
    }

    @Test
    void cpfInvalidoVoltaComMensagemNoCampo() throws Exception {
        cadastrarCliente("cpf.errado", "123.456.789-00", "senha123", rendimentos(0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Confira os campos destacados."))
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido."));
    }

    @Test
    void maisDeTresRendimentosNaoEhAceito() throws Exception {
        cadastrarCliente("quatro.rendas", cpfValido(), "senha123", rendimentos(4))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.rendimentos").value("Informe no máximo 3 rendimentos."));
    }

    @Test
    void camposObrigatoriosDoEnderecoSaoValidados() throws Exception {
        String corpo = """
                {"nome":"Sem Endereço","cpf":"%s","rg":"MG1","endereco":{"cep":"123","logradouro":"","numero":"1",
                 "bairro":"Centro","cidade":"BH","uf":"MG"},"login":"sem.endereco","senha":"senha123"}
                """.formatted(cpfValido());
        postJson("/auth/cadastro/cliente", corpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['endereco.cep']").value("CEP inválido."))
                .andExpect(jsonPath("$.campos['endereco.logradouro']").value("Informe a rua."));
    }

    @Test
    void cpfOuLoginRepetidoDevolveConflito() throws Exception {
        String cpf = cpfValido();
        cadastrarCliente("primeiro", cpf, "senha123", rendimentos(0)).andExpect(status().isCreated());

        cadastrarCliente("segundo", cpf, "senha123", rendimentos(0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um cliente com este CPF."));
        cadastrarCliente("PRIMEIRO", cpfValido(), "senha123", rendimentos(0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Este usuário já está em uso."));
    }

    // ---------------------------------------------------------------- cadastro de agente

    @Test
    void cadastroDeBancoEDeEmpresa() throws Exception {
        cadastrarAgente("BANCO", "banco.horizonte", cnpjValido())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.perfil").value("AGENTE"))
                .andExpect(jsonPath("$.usuario.tipoAgente").value("BANCO"))
                .andExpect(jsonPath("$.usuario.nome").value("Instituição banco.horizonte"));

        cadastrarAgente("EMPRESA", "serra.verde", cnpjValido())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.tipoAgente").value("EMPRESA"));
    }

    @Test
    void cnpjInvalidoOuRepetido() throws Exception {
        cadastrarAgente("EMPRESA", "cnpj.errado", "12.345.678/0001-00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").value("CNPJ inválido."));

        String cnpj = cnpjValido();
        cadastrarAgente("EMPRESA", "locadora.um", cnpj).andExpect(status().isCreated());
        cadastrarAgente("BANCO", "locadora.dois", cnpj)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um agente com este CNPJ."));
    }

    @Test
    void tipoDeAgenteInvalidoDevolve400() throws Exception {
        cadastrarAgente("SEGURADORA", "tipo.errado", cnpjValido())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O corpo da requisição está inválido."));
    }

    // ---------------------------------------------------------------- login e rotas protegidas

    @Test
    void loginComSenhaCertaDevolveTokenQueAbreRotasProtegidas() throws Exception {
        cadastrarCliente("mariana.costa", cpfValido(), "rota1234", rendimentos(3)).andExpect(status().isCreated());

        String resposta = postJson("/auth/login", """
                {"login":"  Mariana.Costa ","senha":"rota1234"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nome").value("Cliente mariana.costa"))
                .andExpect(jsonPath("$.expiraEm").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(resposta, "$.token");

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("mariana.costa"))
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void loginComSenhaErradaOuUsuarioInexistenteDevolve401() throws Exception {
        cadastrarCliente("joao.pereira", cpfValido(), "certa123", rendimentos(0)).andExpect(status().isCreated());

        postJson("/auth/login", "{\"login\":\"joao.pereira\",\"senha\":\"errada\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));
        postJson("/auth/login", "{\"login\":\"ninguem\",\"senha\":\"qualquer\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));
    }

    @Test
    void loginSemCamposDevolve400() throws Exception {
        postJson("/auth/login", "{\"login\":\"\",\"senha\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.login").value("Informe o usuário."))
                .andExpect(jsonPath("$.campos.senha").value("Informe a senha."));
    }

    @Test
    void rotaProtegidaSemTokenOuComTokenAdulteradoDevolve401() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());

        String resposta = cadastrarAgente("EMPRESA", "token.teste", cnpjValido())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(resposta, "$.token");
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoAgente").value("EMPRESA"));
    }

    @Test
    void corsLiberaOFrontEnd() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    // ---------------------------------------------------------------- apoio

    private ResultActions cadastrarCliente(String login, String cpf, String senha, String rendimentosJson) throws Exception {
        String corpo = """
                {"nome":"Cliente %s","cpf":"%s","rg":"MG-12.345.678","profissao":"Analista de sistemas",
                 "endereco":{"cep":"30140-071","logradouro":"Avenida Afonso Pena","numero":"1500","bairro":"Centro",
                             "cidade":"Belo Horizonte","uf":"mg"},
                 "rendimentos":%s,"login":"%s","senha":"%s"}
                """.formatted(login, cpf, rendimentosJson, login, senha);
        return postJson("/auth/cadastro/cliente", corpo);
    }

    private ResultActions cadastrarAgente(String tipo, String login, String cnpj) throws Exception {
        String corpo = """
                {"tipo":"%s","razaoSocial":"Instituição %s","cnpj":"%s","login":"%s","senha":"senha123"}
                """.formatted(tipo, login, cnpj, login);
        return postJson("/auth/cadastro/agente", corpo);
    }

    private ResultActions postJson(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String rendimentos(int quantidade) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < quantidade; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"entidadeEmpregadora\":\"Empresa ").append(i + 1).append("\",\"valor\":1500.50}");
        }
        return json.append(']').toString();
    }

    /** Gera um CPF válido diferente a cada chamada (dígitos verificadores calculados). */
    private static String cpfValido() {
        String base = String.valueOf(SEQUENCIA.incrementAndGet());
        int d1 = digitoModulo11(base, 10);
        int d2 = digitoModulo11(base + d1, 11);
        return base + d1 + d2;
    }

    /** Gera um CNPJ válido diferente a cada chamada. */
    private static String cnpjValido() {
        String base = String.format("%08d", SEQUENCIA.incrementAndGet() % 100_000_000) + "0001";
        int d1 = digitoCnpj(base, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int d2 = digitoCnpj(base + d1, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return base + d1 + d2;
    }

    private static int digitoModulo11(String numeros, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < numeros.length(); i++) {
            soma += (numeros.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoCnpj(String numeros, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (numeros.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
