package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.LoginRequest;
import br.pucminas.aluguelcarros.dto.TokenResponse;
import br.pucminas.aluguelcarros.dto.UsuarioResponse;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC01 Realizar login. */
@Service
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final TokenService tokens;

    public AutenticacaoService(AuthenticationManager authenticationManager, UsuarioRepository usuarios, TokenService tokens) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest pedido) {
        String login = CadastroService.normalizarLogin(pedido.login());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, pedido.senha()));
        Usuario usuario = buscar(login);
        TokenService.TokenGerado token = tokens.emitir(usuario);
        return TokenResponse.bearer(token.token(), token.expiraEm(), usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuarioAtual(String login) {
        return UsuarioResponse.de(buscar(login));
    }

    private Usuario buscar(String login) {
        return usuarios.findByLogin(login).orElseThrow(() -> new BadCredentialsException("Usuário não encontrado."));
    }
}
