package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Qualquer pessoa ou instituição que entra no sistema com login e senha.
 * Clientes e agentes herdam daqui; cada subclasse ganha a sua tabela (JOINED).
 */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    /** Hash da senha (BCrypt). Nunca a senha em texto puro. */
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, length = 120)
    private String nome;

    protected Usuario() {
    }

    protected Usuario(String login, String senha, String nome) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
    }

    public abstract Perfil getPerfil();

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Usuario usuario)) {
            return false;
        }
        // getId() em vez do campo: o outro lado pode ser um proxy do Hibernate.
        return getId() != null && getId().equals(usuario.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
