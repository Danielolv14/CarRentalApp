package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AlteracaoPedidoRequest;
import br.pucminas.aluguelcarros.dto.ParecerRequest;
import br.pucminas.aluguelcarros.dto.PedidoAgenteResponse;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Pedidos vistos pelo agente (só o perfil AGENTE acessa): fila, análise, parecer e modificação.
 * A empresa vê todos os pedidos; o banco, só os que pediram crédito a ele.
 */
@RestController
@RequestMapping("/agente/pedidos")
public class AvaliacaoController {

    private final AvaliacaoService avaliacoes;

    public AvaliacaoController(AvaliacaoService avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    /** Fila de análise: GET /agente/pedidos?status=ENVIADO&status=EM_ANALISE. Sem status, vêm todos. */
    @GetMapping
    public List<PedidoAgenteResponse> listar(@AuthenticationPrincipal Jwt token,
                                             @RequestParam(required = false) List<StatusPedido> status) {
        return avaliacoes.listar(token.getSubject(), status);
    }

    @GetMapping("/{id}")
    public PedidoAgenteResponse detalhar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return avaliacoes.detalhar(token.getSubject(), id);
    }

    @PostMapping("/{id}/analise")
    public PedidoAgenteResponse iniciarAnalise(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return avaliacoes.iniciarAnalise(token.getSubject(), id);
    }

    @PostMapping("/{id}/parecer")
    public PedidoAgenteResponse avaliar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                                        @Valid @RequestBody ParecerRequest dados) {
        return avaliacoes.avaliar(token.getSubject(), id, dados);
    }

    @PutMapping("/{id}")
    public PedidoAgenteResponse modificar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                                          @Valid @RequestBody AlteracaoPedidoRequest dados) {
        return avaliacoes.modificar(token.getSubject(), id, dados);
    }
}
