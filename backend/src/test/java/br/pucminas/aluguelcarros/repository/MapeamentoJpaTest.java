package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.model.Endereco;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.model.ResultadoParecer;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.model.TipoProprietario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Salva o fluxo inteiro (cliente → pedido → parecer → contrato + crédito) num H2 em memória. */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:aluguel;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MapeamentoJpaTest {

    @Autowired private UsuarioRepository usuarios;
    @Autowired private AutomovelRepository automoveis;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ContratoRepository contratos;
    @Autowired private ContratoCreditoRepository creditos;
    @Autowired private EntityManager em;

    @Test
    void persisteOFluxoCompletoDoAluguel() {
        Cliente mariana = new Cliente("mariana", "hash", "Mariana Costa", "MG12345678", "12345678909", "Analista de sistemas",
                new Endereco("30140071", "Avenida Afonso Pena", "1500", "Centro", "Belo Horizonte", "MG"));
        mariana.adicionarRendimento(new Rendimento("Teknova Sistemas Ltda.", new BigDecimal("6200.00")));
        Empresa locadora = new Empresa("serraverde", "hash", "Locadora Serra Verde", "12345678000190", "Locadora Serra Verde Ltda.");
        Banco banco = new Banco("horizonte", "hash", "Banco Horizonte", "98765432000110", "Banco Horizonte S.A.");
        usuarios.saveAll(List.of(mariana, locadora, banco));

        Automovel polo = automoveis.save(new Automovel("01359872046", 2024, "Volkswagen", "Polo", "RTA2B45", new BigDecimal("189.00")));

        Pedido pedido = new Pedido(mariana, polo, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 12));
        pedido.solicitarCredito(banco, new BigDecimal("1323.00"));
        pedido.iniciarAnalise();
        locadora.avaliarPedido(pedido, ResultadoParecer.POSITIVO, "Renda compatível.");
        pedidos.save(pedido);

        creditos.save(banco.concederCredito(pedido, new BigDecimal("1323.00"), 3, new BigDecimal("1.90")));
        contratos.save(locadora.executarContrato(pedido, "CTR-2026-0087", TipoProprietario.EMPRESA));
        em.flush();
        em.clear();

        Pedido salvo = pedidos.findByClienteIdOrderByDataCriacaoDesc(mariana.getId()).getFirst();
        assertThat(salvo.getStatus()).isEqualTo(StatusPedido.CONTRATADO);
        assertThat(salvo.getPareceres()).hasSize(1);
        assertThat(salvo.getBancoCredito().getId()).isEqualTo(banco.getId());

        Contrato contrato = contratos.findByPedidoId(salvo.getId()).orElseThrow();
        assertThat(contrato.getNumero()).isEqualTo("CTR-2026-0087");

        ContratoCredito credito = creditos.findByPedidoId(salvo.getId()).orElseThrow();
        assertThat(credito.calcularValorParcela()).isEqualByComparingTo("457.86");

        assertThat(automoveis.findById(polo.getId()).orElseThrow().getProprietario().getId()).isEqualTo(locadora.getId());
        assertThat(((Cliente) usuarios.findByLogin("mariana").orElseThrow()).calcularRendaTotal()).isEqualByComparingTo("6200.00");
    }
}
