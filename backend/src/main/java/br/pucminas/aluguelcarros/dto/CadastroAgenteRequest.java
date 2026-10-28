package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record CadastroAgenteRequest(
        @NotNull(message = "Escolha Empresa ou Banco.") TipoAgente tipo,
        @NotBlank(message = "Informe a razão social.") @Size(max = 150) String razaoSocial,
        @NotBlank(message = "Informe o CNPJ.") @CNPJ(message = "CNPJ inválido.") String cnpj,
        @NotBlank(message = "Informe o usuário.")
        @Size(min = 3, max = 60, message = "O usuário precisa ter de 3 a 60 caracteres.") String login,
        @NotBlank(message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha precisa ter de 6 a 72 caracteres.") String senha) {

    public enum TipoAgente {
        EMPRESA,
        BANCO
    }
}
