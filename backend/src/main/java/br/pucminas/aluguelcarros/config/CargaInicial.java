package br.pucminas.aluguelcarros.config;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Frota inicial para o protótipo: se ainda não houver automóveis, cadastra os 6 dos wireframes.
 * Desligue com app.carga-inicial=false (os testes já desligam).
 */
@Component
@ConditionalOnProperty(name = "app.carga-inicial", havingValue = "true", matchIfMissing = true)
public class CargaInicial implements ApplicationRunner {

    private final AutomovelRepository automoveis;

    public CargaInicial(AutomovelRepository automoveis) {
        this.automoveis = automoveis;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (automoveis.count() > 0) {
            return;
        }
        automoveis.saveAll(List.of(
                new Automovel("01284736519", 2023, "Fiat", "Argo", "QPX4E21", new BigDecimal("159.00")),
                new Automovel("01359872046", 2024, "Volkswagen", "Polo", "RTA2B45", new BigDecimal("189.00")),
                new Automovel("01362590418", 2024, "Chevrolet", "Onix", "SHB7C09", new BigDecimal("185.00")),
                new Automovel("01297413385", 2023, "Hyundai", "HB20", "PUV3D18", new BigDecimal("177.00")),
                new Automovel("01245068972", 2022, "Jeep", "Renegade", "QNE8A62", new BigDecimal("247.00")),
                new Automovel("01371854620", 2024, "Toyota", "Corolla", "RFZ1G37", new BigDecimal("299.00"))));
    }
}
