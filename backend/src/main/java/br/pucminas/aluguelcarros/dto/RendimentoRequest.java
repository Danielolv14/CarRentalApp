package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RendimentoRequest(
        @NotBlank(message = "Informe a entidade empregadora.") String entidadeEmpregadora,
        @NotNull(message = "Informe o rendimento.")
        @Positive(message = "O rendimento precisa ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "Rendimento inválido.") BigDecimal valor) {
}
