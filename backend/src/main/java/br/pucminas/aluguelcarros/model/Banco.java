package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Agente do tipo banco: além de avaliar pedidos, concede contratos de crédito. */
@Entity
@Table(name = "bancos")
public class Banco extends Agente {

    protected Banco() {
    }

    public Banco(String login, String senha, String nome, String cnpj, String razaoSocial) {
        super(login, senha, nome, cnpj, razaoSocial);
    }

    /** O banco só enxerga os pedidos em que o cliente pediu crédito a ele. */
    @Override
    public boolean podeAcompanhar(Pedido pedido) {
        return pedido.isCreditoSolicitado() && this.equals(pedido.getBancoCredito());
    }

    /** O parecer positivo do banco vem junto com o contrato de crédito ({@link #concederCredito}). */
    @Override
    public boolean podeAvaliar(Pedido pedido) {
        return podeAcompanhar(pedido);
    }

    public ContratoCredito concederCredito(Pedido pedido, BigDecimal valor, int parcelas, BigDecimal taxaMensal) {
        if (!pedido.isCreditoSolicitado() || !this.equals(pedido.getBancoCredito())) {
            throw new RegraNegocioException("O crédito deste pedido não foi solicitado a este banco.");
        }
        return new ContratoCredito(pedido, this, valor, parcelas, taxaMensal);
    }
}
