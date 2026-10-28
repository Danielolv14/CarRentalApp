package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Ciclo de vida do pedido (ENVIADO → EM_ANALISE → APROVADO/REPROVADO → CONTRATADO, ou CANCELADO). */
class PedidoTest {

    private static final LocalDate RETIRADA = LocalDate.of(2026, 11, 5);
    private static final LocalDate DEVOLUCAO = LocalDate.of(2026, 11, 12);

    private Cliente cliente;
    private Automovel polo;
    private Empresa locadora;
    private Banco banco;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        cliente = new Cliente("mariana", "hash", "Mariana Costa", "MG12345678", "12345678909", "Analista de sistemas",
                new Endereco("30140071", "Avenida Afonso Pena", "1500", "Centro", "Belo Horizonte", "MG"));
        polo = new Automovel("01359872046", 2024, "Volkswagen", "Polo", "RTA2B45", new BigDecimal("189.00"));
        locadora = new Empresa("serraverde", "hash", "Locadora Serra Verde", "12345678000190", "Locadora Serra Verde Ltda.");
        banco = new Banco("horizonte", "hash", "Banco Horizonte", "98765432000110", "Banco Horizonte S.A.");
        pedido = new Pedido(cliente, polo, RETIRADA, DEVOLUCAO);
    }

    @Test
    void novoPedidoComecaEnviadoECalculaOValorPelasDiarias() {
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENVIADO);
        assertThat(pedido.calcularDiarias()).isEqualTo(7);
        assertThat(pedido.calcularValorEstimado()).isEqualByComparingTo("1323.00");
    }

    @Test
    void devolucaoAntesDaRetiradaNaoEhAceita() {
        assertThatThrownBy(() -> new Pedido(cliente, polo, DEVOLUCAO, RETIRADA))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void parecerPositivoAprovaENegativoReprova() {
        pedido.iniciarAnalise();
        locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, "Renda compatível.");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.APROVADO);
        assertThat(pedido.getPareceres()).hasSize(1);

        Pedido outro = new Pedido(cliente, polo, RETIRADA, DEVOLUCAO);
        locadora.avaliarPedido(outro, ResultadoParecer.NEGATIVO, "Renda insuficiente.");
        assertThat(outro.getStatus()).isEqualTo(StatusPedido.REPROVADO);
    }

    @Test
    void parecerSemJustificativaNaoEhAceito() {
        assertThatThrownBy(() -> locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, " "))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void soPodeAlterarOuCancelarEnquantoEnviadoOuEmAnalise() {
        pedido.alterar(polo, RETIRADA, DEVOLUCAO.plusDays(1));
        assertThat(pedido.calcularDiarias()).isEqualTo(8);

        locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, "Ok.");
        assertThatThrownBy(pedido::cancelar).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> pedido.alterar(polo, RETIRADA, DEVOLUCAO)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelarMudaOStatus() {
        pedido.cancelar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(pedido.podeSerAlterado()).isFalse();
    }

    @Test
    void executarContratoExigePedidoAprovadoERegistraOProprietario() {
        assertThatThrownBy(() -> locadora.executarContrato(pedido, "CTR-2026-0001", TipoProprietario.EMPRESA))
                .isInstanceOf(RegraNegocioException.class);

        locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, "Ok.");
        Contrato contrato = locadora.executarContrato(pedido, "CTR-2026-0001", TipoProprietario.EMPRESA);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONTRATADO);
        assertThat(contrato.getTipoProprietario()).isEqualTo(TipoProprietario.EMPRESA);
        assertThat(polo.getProprietario()).isSameAs(locadora);
    }

    @Test
    void proprietarioBancoExigeCreditoSolicitado() {
        locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, "Ok.");
        assertThatThrownBy(() -> locadora.executarContrato(pedido, "CTR-2026-0002", TipoProprietario.BANCO))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void bancoSoConcedeCreditoQuandoFoiEscolhidoPeloCliente() {
        Banco outroBanco = new Banco("outro", "hash", "Outro Banco", "11111111000111", "Outro Banco S.A.");
        pedido.solicitarCredito(banco, new BigDecimal("1323.00"));

        assertThatThrownBy(() -> outroBanco.concederCredito(pedido, new BigDecimal("1323.00"), 3, new BigDecimal("1.90")))
                .isInstanceOf(RegraNegocioException.class);

        ContratoCredito credito = banco.concederCredito(pedido, new BigDecimal("1323.00"), 3, new BigDecimal("1.90"));
        assertThat(credito.getBanco()).isSameAs(banco);
    }
}
