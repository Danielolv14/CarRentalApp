package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);
}
