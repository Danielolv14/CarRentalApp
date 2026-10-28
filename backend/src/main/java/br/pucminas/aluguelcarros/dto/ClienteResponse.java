package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Endereco;
import br.pucminas.aluguelcarros.model.Rendimento;

import java.math.BigDecimal;
import java.util.List;

/** Cadastro completo do cliente logado (tela Meu perfil). */
public record ClienteResponse(Long id, String login, String nome, String cpf, String rg, String profissao,
                              EnderecoResponse endereco, List<RendimentoResponse> rendimentos, BigDecimal rendaTotal) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getLogin(), cliente.getNome(), cliente.getCpf(), cliente.getRg(),
                cliente.getProfissao(), EnderecoResponse.de(cliente.getEndereco()),
                cliente.getRendimentos().stream().map(RendimentoResponse::de).toList(), cliente.calcularRendaTotal());
    }

    public record EnderecoResponse(String cep, String logradouro, String numero, String bairro, String cidade, String uf) {

        static EnderecoResponse de(Endereco e) {
            return e == null ? null
                    : new EnderecoResponse(e.getCep(), e.getLogradouro(), e.getNumero(), e.getBairro(), e.getCidade(), e.getUf());
        }
    }

    public record RendimentoResponse(Long id, String entidadeEmpregadora, BigDecimal valor) {

        static RendimentoResponse de(Rendimento r) {
            return new RendimentoResponse(r.getId(), r.getEntidadeEmpregadora(), r.getValor());
        }
    }
}
