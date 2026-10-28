package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Lista os bancos para o cliente escolher onde pedir crédito. */
public interface BancoRepository extends JpaRepository<Banco, Long> {

    List<Banco> findByAtivoTrueOrderByRazaoSocialAsc();
}
