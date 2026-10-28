package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.Usuario;

import java.util.List;

/** Papéis de acesso de cada tipo de usuário: CLIENTE, ou AGENTE + EMPRESA/BANCO. */
public final class Papeis {

    private Papeis() {
    }

    public static List<String> de(Usuario usuario) {
        return switch (usuario) {
            case Banco ignored -> List.of("AGENTE", "BANCO");
            case Empresa ignored -> List.of("AGENTE", "EMPRESA");
            default -> List.of(usuario.getPerfil().name());
        };
    }
}
