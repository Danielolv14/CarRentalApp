package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AutomovelResponse;
import br.pucminas.aluguelcarros.dto.BancoResponse;
import br.pucminas.aluguelcarros.service.PedidoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listas usadas para montar o pedido: automóveis disponíveis e bancos para o crédito. Qualquer usuário logado. */
@RestController
public class CatalogoController {

    private final PedidoService pedidos;

    public CatalogoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping("/automoveis")
    public List<AutomovelResponse> automoveis() {
        return pedidos.listarAutomoveis();
    }

    @GetMapping("/bancos")
    public List<BancoResponse> bancos() {
        return pedidos.listarBancos();
    }
}
