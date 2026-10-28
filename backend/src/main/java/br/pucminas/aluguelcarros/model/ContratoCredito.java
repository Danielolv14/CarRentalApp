package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Crédito concedido por um banco para financiar o aluguel de um pedido. */
@Entity
@Table(name = "contratos_credito")
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "numero_parcelas", nullable = false)
    private int numeroParcelas;

    /** Taxa de juros ao mês em percentual (1.90 = 1,90% a.m.). */
    @Column(name = "taxa_juros_mensal", nullable = false, precision = 6, scale = 4)
    private BigDecimal taxaJurosMensal;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id")
    private Banco banco;

    protected ContratoCredito() {
    }

    ContratoCredito(Pedido pedido, Banco banco, BigDecimal valor, int numeroParcelas, BigDecimal taxaJurosMensal) {
        if (valor == null || valor.signum() <= 0 || numeroParcelas < 1 || taxaJurosMensal == null || taxaJurosMensal.signum() < 0) {
            throw new RegraNegocioException("Valor, parcelas e taxa do crédito precisam ser positivos.");
        }
        this.pedido = pedido;
        this.banco = banco;
        this.valor = valor;
        this.numeroParcelas = numeroParcelas;
        this.taxaJurosMensal = taxaJurosMensal;
    }

    /** Parcela fixa pela tabela Price: PMT = PV · i / (1 − (1 + i)^−n). */
    public BigDecimal calcularValorParcela() {
        BigDecimal n = BigDecimal.valueOf(numeroParcelas);
        if (taxaJurosMensal.signum() == 0) {
            return valor.divide(n, 2, RoundingMode.HALF_UP);
        }
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal i = taxaJurosMensal.divide(BigDecimal.valueOf(100), mc);
        BigDecimal fator = BigDecimal.ONE.add(i).pow(numeroParcelas, mc);
        BigDecimal parcela = valor.multiply(i, mc).multiply(fator, mc).divide(fator.subtract(BigDecimal.ONE), mc);
        return parcela.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public BigDecimal getTaxaJurosMensal() {
        return taxaJurosMensal;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Banco getBanco() {
        return banco;
    }
}
