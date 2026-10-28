package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Parcela pela tabela Price. Valores conferidos na calculadora: 1.729,00 em 3x a 1,9% a.m. = 598,37. */
class ContratoCreditoTest {

    private final Pedido pedido = new Pedido(null, null, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 17));
    private final Banco banco = new Banco("horizonte", "hash", "Banco Horizonte", "98765432000110", "Banco Horizonte S.A.");

    @Test
    void calculaParcelaPelaTabelaPrice() {
        ContratoCredito credito = new ContratoCredito(pedido, banco, new BigDecimal("1729.00"), 3, new BigDecimal("1.90"));
        assertThat(credito.calcularValorParcela()).isEqualByComparingTo("598.37");
    }

    @Test
    void semJurosDivideIgual() {
        ContratoCredito credito = new ContratoCredito(pedido, banco, new BigDecimal("900.00"), 3, BigDecimal.ZERO);
        assertThat(credito.calcularValorParcela()).isEqualByComparingTo("300.00");
    }

    @Test
    void recusaValoresInvalidos() {
        assertThatThrownBy(() -> new ContratoCredito(pedido, banco, BigDecimal.ZERO, 3, BigDecimal.ONE))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new ContratoCredito(pedido, banco, BigDecimal.TEN, 0, BigDecimal.ONE))
                .isInstanceOf(RegraNegocioException.class);
    }
}
