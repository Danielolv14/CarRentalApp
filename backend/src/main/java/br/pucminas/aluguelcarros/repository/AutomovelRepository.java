package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByMatricula(String matricula);
}
