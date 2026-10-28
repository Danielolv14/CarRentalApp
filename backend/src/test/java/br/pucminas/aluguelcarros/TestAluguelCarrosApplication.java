package br.pucminas.aluguelcarros;

import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com banco H2 em memória, sem precisar do Supabase (para desenvolvimento e demonstração).
 * Uso: ./mvnw spring-boot:test-run -Dspring-boot.run.profiles=h2
 * Os dados somem quando a API para.
 */
public class TestAluguelCarrosApplication {

    public static void main(String[] args) {
        SpringApplication.from(AluguelCarrosApplication::main).run(args);
    }
}
