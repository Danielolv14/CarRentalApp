package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.TipoProprietario;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Contrato de aluguel para a lista de contratos (UC12), com o crédito associado quando houver. */
public record ContratoResponse(Long id, String numero, LocalDate dataExecucao, TipoProprietario tipoProprietario,
                               String proprietario, String agente, Long pedidoId, String pedido, String cliente,
                               AutomovelResponse automovel, LocalDate dataRetirada, LocalDate dataDevolucao,
                               BigDecimal valor, CreditoConcedidoResponse credito) {

    public static ContratoResponse de(Contrato c, ContratoCredito credito) {
        Pedido p = c.getPedido();
        String proprietario = switch (c.getTipoProprietario()) {
            case CLIENTE -> p.getCliente().getNome();
            case EMPRESA -> c.getAgente().getRazaoSocial();
            case BANCO -> p.getBancoCredito().getRazaoSocial();
        };
        return new ContratoResponse(c.getId(), c.getNumero(), c.getDataExecucao(), c.getTipoProprietario(), proprietario,
                c.getAgente().getRazaoSocial(), p.getId(), PedidoResponse.numero(p.getId()), p.getCliente().getNome(),
                AutomovelResponse.de(p.getAutomovel()), p.getDataRetirada(), p.getDataDevolucao(), p.calcularValorEstimado(),
                CreditoConcedidoResponse.de(credito));
    }
}
