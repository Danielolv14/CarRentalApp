package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Automóvel que pode ser alugado. O proprietário registrado (cliente, empresa ou banco)
 * é definido quando o contrato é executado.
 */
@Entity
@Table(name = "automoveis")
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Matrícula do veículo (RENAVAM). */
    @Column(nullable = false, unique = true, length = 11)
    private String matricula;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    /** Placa no padrão Mercosul, sem traço (ex.: RTA2B45). */
    @Column(nullable = false, unique = true, length = 7)
    private String placa;

    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    protected Automovel() {
    }

    public Automovel(String matricula, int ano, String marca, String modelo, String placa, BigDecimal valorDiaria) {
        this.matricula = matricula;
        this.ano = ano;
        this.marca = marca;
        this.modelo = modelo;
        this.placa = placa;
        this.valorDiaria = valorDiaria;
    }

    public Long getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public Usuario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Usuario proprietario) {
        this.proprietario = proprietario;
    }
}
