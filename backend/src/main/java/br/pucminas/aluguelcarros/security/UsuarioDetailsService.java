package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Busca o usuário pelo login para o Spring Security conferir a senha (BCrypt). */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        Usuario usuario = usuarios.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenha())
                .roles(Papeis.de(usuario).toArray(String[]::new))
                .disabled(!usuario.isAtivo())
                .build();
    }
}
