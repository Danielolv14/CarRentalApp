package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Dados que o cliente pode alterar no próprio cadastro. O CPF e o login não mudam. */
public record AtualizacaoClienteRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o RG.") @Size(max = 20, message = "RG muito longo.") String rg,
        @Size(max = 80) String profissao,
        @NotNull(message = "Informe o endereço.") @Valid EnderecoRequest endereco,
        @Size(max = Cliente.MAXIMO_RENDIMENTOS, message = "Informe no máximo 3 rendimentos.")
        List<@Valid RendimentoRequest> rendimentos) {
}
