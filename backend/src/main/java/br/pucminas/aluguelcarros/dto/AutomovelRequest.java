package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Locale;

/** UC13 Manter automóveis: dados para cadastrar ou editar um automóvel da frota. */
public record AutomovelRequest(
        @NotBlank(message = "Informe a matrícula (RENAVAM).")
        @Pattern(regexp = "\\d{11}", message = "A matrícula (RENAVAM) tem 11 dígitos.") String matricula,
        @NotBlank(message = "Informe a marca.") @Size(max = 60, message = "Marca muito longa.") String marca,
        @NotBlank(message = "Informe o modelo.") @Size(max = 60, message = "Modelo muito longo.") String modelo,
        @NotNull(message = "Informe o ano.") @Min(value = 1950, message = "Ano inválido.") Integer ano,
        @NotBlank(message = "Informe a placa.")
        @Pattern(regexp = "[A-Za-z]{3}-?\\d[A-Za-z0-9]\\d{2}", message = "Placa inválida. Use o padrão ABC1D23.") String placa,
        @NotNull(message = "Informe o valor da diária.")
        @Positive(message = "A diária precisa ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "Diária inválida.") BigDecimal valorDiaria) {

    /** Placa sem traço e em maiúsculas (ABC1D23). */
    public String placaNormalizada() {
        return placa.replace("-", "").toUpperCase(Locale.ROOT);
    }
}
