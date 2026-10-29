package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.dto.ExecucaoContratoRequest;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.repository.ContratoCreditoRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

/** UC11 Executar contrato e UC12 Consultar contratos. */
@Service
public class ContratoService {

    private final AvaliacaoService avaliacoes;
    private final ContratoRepository contratos;
    private final ContratoCreditoRepository creditos;

    public ContratoService(AvaliacaoService avaliacoes, ContratoRepository contratos, ContratoCreditoRepository creditos) {
        this.avaliacoes = avaliacoes;
        this.contratos = contratos;
        this.creditos = creditos;
    }

    /** O pedido aprovado vira contrato; o crédito concedido pelo banco fica associado pelo pedido. */
    @Transactional
    public ContratoResponse executar(String login, Long pedidoId, ExecucaoContratoRequest dados) {
        Agente agente = avaliacoes.logado(login);
        Pedido pedido = avaliacoes.acompanhado(agente, pedidoId);
        Contrato contrato = agente.executarContrato(pedido, proximoNumero(), dados.tipoProprietario());
        return resposta(contratos.save(contrato));
    }

    /** Contratos dos pedidos que o agente acompanha, do mais novo para o mais antigo. */
    @Transactional(readOnly = true)
    public List<ContratoResponse> listar(String login) {
        Agente agente = avaliacoes.logado(login);
        return contratos.findAllByOrderByDataExecucaoDescIdDesc().stream()
                .filter(c -> agente.podeAcompanhar(c.getPedido()))
                .map(this::resposta)
                .toList();
    }

    /** CTR-AAAA-NNNN, numerado dentro do ano (ex.: CTR-2026-0087). */
    private String proximoNumero() {
        String prefixo = "CTR-%d-".formatted(Year.now().getValue());
        return prefixo + "%04d".formatted(contratos.countByNumeroStartingWith(prefixo) + 1);
    }

    private ContratoResponse resposta(Contrato c) {
        return ContratoResponse.de(c, creditos.findByPedidoId(c.getPedido().getId()).orElse(null));
    }
}
