package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Agente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenteRepository extends JpaRepository<Agente, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Agente> findByLogin(String login);
}
