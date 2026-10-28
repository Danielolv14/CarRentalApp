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
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Avaliação financeira de um pedido feita por um agente. Criado por {@link Agente#avaliarPedido}. */
@Entity
@Table(name = "pareceres")
public class Parecer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_parecer", nullable = false)
    private LocalDateTime dataParecer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResultadoParecer resultado;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private Agente agente;

    protected Parecer() {
    }

    Parecer(Pedido pedido, Agente agente, ResultadoParecer resultado, String justificativa) {
        this.pedido = pedido;
        this.agente = agente;
        this.resultado = resultado;
        this.justificativa = justificativa;
        this.dataParecer = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataParecer() {
        return dataParecer;
    }

    public ResultadoParecer getResultado() {
        return resultado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Agente getAgente() {
        return agente;
    }
}
