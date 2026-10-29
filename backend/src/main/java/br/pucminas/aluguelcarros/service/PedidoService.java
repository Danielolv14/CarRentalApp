package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AlteracaoPedidoRequest;
import br.pucminas.aluguelcarros.dto.BancoResponse;
import br.pucminas.aluguelcarros.dto.NovoPedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.exception.NaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.BancoRepository;
import br.pucminas.aluguelcarros.repository.ContratoCreditoRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** UC04 Introduzir pedido, UC05 Consultar pedidos, UC06 Modificar pedido, UC07 Cancelar pedido e UC08 Solicitar crédito (lado do cliente). */
@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final AutomovelRepository automoveis;
    private final BancoRepository bancos;
    private final ClienteService clientes;
    private final ContratoRepository contratos;
    private final ContratoCreditoRepository creditos;

    public PedidoService(PedidoRepository pedidos, AutomovelRepository automoveis, BancoRepository bancos, ClienteService clientes,
                         ContratoRepository contratos, ContratoCreditoRepository creditos) {
        this.pedidos = pedidos;
        this.automoveis = automoveis;
        this.bancos = bancos;
        this.clientes = clientes;
        this.contratos = contratos;
        this.creditos = creditos;
    }

    @Transactional(readOnly = true)
    public List<BancoResponse> listarBancos() {
        return bancos.findByAtivoTrueOrderByRazaoSocialAsc().stream().map(BancoResponse::de).toList();
    }

    @Transactional
    public PedidoResponse criar(String login, NovoPedidoRequest dados) {
        Cliente cliente = clientes.logado(login);
        Automovel automovel = automoveis.findById(dados.automovelId())
                .orElseThrow(() -> new NaoEncontradoException("Automóvel não encontrado."));
        exigirRetiradaValida(dados.dataRetirada(), null);

        Pedido pedido = new Pedido(cliente, automovel, dados.dataRetirada(), dados.dataDevolucao());
        if (dados.credito() != null) {
            Banco banco = bancos.findById(dados.credito().bancoId())
                    .filter(Banco::isAtivo)
                    .orElseThrow(() -> new NaoEncontradoException("Banco não encontrado."));
            pedido.solicitarCredito(banco, dados.credito().valor());
        }
        return PedidoResponse.de(pedidos.save(pedido));
    }

    /** "Meus pedidos": do mais novo para o mais antigo. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDoCliente(String login) {
        Cliente cliente = clientes.logado(login);
        return pedidos.findByClienteIdOrderByDataCriacaoDesc(cliente.getId()).stream().map(PedidoResponse::de).toList();
    }

    /** O detalhe traz também o crédito concedido pelo banco e o número do contrato, quando existirem. */
    @Transactional(readOnly = true)
    public PedidoResponse detalhar(String login, Long id) {
        Pedido pedido = doCliente(login, id);
        return PedidoResponse.de(pedido,
                pedido.isCreditoSolicitado() ? creditos.findByPedidoId(id).orElse(null) : null,
                pedido.getStatus() == StatusPedido.CONTRATADO ? contratos.findByPedidoId(id).orElse(null) : null);
    }

    /** Troca o automóvel e as datas enquanto o pedido está Enviado ou Em análise. O crédito pedido continua o mesmo. */
    @Transactional
    public PedidoResponse modificar(String login, Long id, AlteracaoPedidoRequest dados) {
        Pedido pedido = doCliente(login, id);
        Automovel automovel = automoveis.findById(dados.automovelId())
                .orElseThrow(() -> new NaoEncontradoException("Automóvel não encontrado."));
        exigirRetiradaValida(dados.dataRetirada(), pedido.getDataRetirada());
        pedido.alterar(automovel, dados.dataRetirada(), dados.dataDevolucao());
        return PedidoResponse.de(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(String login, Long id) {
        Pedido pedido = doCliente(login, id);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    /** A retirada não pode estar no passado. Ao modificar, a data que o pedido já tinha continua valendo. */
    static void exigirRetiradaValida(LocalDate retirada, LocalDate anterior) {
        if (!retirada.equals(anterior) && retirada.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de retirada não pode estar no passado.");
        }
    }

    /** O cliente só enxerga os próprios pedidos: o pedido de outro cliente responde como inexistente. */
    private Pedido doCliente(String login, Long id) {
        Cliente cliente = clientes.logado(login);
        return pedidos.findById(id)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));
    }
}
