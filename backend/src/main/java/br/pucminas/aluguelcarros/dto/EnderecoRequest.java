package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Locale;

public record EnderecoRequest(
        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.") String cep,
        @NotBlank(message = "Informe a rua.") String logradouro,
        @NotBlank(message = "Informe o número.") String numero,
        @NotBlank(message = "Informe o bairro.") String bairro,
        @NotBlank(message = "Informe a cidade.") String cidade,
        @NotBlank(message = "Informe a UF.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida.") String uf) {

    /** CEP só com dígitos e UF em maiúsculas. */
    public Endereco paraModelo() {
        return new Endereco(cep.replaceAll("\\D", ""), logradouro.trim(), numero.trim(), bairro.trim(), cidade.trim(),
                uf.toUpperCase(Locale.ROOT));
    }
}
