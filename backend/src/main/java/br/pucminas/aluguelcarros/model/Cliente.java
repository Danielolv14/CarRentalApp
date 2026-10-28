package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pessoa física que pede o aluguel. Guarda até 3 rendimentos para a análise financeira. */
@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    public static final int MAXIMO_RENDIMENTOS = 3;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(length = 80)
    private String profissao;

    @Embedded
    private Endereco endereco;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cliente_id")
    private List<Rendimento> rendimentos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String login, String senha, String nome, String rg, String cpf, String profissao, Endereco endereco) {
        super(login, senha, nome);
        this.rg = rg;
        this.cpf = cpf;
        this.profissao = profissao;
        this.endereco = endereco;
    }

    public void adicionarRendimento(Rendimento rendimento) {
        if (rendimentos.size() >= MAXIMO_RENDIMENTOS) {
            throw new RegraNegocioException("O cliente pode ter no máximo " + MAXIMO_RENDIMENTOS + " rendimentos.");
        }
        rendimentos.add(rendimento);
    }

    public void removerRendimento(Rendimento rendimento) {
        rendimentos.remove(rendimento);
    }

    public BigDecimal calcularRendaTotal() {
        return rendimentos.stream()
                .map(Rendimento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.CLIENTE;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCpf() {
        return cpf;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<Rendimento> getRendimentos() {
        return Collections.unmodifiableList(rendimentos);
    }
}
