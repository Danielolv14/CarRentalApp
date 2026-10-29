package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.controller.ApiDeTeste.Conta;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;
import java.util.List;

import static br.pucminas.aluguelcarros.controller.ApiDeTeste.corpo;
import static br.pucminas.aluguelcarros.controller.ApiDeTeste.numero;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API da frota: a empresa cadastra, edita e remove automóveis (UC13). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutomovelControllerTest {

    @Autowired private MockMvc mvc;

    private ApiDeTeste api;

    @BeforeEach
    void preparar() {
        api = new ApiDeTeste(mvc);
    }

    @Test
    void empresaCadastraEditaERemove() throws Exception {
        Conta empresa = api.empresa("frota.locadora");
        String placa = ApiDeTeste.placa("FRT");
        String placaComTraco = placa.substring(0, 3).toLowerCase() + "-" + placa.substring(3);

        long id = numero(corpo(api.postar("/automoveis", empresa.token(), automovel(ApiDeTeste.matricula(72_000_000_000L), placaComTraco, 2024, "210.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value(placa))
                .andExpect(jsonPath("$.proprietario").doesNotExist())), "$.id");

        api.alterar("/automoveis/" + id, empresa.token(), automovel(ApiDeTeste.matricula(72_000_000_000L), placa, 2025, "225.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano").value(2025))
                .andExpect(jsonPath("$.valorDiaria").value(225.50));

        api.apagar("/automoveis/" + id, empresa.token()).andExpect(status().isNoContent());
        List<Number> ids = JsonPath.read(corpo(api.obter("/automoveis", empresa.token())), "$[*].id");
        assertThat(ids.stream().map(Number::longValue)).doesNotContain(id);
        api.apagar("/automoveis/" + id, empresa.token()).andExpect(status().isNotFound());
    }

    @Test
    void validaPlacaMatriculaAnoEDuplicados() throws Exception {
        Conta empresa = api.empresa("frota.valida");
        String matricula = ApiDeTeste.matricula(72_000_000_000L);
        String placa = ApiDeTeste.placa("FRT");
        api.postar("/automoveis", empresa.token(), automovel(matricula, placa, 2024, "150.00")).andExpect(status().isCreated());

        api.postar("/automoveis", empresa.token(), automovel("123", "AB12345", 2024, "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.matricula").value("A matrícula (RENAVAM) tem 11 dígitos."))
                .andExpect(jsonPath("$.campos.placa").value("Placa inválida. Use o padrão ABC1D23."))
                .andExpect(jsonPath("$.campos.valorDiaria").value("A diária precisa ser maior que zero."));
        api.postar("/automoveis", empresa.token(), automovel(ApiDeTeste.matricula(72_000_000_000L), placa, 2024, "150.00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um automóvel com esta placa."));
        api.postar("/automoveis", empresa.token(), automovel(matricula, ApiDeTeste.placa("FRT"), 2024, "150.00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um automóvel com esta matrícula."));
        int anoQueVem = Year.now().getValue() + 2;
        api.postar("/automoveis", empresa.token(), automovel(ApiDeTeste.matricula(72_000_000_000L), ApiDeTeste.placa("FRT"), anoQueVem, "150.00"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O ano do automóvel pode ser no máximo " + (anoQueVem - 1) + "."));
    }

    @Test
    void automovelComPedidoNaoSaiDaFrotaENovaDiariaNaoMudaOPedido() throws Exception {
        Conta empresa = api.empresa("frota.historico");
        Conta cliente = api.cliente("frota.cliente");
        String matricula = ApiDeTeste.matricula(72_000_000_000L);
        String placa = ApiDeTeste.placa("FRT");
        long id = numero(corpo(api.postar("/automoveis", empresa.token(), automovel(matricula, placa, 2024, "100.00"))), "$.id");
        long pedido = api.pedido(cliente, id, null, null);

        api.alterar("/automoveis/" + id, empresa.token(), automovel(matricula, placa, 2024, "130.00")).andExpect(status().isOk());
        api.obter("/pedidos/" + pedido, cliente.token())
                .andExpect(jsonPath("$.valorDiaria").value(100.00))
                .andExpect(jsonPath("$.valorEstimado").value(700.00));

        api.apagar("/automoveis/" + id, empresa.token())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Este automóvel já aparece em pedidos ou contratos e não pode ser removido."));
    }

    @Test
    void soAEmpresaMexeNaFrota() throws Exception {
        Conta banco = api.banco("frota.banco", "Banco Frota S.A.");
        Conta cliente = api.cliente("frota.olha");
        String corpo = automovel(ApiDeTeste.matricula(72_000_000_000L), ApiDeTeste.placa("FRT"), 2024, "150.00");

        api.postar("/automoveis", banco.token(), corpo).andExpect(status().isForbidden());
        api.postar("/automoveis", cliente.token(), corpo).andExpect(status().isForbidden());
        api.apagar("/automoveis/1", cliente.token()).andExpect(status().isForbidden());
        api.obter("/automoveis", banco.token()).andExpect(status().isOk());
        api.obter("/automoveis", cliente.token()).andExpect(status().isOk());
    }

    private static String automovel(String matricula, String placa, int ano, String diaria) {
        return """
                {"matricula":"%s","marca":"Fiat","modelo":"Pulse","ano":%d,"placa":"%s","valorDiaria":%s}
                """.formatted(matricula, ano, placa, diaria);
    }
}
