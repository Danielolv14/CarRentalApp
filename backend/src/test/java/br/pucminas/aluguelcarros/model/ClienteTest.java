package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private final Cliente cliente = new Cliente("mariana", "hash", "Mariana Costa", "MG12345678", "12345678909",
            "Analista de sistemas", null);

    @Test
    void somaOsRendimentosNaRendaTotal() {
        cliente.adicionarRendimento(new Rendimento("Teknova Sistemas Ltda.", new BigDecimal("6200.00")));
        cliente.adicionarRendimento(new Rendimento("Estúdio Pixel", new BigDecimal("1800.00")));
        cliente.adicionarRendimento(new Rendimento("Colégio Horizonte", new BigDecimal("1200.00")));

        assertThat(cliente.calcularRendaTotal()).isEqualByComparingTo("9200.00");
        assertThat(cliente.getPerfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void naoAceitaMaisDeTresRendimentos() {
        for (int i = 0; i < Cliente.MAXIMO_RENDIMENTOS; i++) {
            cliente.adicionarRendimento(new Rendimento("Empresa " + i, BigDecimal.TEN));
        }
        assertThatThrownBy(() -> cliente.adicionarRendimento(new Rendimento("Quarta", BigDecimal.ONE)))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(cliente.getRendimentos()).hasSize(3);
    }

    @Test
    void semRendimentosARendaEhZero() {
        assertThat(cliente.calcularRendaTotal()).isEqualByComparingTo("0");
    }
}
