package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Automovel;

import java.math.BigDecimal;

public record AutomovelResponse(Long id, String marca, String modelo, int ano, String placa, String matricula,
                                BigDecimal valorDiaria) {

    public static AutomovelResponse de(Automovel a) {
        return new AutomovelResponse(a.getId(), a.getMarca(), a.getModelo(), a.getAno(), a.getPlaca(), a.getMatricula(),
                a.getValorDiaria());
    }
}
