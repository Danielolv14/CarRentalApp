package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.TipoProprietario;
import jakarta.validation.constraints.NotNull;

/** UC11 Executar contrato: em nome de quem o automóvel fica registrado (cliente, empresa ou banco). */
public record ExecucaoContratoRequest(
        @NotNull(message = "Escolha o proprietário do automóvel.") TipoProprietario tipoProprietario) {
}
