package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AlteracaoPedidoRequest;
import br.pucminas.aluguelcarros.dto.NovoPedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Pedidos do cliente logado (só o perfil CLIENTE acessa). */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody NovoPedidoRequest dados) {
        return pedidos.criar(token.getSubject(), dados);
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt token) {
        return pedidos.listarDoCliente(token.getSubject());
    }

    @GetMapping("/{id}")
    public PedidoResponse detalhar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return pedidos.detalhar(token.getSubject(), id);
    }

    @PutMapping("/{id}")
    public PedidoResponse modificar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                                    @Valid @RequestBody AlteracaoPedidoRequest dados) {
        return pedidos.modificar(token.getSubject(), id, dados);
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return pedidos.cancelar(token.getSubject(), id);
    }
}
