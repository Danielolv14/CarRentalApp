package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    Optional<Contrato> findByPedidoId(Long pedidoId);

    /** Lista de contratos: do mais novo para o mais antigo. */
    List<Contrato> findAllByOrderByDataExecucaoDescIdDesc();

    /** Quantos contratos o ano já tem, para numerar o próximo (CTR-2026-0001, CTR-2026-0002...). */
    long countByNumeroStartingWith(String prefixo);
}
