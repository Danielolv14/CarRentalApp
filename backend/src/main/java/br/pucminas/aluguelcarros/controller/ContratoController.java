package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.dto.ExecucaoContratoRequest;
import br.pucminas.aluguelcarros.service.ContratoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Contratos de aluguel: o agente executa o contrato de um pedido aprovado e consulta os contratos (perfil AGENTE). */
@RestController
public class ContratoController {

    private final ContratoService contratos;

    public ContratoController(ContratoService contratos) {
        this.contratos = contratos;
    }

    @PostMapping("/agente/pedidos/{id}/contrato")
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse executar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                                     @Valid @RequestBody ExecucaoContratoRequest dados) {
        return contratos.executar(token.getSubject(), id, dados);
    }

    @GetMapping("/agente/contratos")
    public List<ContratoResponse> listar(@AuthenticationPrincipal Jwt token) {
        return contratos.listar(token.getSubject());
    }
}
