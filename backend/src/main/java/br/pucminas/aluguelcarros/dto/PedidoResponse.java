package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Parecer;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Pedido com o automóvel, o crédito pedido e os pareceres, para a lista e o detalhe. */
public record PedidoResponse(Long id, String numero, StatusPedido status, LocalDateTime dataCriacao,
                             LocalDate dataRetirada, LocalDate dataDevolucao, long diarias, BigDecimal valorEstimado,
                             boolean podeSerAlterado, AutomovelResponse automovel, CreditoResponse credito,
                             List<ParecerResponse> pareceres) {

    public static PedidoResponse de(Pedido p) {
        CreditoResponse credito = p.isCreditoSolicitado()
                ? new CreditoResponse(p.getBancoCredito().getId(), p.getBancoCredito().getRazaoSocial(), p.getValorCreditoSolicitado())
                : null;
        return new PedidoResponse(p.getId(), numero(p.getId()), p.getStatus(), p.getDataCriacao(), p.getDataRetirada(),
                p.getDataDevolucao(), p.calcularDiarias(), p.calcularValorEstimado(), p.podeSerAlterado(),
                AutomovelResponse.de(p.getAutomovel()), credito, p.getPareceres().stream().map(ParecerResponse::de).toList());
    }

    /** Número legível do pedido (ex.: PED-0042). */
    public static String numero(Long id) {
        return id == null ? null : "PED-%04d".formatted(id);
    }

    public record CreditoResponse(Long bancoId, String banco, BigDecimal valor) {
    }

    public record ParecerResponse(LocalDateTime data, ResultadoParecer resultado, String justificativa, String agente) {

        static ParecerResponse de(Parecer p) {
            return new ParecerResponse(p.getDataParecer(), p.getResultado(), p.getJustificativa(), p.getAgente().getRazaoSocial());
        }
    }
}
