package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.ContratoCredito;

import java.math.BigDecimal;

/** Condições do crédito que o banco concedeu, com a parcela calculada pela tabela Price. */
public record CreditoConcedidoResponse(String banco, BigDecimal valor, int parcelas, BigDecimal taxaMensal,
                                       BigDecimal valorParcela) {

    public static CreditoConcedidoResponse de(ContratoCredito c) {
        return c == null ? null
                : new CreditoConcedidoResponse(c.getBanco().getRazaoSocial(), c.getValor(), c.getNumeroParcelas(),
                c.getTaxaJurosMensal(), c.calcularValorParcela());
    }
}
