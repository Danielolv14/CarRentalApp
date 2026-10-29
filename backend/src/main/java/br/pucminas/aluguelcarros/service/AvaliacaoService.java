package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AlteracaoPedidoRequest;
import br.pucminas.aluguelcarros.dto.ParecerRequest;
import br.pucminas.aluguelcarros.dto.PedidoAgenteResponse;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.exception.NaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ContratoCreditoRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lado do agente: fila de análise, UC09 Avaliar pedido, UC10 Conceder crédito (banco) e UC06 Modificar pedido.
 * Quem vê e quem avalia cada pedido é decidido pelo Agente (Empresa ou Banco), no model.
 */
@Service
public class AvaliacaoService {

    private final AgenteRepository agentes;
    private final PedidoRepository pedidos;
    private final AutomovelRepository automoveis;
    private final ContratoRepository contratos;
    private final ContratoCreditoRepository creditos;

    public AvaliacaoService(AgenteRepository agentes, PedidoRepository pedidos, AutomovelRepository automoveis,
                            ContratoRepository contratos, ContratoCreditoRepository creditos) {
        this.agentes = agentes;
        this.pedidos = pedidos;
        this.automoveis = automoveis;
        this.contratos = contratos;
        this.creditos = creditos;
    }

    /** Pedidos que o agente acompanha, do mais antigo para o mais novo. Sem status, vêm todos. */
    @Transactional(readOnly = true)
    public List<PedidoAgenteResponse> listar(String login, List<StatusPedido> status) {
        Agente agente = logado(login);
        List<Pedido> lista = status == null || status.isEmpty()
                ? pedidos.findAllByOrderByDataCriacaoAsc()
                : pedidos.findByStatusInOrderByDataCriacaoAsc(status);
        return lista.stream().filter(agente::podeAcompanhar).map(p -> resposta(agente, p)).toList();
    }

    @Transactional(readOnly = true)
    public PedidoAgenteResponse detalhar(String login, Long id) {
        Agente agente = logado(login);
        return resposta(agente, acompanhado(agente, id));
    }

    /** Ao abrir o pedido para avaliar, ele passa de ENVIADO para EM_ANALISE (US11). */
    @Transactional
    public PedidoAgenteResponse iniciarAnalise(String login, Long id) {
        Agente agente = logado(login);
        Pedido pedido = acompanhado(agente, id);
        agente.iniciarAnalise(pedido);
        return resposta(agente, pedido);
    }

    /** Parecer do agente. O positivo de um banco já concede o crédito com as condições informadas. */
    @Transactional
    public PedidoAgenteResponse avaliar(String login, Long id, ParecerRequest dados) {
        Agente agente = logado(login);
        Pedido pedido = acompanhado(agente, id);
        agente.avaliarPedido(pedido, dados.resultado(), dados.justificativa().trim());

        if (agente instanceof Banco banco && dados.resultado() == ResultadoParecer.POSITIVO) {
            ParecerRequest.CondicoesCredito credito = dados.credito();
            if (credito == null) {
                throw new RegraNegocioException("Informe as condições do crédito: valor, parcelas e taxa ao mês.");
            }
            creditos.save(banco.concederCredito(pedido, credito.valor(), credito.parcelas(), credito.taxaMensal()));
        }
        return resposta(agente, pedido);
    }

    @Transactional
    public PedidoAgenteResponse modificar(String login, Long id, AlteracaoPedidoRequest dados) {
        Agente agente = logado(login);
        Pedido pedido = acompanhado(agente, id);
        Automovel automovel = automoveis.findById(dados.automovelId())
                .orElseThrow(() -> new NaoEncontradoException("Automóvel não encontrado."));
        if (!dados.dataRetirada().equals(pedido.getDataRetirada()) && dados.dataRetirada().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de retirada não pode estar no passado.");
        }
        agente.modificarPedido(pedido, automovel, dados.dataRetirada(), dados.dataDevolucao());
        return resposta(agente, pedido);
    }

    /** Agente ativo dono do token; se não existir mais, a sessão é inválida (401). */
    Agente logado(String login) {
        return agentes.findByLogin(login)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new BadCredentialsException("Agente não encontrado."));
    }

    /** Pedido que o agente não acompanha (ex.: pedido sem crédito, para um banco) responde como inexistente. */
    Pedido acompanhado(Agente agente, Long id) {
        return pedidos.findById(id)
                .filter(agente::podeAcompanhar)
                .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));
    }

    private PedidoAgenteResponse resposta(Agente agente, Pedido p) {
        ContratoCredito concedido = p.isCreditoSolicitado() ? creditos.findByPedidoId(p.getId()).orElse(null) : null;
        Contrato contrato = p.getStatus() == StatusPedido.CONTRATADO ? contratos.findByPedidoId(p.getId()).orElse(null) : null;
        return PedidoAgenteResponse.de(PedidoResponse.de(p, concedido, contrato), p, agente);
    }
}
