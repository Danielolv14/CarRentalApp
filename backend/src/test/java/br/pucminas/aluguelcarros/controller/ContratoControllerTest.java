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
import java.time.Year;
import java.util.List;

import static br.pucminas.aluguelcarros.controller.ApiDeTeste.corpo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes de API dos contratos: executar o contrato de um pedido aprovado e consultar os contratos (UC11, UC12). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContratoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private AutomovelRepository automoveis;

    private ApiDeTeste api;
    private Automovel polo;

    @BeforeEach
    void preparar() {
        api = new ApiDeTeste(mvc);
        polo = automoveis.save(new Automovel(ApiDeTeste.matricula(71_000_000_000L), 2024, "Volkswagen", "Polo",
                ApiDeTeste.placa("CTR"), new BigDecimal("189.00")));
    }

    @Test
    void empresaExecutaOContratoDeUmPedidoAprovado() throws Exception {
        Conta cliente = api.cliente("contrato.cliente");
        Conta empresa = api.empresa("contrato.locadora");
        long id = api.pedido(cliente, polo.getId(), null, null);
        String url = "/agente/pedidos/%d/contrato".formatted(id);

        api.postar(url, empresa.token(), proprietario("EMPRESA"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Só pedidos aprovados podem virar contrato."));
        aprovar(empresa, id);

        api.postar(url, empresa.token(), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipoProprietario").value("Escolha o proprietário do automóvel."));
        String contrato = corpo(api.postar(url, empresa.token(), proprietario("EMPRESA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(matchesPattern("CTR-%d-\\d{4}".formatted(Year.now().getValue()))))
                .andExpect(jsonPath("$.tipoProprietario").value("EMPRESA"))
                .andExpect(jsonPath("$.proprietario").value("Locadora contrato.locadora"))
                .andExpect(jsonPath("$.cliente").value("Cliente contrato.cliente"))
                .andExpect(jsonPath("$.valor").value(1323.00))
                .andExpect(jsonPath("$.credito").doesNotExist()));
        String numero = JsonPath.read(contrato, "$.numero");

        api.postar(url, empresa.token(), proprietario("CLIENTE")).andExpect(status().isUnprocessableContent());
        api.obter("/pedidos/" + id, cliente.token())
                .andExpect(jsonPath("$.status").value("CONTRATADO"))
                .andExpect(jsonPath("$.contrato.numero").value(numero));
        String frota = corpo(api.obter("/automoveis", cliente.token()));
        assertThat(JsonPath.<List<String>>read(frota, "$[?(@.id == %d)].proprietario".formatted(polo.getId()))).containsExactly("EMPRESA");
        assertThat(JsonPath.<List<String>>read(corpo(api.obter("/agente/contratos", empresa.token())), "$[*].numero")).contains(numero);
    }

    @Test
    void contratoComCreditoDoBancoFicaAssociadoEOBancoPodeSerOProprietario() throws Exception {
        Conta cliente = api.cliente("contrato.credito");
        Conta empresa = api.empresa("contrato.empresa2");
        Conta banco = api.banco("contrato.banco", "Banco Contrato S.A.");
        Conta outroBanco = api.banco("contrato.outro", "Outro Contrato S.A.");
        long id = api.pedido(cliente, polo.getId(), banco.id(), "1323.00");
        api.postar("/agente/pedidos/%d/parecer".formatted(id), banco.token(), """
                {"resultado":"POSITIVO","justificativa":"Crédito aprovado.","credito":{"valor":1323.00,"parcelas":3,"taxaMensal":1.9}}
                """).andExpect(status().isOk());

        api.postar("/agente/pedidos/%d/contrato".formatted(id), banco.token(), proprietario("EMPRESA"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Só uma empresa pode registrar o automóvel em nome da empresa."));
        String numero = JsonPath.read(corpo(api.postar("/agente/pedidos/%d/contrato".formatted(id), empresa.token(), proprietario("BANCO"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proprietario").value("Banco Contrato S.A."))
                .andExpect(jsonPath("$.credito.banco").value("Banco Contrato S.A."))
                .andExpect(jsonPath("$.credito.valorParcela").value(457.86))), "$.numero");

        assertThat(JsonPath.<List<String>>read(corpo(api.obter("/agente/contratos", banco.token())), "$[*].numero")).containsExactly(numero);
        api.obter("/agente/contratos", outroBanco.token()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void clienteNaoVeOsContratosDosAgentes() throws Exception {
        Conta cliente = api.cliente("contrato.curioso");
        api.obter("/agente/contratos", cliente.token()).andExpect(status().isForbidden());
    }

    private void aprovar(Conta agente, long pedidoId) throws Exception {
        api.postar("/agente/pedidos/%d/parecer".formatted(pedidoId), agente.token(),
                "{\"resultado\":\"POSITIVO\",\"justificativa\":\"Renda compatível.\"}").andExpect(status().isOk());
    }

    private static String proprietario(String tipo) {
        return "{\"tipoProprietario\":\"%s\"}".formatted(tipo);
    }
}
