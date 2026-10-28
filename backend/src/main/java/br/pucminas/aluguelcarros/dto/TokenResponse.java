package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Usuario;

import java.time.Instant;

/** Resposta do login e do cadastro: o token para o cabeçalho Authorization e quem está logado. */
public record TokenResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {

    public static TokenResponse bearer(String token, Instant expiraEm, Usuario usuario) {
        return new TokenResponse(token, "Bearer", expiraEm, UsuarioResponse.de(usuario));
    }
}
