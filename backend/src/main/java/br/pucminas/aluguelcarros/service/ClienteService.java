package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AtualizacaoClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteResponse;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** UC03 Manter dados cadastrais: o cliente consulta, altera e exclui o próprio cadastro. */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final PedidoRepository pedidos;

    public ClienteService(ClienteRepository clientes, PedidoRepository pedidos) {
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public ClienteResponse consultar(String login) {
        return ClienteResponse.de(logado(login));
    }

    /** Troca os dados e a lista de rendimentos inteira (o limite de 3 é conferido pelo Cliente). */
    @Transactional
    public ClienteResponse atualizar(String login, AtualizacaoClienteRequest dados) {
        Cliente cliente = logado(login);
        cliente.setNome(dados.nome().trim());
        cliente.setRg(dados.rg().trim());
        cliente.setProfissao(CadastroService.textoOuNulo(dados.profissao()));
        cliente.setEndereco(dados.endereco().paraModelo());

        List.copyOf(cliente.getRendimentos()).forEach(cliente::removerRendimento);
        if (dados.rendimentos() != null) {
            dados.rendimentos().forEach(r ->
                    cliente.adicionarRendimento(new Rendimento(r.entidadeEmpregadora().trim(), r.valor())));
        }
        return ClienteResponse.de(clientes.saveAndFlush(cliente));
    }

    /**
     * Sem pedidos, o cadastro é apagado. Com histórico, os pedidos ainda em análise são cancelados
     * e a conta é desativada, para os contratos já executados continuarem registrados (US05).
     */
    @Transactional
    public void excluir(String login) {
        Cliente cliente = logado(login);
        List<Pedido> historico = pedidos.findByClienteIdOrderByDataCriacaoDesc(cliente.getId());
        if (historico.isEmpty()) {
            clientes.delete(cliente);
            return;
        }
        historico.stream().filter(Pedido::podeSerAlterado).forEach(Pedido::cancelar);
        cliente.desativar();
    }

    private Cliente logado(String login) {
        return clientes.findByLogin(login)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new BadCredentialsException("Cliente não encontrado."));
    }
}
