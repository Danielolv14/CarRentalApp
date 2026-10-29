package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.AutomovelResponse;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.exception.NaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

/** UC13 Manter automóveis: a empresa cadastra, edita e remove a frota. Qualquer usuário logado consulta. */
@Service
public class AutomovelService {

    private final AutomovelRepository automoveis;
    private final PedidoRepository pedidos;

    public AutomovelService(AutomovelRepository automoveis, PedidoRepository pedidos) {
        this.automoveis = automoveis;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automoveis.findAllByOrderByMarcaAscModeloAsc().stream().map(AutomovelResponse::de).toList();
    }

    @Transactional
    public AutomovelResponse cadastrar(AutomovelRequest dados) {
        String placa = dados.placaNormalizada();
        exigirAnoValido(dados.ano());
        if (automoveis.existsByPlaca(placa)) {
            throw new ConflitoException("Já existe um automóvel com esta placa.");
        }
        if (automoveis.existsByMatricula(dados.matricula())) {
            throw new ConflitoException("Já existe um automóvel com esta matrícula.");
        }
        Automovel automovel = new Automovel(dados.matricula(), dados.ano(), dados.marca().trim(), dados.modelo().trim(),
                placa, dados.valorDiaria());
        return AutomovelResponse.de(automoveis.save(automovel));
    }

    /** A nova diária só vale para os próximos pedidos: cada pedido guarda a diária do dia em que foi feito. */
    @Transactional
    public AutomovelResponse atualizar(Long id, AutomovelRequest dados) {
        Automovel automovel = buscar(id);
        String placa = dados.placaNormalizada();
        exigirAnoValido(dados.ano());
        if (automoveis.existsByPlacaAndIdNot(placa, id)) {
            throw new ConflitoException("Já existe um automóvel com esta placa.");
        }
        if (automoveis.existsByMatriculaAndIdNot(dados.matricula(), id)) {
            throw new ConflitoException("Já existe um automóvel com esta matrícula.");
        }
        automovel.setMatricula(dados.matricula());
        automovel.setAno(dados.ano());
        automovel.setMarca(dados.marca().trim());
        automovel.setModelo(dados.modelo().trim());
        automovel.setPlaca(placa);
        automovel.setValorDiaria(dados.valorDiaria());
        return AutomovelResponse.de(automovel);
    }

    /** Automóvel que já aparece em pedidos (e, portanto, em contratos) fica no histórico e não pode sair. */
    @Transactional
    public void remover(Long id) {
        Automovel automovel = buscar(id);
        if (pedidos.existsByAutomovelId(id)) {
            throw new RegraNegocioException("Este automóvel já aparece em pedidos ou contratos e não pode ser removido.");
        }
        automoveis.delete(automovel);
    }

    private Automovel buscar(Long id) {
        return automoveis.findById(id).orElseThrow(() -> new NaoEncontradoException("Automóvel não encontrado."));
    }

    private static void exigirAnoValido(int ano) {
        int limite = Year.now().getValue() + 1;
        if (ano > limite) {
            throw new RegraNegocioException("O ano do automóvel pode ser no máximo " + limite + ".");
        }
    }
}
