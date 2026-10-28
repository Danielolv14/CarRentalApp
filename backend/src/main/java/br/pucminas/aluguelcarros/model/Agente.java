package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

    /** Dá o parecer financeiro: positivo aprova o pedido, negativo reprova. */
    public Parecer avaliarPedido(Pedido pedido, ResultadoParecer resultado, String justificativa) {
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
