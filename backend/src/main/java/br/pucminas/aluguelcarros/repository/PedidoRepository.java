package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** "Meus pedidos" do cliente, do mais novo para o mais antigo. */
    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    /** Fila de análise dos agentes, do mais antigo para o mais novo. */
    List<Pedido> findByStatusInOrderByDataCriacaoAsc(Collection<StatusPedido> status);
}
