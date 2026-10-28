package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Banco;

/** Banco que o cliente pode escolher para pedir crédito. */
public record BancoResponse(Long id, String nome) {

    public static BancoResponse de(Banco banco) {
        return new BancoResponse(banco.getId(), banco.getRazaoSocial());
    }
}
