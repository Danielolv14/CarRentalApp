package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;

/**
 * Pedido visto pelo agente: os dados do pedido, o cadastro do cliente (com a renda total) e o que o agente pode fazer.
 * podeAvaliar vale para o parecer e para modificar o pedido; podeExecutarContrato, para pedidos aprovados.
 */
public record PedidoAgenteResponse(PedidoResponse pedido, ClienteResponse cliente, boolean podeAvaliar,
                                   boolean podeExecutarContrato) {

    public static PedidoAgenteResponse de(PedidoResponse pedido, Pedido p, Agente agente) {
        return new PedidoAgenteResponse(pedido, ClienteResponse.de(p.getCliente()),
                p.podeSerAlterado() && agente.podeAvaliar(p), p.getStatus() == StatusPedido.APROVADO);
    }
}
