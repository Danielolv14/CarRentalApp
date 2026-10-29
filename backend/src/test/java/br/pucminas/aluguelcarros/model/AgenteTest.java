package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Quem acompanha e quem avalia cada pedido: a empresa fica com os pedidos sem crédito, o banco com os de crédito. */
class AgenteTest {

    private static final LocalDate RETIRADA = LocalDate.of(2026, 11, 5);
    private static final LocalDate DEVOLUCAO = LocalDate.of(2026, 11, 12);

    private Cliente cliente;
    private Automovel polo;
    private Empresa locadora;
    private Banco horizonte;
    private Banco outroBanco;
    private Pedido semCredito;
    private Pedido comCredito;

    @BeforeEach
    void preparar() {
        cliente = new Cliente("mariana", "hash", "Mariana Costa", "MG12345678", "12345678909", null,
                new Endereco("30140071", "Avenida Afonso Pena", "1500", "Centro", "Belo Horizonte", "MG"));
        polo = new Automovel("01359872046", 2024, "Volkswagen", "Polo", "RTA2B45", new BigDecimal("189.00"));
        locadora = new Empresa("serraverde", "hash", "Locadora Serra Verde", "12345678000190", "Locadora Serra Verde Ltda.");
        horizonte = new Banco("horizonte", "hash", "Banco Horizonte", "98765432000110", "Banco Horizonte S.A.");
        outroBanco = new Banco("outro", "hash", "Outro Banco", "11111111000111", "Outro Banco S.A.");
        semCredito = new Pedido(cliente, polo, RETIRADA, DEVOLUCAO);
        comCredito = new Pedido(cliente, polo, RETIRADA, DEVOLUCAO);
        comCredito.solicitarCredito(horizonte, new BigDecimal("1323.00"));
    }

    @Test
    void empresaAcompanhaTudoMasSoAvaliaPedidosSemCredito() {
        assertThat(locadora.podeAcompanhar(semCredito)).isTrue();
        assertThat(locadora.podeAcompanhar(comCredito)).isTrue();
        assertThat(locadora.podeAvaliar(semCredito)).isTrue();
        assertThat(locadora.podeAvaliar(comCredito)).isFalse();

        assertThatThrownBy(() -> locadora.avaliarPedido(comCredito, ResultadoParecer.POSITIVO, "Ok."))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O pedido pediu crédito ao Banco Horizonte S.A.: o parecer é do banco.");
        assertThat(comCredito.getStatus()).isEqualTo(StatusPedido.ENVIADO);
    }

    @Test
    void bancoSoVeEAvaliaOsPedidosComCreditoParaEle() {
        assertThat(horizonte.podeAcompanhar(comCredito)).isTrue();
        assertThat(horizonte.podeAvaliar(comCredito)).isTrue();
        assertThat(horizonte.podeAcompanhar(semCredito)).isFalse();
        assertThat(outroBanco.podeAcompanhar(comCredito)).isFalse();

        horizonte.iniciarAnalise(comCredito);
        assertThat(comCredito.getStatus()).isEqualTo(StatusPedido.EM_ANALISE);
        horizonte.avaliarPedido(comCredito, ResultadoParecer.NEGATIVO, "Renda insuficiente para o crédito.");
        assertThat(comCredito.getStatus()).isEqualTo(StatusPedido.REPROVADO);

        assertThatThrownBy(() -> horizonte.iniciarAnalise(semCredito)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void agenteModificaOPedidoQueAvaliaEOValorUsaADiariaAtual() {
        Automovel corolla = new Automovel("01371854620", 2024, "Toyota", "Corolla", "RFZ1G37", new BigDecimal("299.00"));
        locadora.modificarPedido(semCredito, corolla, RETIRADA, RETIRADA.plusDays(2));

        assertThat(semCredito.getAutomovel()).isSameAs(corolla);
        assertThat(semCredito.calcularValorEstimado()).isEqualByComparingTo("598.00");
        assertThatThrownBy(() -> locadora.modificarPedido(comCredito, corolla, RETIRADA, DEVOLUCAO))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void mudarADiariaDaFrotaNaoMudaPedidosJaFeitos() {
        polo.setValorDiaria(new BigDecimal("250.00"));

        assertThat(semCredito.getValorDiaria()).isEqualByComparingTo("189.00");
        assertThat(semCredito.calcularValorEstimado()).isEqualByComparingTo("1323.00");
    }
}
