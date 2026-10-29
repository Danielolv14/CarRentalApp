package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.TipoProprietario;
import br.pucminas.aluguelcarros.model.Usuario;
import org.hibernate.Hibernate;

import java.math.BigDecimal;

/** Automóvel da frota. "proprietario" é só o tipo (null antes do primeiro contrato), para não expor o nome do dono. */
public record AutomovelResponse(Long id, String marca, String modelo, int ano, String placa, String matricula,
                                BigDecimal valorDiaria, TipoProprietario proprietario) {

    public static AutomovelResponse de(Automovel a) {
        return new AutomovelResponse(a.getId(), a.getMarca(), a.getModelo(), a.getAno(), a.getPlaca(), a.getMatricula(),
                a.getValorDiaria(), tipoDe(a.getProprietario()));
    }

    /** O proprietário vem como proxy do Hibernate (tipo Usuario): é preciso desembrulhar para saber a subclasse. */
    private static TipoProprietario tipoDe(Usuario proprietario) {
        return switch (Hibernate.unproxy(proprietario)) {
            case null -> null;
            case Banco ignored -> TipoProprietario.BANCO;
            case Empresa ignored -> TipoProprietario.EMPRESA;
            default -> TipoProprietario.CLIENTE;
        };
    }
}
