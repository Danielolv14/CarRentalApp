package br.pucminas.aluguelcarros.dto;

import java.util.Map;

/** Corpo padrão de erro. "campos" traz a mensagem de cada campo inválido (ou null). */
public record ErroResponse(String mensagem, Map<String, String> campos) {

    public static ErroResponse de(String mensagem) {
        return new ErroResponse(mensagem, null);
    }
}
