package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.CadastroAgenteRequest;
import br.pucminas.aluguelcarros.dto.CadastroClienteRequest;
import br.pucminas.aluguelcarros.dto.LoginRequest;
import br.pucminas.aluguelcarros.dto.TokenResponse;
import br.pucminas.aluguelcarros.dto.UsuarioResponse;
import br.pucminas.aluguelcarros.service.AutenticacaoService;
import br.pucminas.aluguelcarros.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Login, cadastro e "quem sou eu". Só o login e o cadastro funcionam sem token. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacaoService autenticacao;
    private final CadastroService cadastro;

    public AuthController(AutenticacaoService autenticacao, CadastroService cadastro) {
        this.autenticacao = autenticacao;
        this.cadastro = cadastro;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest dados) {
        return autenticacao.login(dados);
    }

    @PostMapping("/cadastro/cliente")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse cadastrarCliente(@Valid @RequestBody CadastroClienteRequest dados) {
        return cadastro.cadastrarCliente(dados);
    }

    @PostMapping("/cadastro/agente")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse cadastrarAgente(@Valid @RequestBody CadastroAgenteRequest dados) {
        return cadastro.cadastrarAgente(dados);
    }

    @GetMapping("/me")
    public UsuarioResponse eu(@AuthenticationPrincipal Jwt token) {
        return autenticacao.usuarioAtual(token.getSubject());
    }
}
