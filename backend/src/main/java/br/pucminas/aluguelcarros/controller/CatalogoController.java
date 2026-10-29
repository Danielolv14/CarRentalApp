package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.BancoResponse;
import br.pucminas.aluguelcarros.service.PedidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Bancos que o cliente pode escolher para pedir crédito. Qualquer usuário logado. A frota fica no AutomovelController. */
@RestController
public class CatalogoController {

    private final PedidoService pedidos;

    public CatalogoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping("/bancos")
    public List<BancoResponse> bancos() {
        return pedidos.listarBancos();
    }
}
