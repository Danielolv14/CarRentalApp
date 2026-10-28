package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.") String cep,
        @NotBlank(message = "Informe a rua.") String logradouro,
        @NotBlank(message = "Informe o número.") String numero,
        @NotBlank(message = "Informe o bairro.") String bairro,
        @NotBlank(message = "Informe a cidade.") String cidade,
        @NotBlank(message = "Informe a UF.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida.") String uf) {
}
