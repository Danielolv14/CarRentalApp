package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.Perfil;
import br.pucminas.aluguelcarros.model.Usuario;

/** Dados públicos do usuário logado. tipoAgente é EMPRESA ou BANCO (null para clientes). */
public record UsuarioResponse(Long id, String login, String nome, Perfil perfil, String tipoAgente) {

    public static UsuarioResponse de(Usuario usuario) {
        String tipoAgente = switch (usuario) {
            case Banco ignored -> "BANCO";
            case Empresa ignored -> "EMPRESA";
            default -> null;
        };
        return new UsuarioResponse(usuario.getId(), usuario.getLogin(), usuario.getNome(), usuario.getPerfil(), tipoAgente);
    }
}
