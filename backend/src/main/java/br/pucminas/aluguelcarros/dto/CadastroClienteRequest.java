package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.util.List;

public record CadastroClienteRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o CPF.") @CPF(message = "CPF inválido.") String cpf,
        @NotBlank(message = "Informe o RG.") @Size(max = 20, message = "RG muito longo.") String rg,
        @Size(max = 80) String profissao,
        @NotNull(message = "Informe o endereço.") @Valid EnderecoRequest endereco,
        @Size(max = Cliente.MAXIMO_RENDIMENTOS, message = "Informe no máximo 3 rendimentos.")
        List<@Valid RendimentoRequest> rendimentos,
        @NotBlank(message = "Informe o usuário.")
        @Size(min = 3, max = 60, message = "O usuário precisa ter de 3 a 60 caracteres.") String login,
        @NotBlank(message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha precisa ter de 6 a 72 caracteres.") String senha) {
}
