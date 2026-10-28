package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Contrato de aluguel executado por um agente a partir de um pedido aprovado. */
@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número legível do contrato (ex.: CTR-2026-0087). */
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(name = "data_execucao", nullable = false)
    private LocalDate dataExecucao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proprietario", nullable = false, length = 10)
    private TipoProprietario tipoProprietario;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    protected Contrato() {
    }

    Contrato(Pedido pedido, Agente agente, String numero, TipoProprietario tipoProprietario) {
        this.pedido = pedido;
        this.agente = agente;
        this.numero = numero;
        this.tipoProprietario = tipoProprietario;
        this.dataExecucao = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDate getDataExecucao() {
        return dataExecucao;
    }

    public TipoProprietario getTipoProprietario() {
        return tipoProprietario;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Agente getAgente() {
        return agente;
    }
}
