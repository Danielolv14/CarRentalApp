package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** "Meus pedidos" do cliente, do mais novo para o mais antigo. */
    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    /** Pedidos vistos pelos agentes, do mais antigo para o mais novo (a fila de análise). */
    List<Pedido> findAllByOrderByDataCriacaoAsc();

    List<Pedido> findByStatusInOrderByDataCriacaoAsc(Collection<StatusPedido> status);

    /** Automóvel que já aparece em algum pedido não pode ser removido da frota. */
    boolean existsByAutomovelId(Long automovelId);
}
