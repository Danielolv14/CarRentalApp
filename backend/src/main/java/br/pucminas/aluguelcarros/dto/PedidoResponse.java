package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.Parecer;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.model.TipoProprietario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pedido com o automóvel, o crédito e os pareceres, para a lista e o detalhe.
 * No detalhe vêm também o crédito concedido pelo banco e o contrato, quando existirem.
 */
public record PedidoResponse(Long id, String numero, StatusPedido status, LocalDateTime dataCriacao,
                             LocalDate dataRetirada, LocalDate dataDevolucao, long diarias, BigDecimal valorDiaria,
                             BigDecimal valorEstimado, boolean podeSerAlterado, AutomovelResponse automovel,
                             CreditoResponse credito, List<ParecerResponse> pareceres, ContratoResumo contrato) {

    public static PedidoResponse de(Pedido p) {
        return de(p, null, null);
    }

    public static PedidoResponse de(Pedido p, ContratoCredito concedido, Contrato contrato) {
        CreditoResponse credito = p.isCreditoSolicitado()
                ? new CreditoResponse(p.getBancoCredito().getId(), p.getBancoCredito().getRazaoSocial(),
                p.getValorCreditoSolicitado(), CreditoConcedidoResponse.de(concedido))
                : null;
        return new PedidoResponse(p.getId(), numero(p.getId()), p.getStatus(), p.getDataCriacao(), p.getDataRetirada(),
                p.getDataDevolucao(), p.calcularDiarias(), p.getValorDiaria(), p.calcularValorEstimado(), p.podeSerAlterado(),
                AutomovelResponse.de(p.getAutomovel()), credito, p.getPareceres().stream().map(ParecerResponse::de).toList(),
                ContratoResumo.de(contrato));
    }

    /** Número legível do pedido (ex.: PED-0042). */
    public static String numero(Long id) {
        return id == null ? null : "PED-%04d".formatted(id);
    }

    /** Crédito pedido pelo cliente. "concedido" fica null até o banco dar o parecer positivo. */
    public record CreditoResponse(Long bancoId, String banco, BigDecimal valor, CreditoConcedidoResponse concedido) {
    }

    public record ParecerResponse(LocalDateTime data, ResultadoParecer resultado, String justificativa, String agente) {

        static ParecerResponse de(Parecer p) {
            return new ParecerResponse(p.getDataParecer(), p.getResultado(), p.getJustificativa(), p.getAgente().getRazaoSocial());
        }
    }

    public record ContratoResumo(String numero, LocalDate dataExecucao, TipoProprietario tipoProprietario) {

        static ContratoResumo de(Contrato c) {
            return c == null ? null : new ContratoResumo(c.getNumero(), c.getDataExecucao(), c.getTipoProprietario());
        }
    }
}
