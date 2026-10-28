package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Agente do tipo empresa (locadora): avalia pedidos e mantém a frota de automóveis. */
@Entity
@Table(name = "empresas")
public class Empresa extends Agente {

    protected Empresa() {
    }

    public Empresa(String login, String senha, String nome, String cnpj, String razaoSocial) {
        super(login, senha, nome, cnpj, razaoSocial);
    }
}
