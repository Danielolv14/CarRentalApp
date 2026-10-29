package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByMatricula(String matricula);

    /** Na edição, a placa e a matrícula só podem repetir as do próprio automóvel. */
    boolean existsByPlacaAndIdNot(String placa, Long id);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    List<Automovel> findAllByOrderByMarcaAscModeloAsc();
}
