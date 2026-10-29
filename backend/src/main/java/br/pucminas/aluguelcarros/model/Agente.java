package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Instituição (empresa ou banco) que avalia pedidos e executa contratos. */
@Entity
@Table(name = "agentes")
public abstract class Agente extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    protected Agente() {
    }

    protected Agente(String login, String senha, String nome, String cnpj, String razaoSocial) {
        super(login, senha, nome);
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
    }

    /** Se o agente enxerga o pedido: fila, detalhe, contrato e lista de contratos. */
    public abstract boolean podeAcompanhar(Pedido pedido);

    /** Se o parecer do pedido cabe a este agente. Pedido com crédito é avaliado pelo banco escolhido pelo cliente. */
    public abstract boolean podeAvaliar(Pedido pedido);

    /** Abre o pedido para análise: de ENVIADO ele passa para EM_ANALISE. */
    public void iniciarAnalise(Pedido pedido) {
        exigirAvaliador(pedido);
        pedido.iniciarAnalise();
    }

    /** O agente responsável pela análise também pode trocar o automóvel e as datas (US08). */
    public void modificarPedido(Pedido pedido, Automovel automovel, LocalDate retirada, LocalDate devolucao) {
        exigirAvaliador(pedido);
        pedido.alterar(automovel, retirada, devolucao);
    }

    /** Dá o parecer financeiro: positivo aprova o pedido, negativo reprova. */
    public Parecer avaliarPedido(Pedido pedido, ResultadoParecer resultado, String justificativa) {
        exigirAvaliador(pedido);
        if (justificativa == null || justificativa.isBlank()) {
            throw new RegraNegocioException("A justificativa do parecer é obrigatória.");
        }
        Parecer parecer = new Parecer(pedido, this, resultado, justificativa);
        pedido.registrarParecer(parecer);
        return parecer;
    }

    /** Fecha o aluguel de um pedido aprovado e registra o automóvel no nome do proprietário escolhido. */
    public Contrato executarContrato(Pedido pedido, String numero, TipoProprietario tipoProprietario) {
        Usuario proprietario = switch (tipoProprietario) {
            case CLIENTE -> pedido.getCliente();
            case EMPRESA -> {
                if (!(this instanceof Empresa)) {
                    throw new RegraNegocioException("Só uma empresa pode registrar o automóvel em nome da empresa.");
                }
                yield this;
            }
            case BANCO -> {
                if (pedido.getBancoCredito() == null) {
                    throw new RegraNegocioException("O pedido não tem banco de crédito para ser o proprietário.");
                }
                yield pedido.getBancoCredito();
            }
        };
        pedido.marcarComoContratado();
        pedido.getAutomovel().setProprietario(proprietario);
        return new Contrato(pedido, this, numero, tipoProprietario);
    }

    private void exigirAvaliador(Pedido pedido) {
        if (!podeAvaliar(pedido)) {
            throw new RegraNegocioException(pedido.isCreditoSolicitado()
                    ? "O pedido pediu crédito ao " + pedido.getBancoCredito().getRazaoSocial() + ": o parecer é do banco."
                    : "Só uma empresa avalia pedidos sem crédito bancário.");
        }
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.AGENTE;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}
