package br.pucminas.aluguelcarros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** UC04 Introduzir pedido. "credito" é opcional (UC08 Solicitar crédito bancário). */
public record NovoPedidoRequest(
        @NotNull(message = "Escolha o automóvel.") Long automovelId,
        @NotNull(message = "Informe a data de retirada.") LocalDate dataRetirada,
        @NotNull(message = "Informe a data de devolução.") LocalDate dataDevolucao,
        @Valid CreditoRequest credito) {

    public record CreditoRequest(
            @NotNull(message = "Escolha o banco.") Long bancoId,
            @NotNull(message = "Informe o valor do crédito.")
            @Positive(message = "O crédito precisa ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Valor de crédito inválido.") BigDecimal valor) {
    }
}
