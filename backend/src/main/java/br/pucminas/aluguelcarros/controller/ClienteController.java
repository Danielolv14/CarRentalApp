package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AtualizacaoClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteResponse;
import br.pucminas.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD do cliente sobre o próprio cadastro. O "C" é o cadastro em /auth/cadastro/cliente.
 * Só o perfil CLIENTE acessa (SecurityConfig), e cada um só enxerga a própria conta: o login vem do token.
 */
@RestController
@RequestMapping("/clientes/me")
public class ClienteController {

    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public ClienteResponse consultar(@AuthenticationPrincipal Jwt token) {
        return clientes.consultar(token.getSubject());
    }

    @PutMapping
    public ClienteResponse atualizar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody AtualizacaoClienteRequest dados) {
        return clientes.atualizar(token.getSubject(), dados);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt token) {
        clientes.excluir(token.getSubject());
    }
}
