package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    Optional<Cliente> findByLogin(String login);
}
