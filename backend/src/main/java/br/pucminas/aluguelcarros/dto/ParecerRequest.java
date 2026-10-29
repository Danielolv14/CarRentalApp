package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.ResultadoParecer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * UC09 Avaliar pedido. "credito" é obrigatório só no parecer positivo de um banco (UC10 Conceder crédito):
 * valor, número de parcelas e taxa de juros ao mês em percentual (1.90 = 1,90% a.m.).
 */
public record ParecerRequest(
        @NotNull(message = "Escolha positivo ou negativo.") ResultadoParecer resultado,
        @NotBlank(message = "Escreva a justificativa.")
        @Size(max = 1000, message = "A justificativa pode ter até 1000 caracteres.") String justificativa,
        @Valid CondicoesCredito credito) {

    public record CondicoesCredito(
            @NotNull(message = "Informe o valor do crédito.")
            @Positive(message = "O crédito precisa ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Valor de crédito inválido.") BigDecimal valor,
            @NotNull(message = "Informe o número de parcelas.")
            @Min(value = 1, message = "No mínimo 1 parcela.") @Max(value = 60, message = "No máximo 60 parcelas.") Integer parcelas,
            @NotNull(message = "Informe a taxa ao mês.")
            @DecimalMin(value = "0.00", message = "A taxa não pode ser negativa.")
            @DecimalMax(value = "20.00", message = "A taxa pode ser no máximo 20% ao mês.")
            @Digits(integer = 2, fraction = 4, message = "Taxa inválida.") BigDecimal taxaMensal) {
    }
}
