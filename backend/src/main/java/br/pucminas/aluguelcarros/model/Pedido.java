package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido de aluguel feito por um cliente.
 * Ciclo: ENVIADO → EM_ANALISE → APROVADO | REPROVADO → CONTRATADO; CANCELADO se o cliente desistir antes.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_retirada", nullable = false)
    private LocalDate dataRetirada;

    @Column(name = "data_devolucao", nullable = false)
    private LocalDate dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "credito_solicitado", nullable = false)
    private boolean creditoSolicitado;

    @Column(name = "valor_credito_solicitado", precision = 12, scale = 2)
    private BigDecimal valorCreditoSolicitado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "automovel_id")
    private Automovel automovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_credito_id")
    private Banco bancoCredito;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataParecer")
    private List<Parecer> pareceres = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Cliente cliente, Automovel automovel, LocalDate dataRetirada, LocalDate dataDevolucao) {
        validarPeriodo(dataRetirada, dataDevolucao);
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.ENVIADO;
    }

    public long calcularDiarias() {
        return ChronoUnit.DAYS.between(dataRetirada, dataDevolucao);
    }

    public BigDecimal calcularValorEstimado() {
        return automovel.getValorDiaria().multiply(BigDecimal.valueOf(calcularDiarias()));
    }

    public boolean podeSerAlterado() {
        return status == StatusPedido.ENVIADO || status == StatusPedido.EM_ANALISE;
    }

    public void alterar(Automovel automovel, LocalDate retirada, LocalDate devolucao) {
        exigirAlteravel();
        validarPeriodo(retirada, devolucao);
        this.automovel = automovel;
        this.dataRetirada = retirada;
        this.dataDevolucao = devolucao;
    }

    public void solicitarCredito(Banco banco, BigDecimal valor) {
        exigirAlteravel();
        if (banco == null || valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Informe o banco e um valor de crédito maior que zero.");
        }
        this.creditoSolicitado = true;
        this.bancoCredito = banco;
        this.valorCreditoSolicitado = valor;
    }

    public void iniciarAnalise() {
        if (status == StatusPedido.ENVIADO) {
            status = StatusPedido.EM_ANALISE;
        } else if (status != StatusPedido.EM_ANALISE) {
            throw new RegraNegocioException("Só pedidos enviados podem entrar em análise.");
        }
    }

    public void registrarParecer(Parecer parecer) {
        if (!podeSerAlterado()) {
            throw new RegraNegocioException("O pedido " + status + " não aceita novo parecer.");
        }
        pareceres.add(parecer);
        status = parecer.getResultado() == ResultadoParecer.POSITIVO ? StatusPedido.APROVADO : StatusPedido.REPROVADO;
    }

    public void cancelar() {
        exigirAlteravel();
        status = StatusPedido.CANCELADO;
    }

    public void marcarComoContratado() {
        if (status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Só pedidos aprovados podem virar contrato.");
        }
        status = StatusPedido.CONTRATADO;
    }

    private void exigirAlteravel() {
        if (!podeSerAlterado()) {
            throw new RegraNegocioException("Pedidos com status " + status + " não podem ser alterados.");
        }
    }

    private static void validarPeriodo(LocalDate retirada, LocalDate devolucao) {
        if (retirada == null || devolucao == null || !devolucao.isAfter(retirada)) {
            throw new RegraNegocioException("A data de devolução precisa ser posterior à de retirada.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public boolean isCreditoSolicitado() {
        return creditoSolicitado;
    }

    public BigDecimal getValorCreditoSolicitado() {
        return valorCreditoSolicitado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Automovel getAutomovel() {
        return automovel;
    }

    public Banco getBancoCredito() {
        return bancoCredito;
    }

    public List<Parecer> getPareceres() {
        return Collections.unmodifiableList(pareceres);
    }
}
