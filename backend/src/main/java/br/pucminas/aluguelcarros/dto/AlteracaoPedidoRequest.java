package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** UC06 Modificar pedido (pelo cliente ou pelo agente que avalia): troca o automóvel e as datas. O crédito pedido continua o mesmo. */
public record AlteracaoPedidoRequest(
        @NotNull(message = "Escolha o automóvel.") Long automovelId,
        @NotNull(message = "Informe a data de retirada.") LocalDate dataRetirada,
        @NotNull(message = "Informe a data de devolução.") LocalDate dataDevolucao) {
}
