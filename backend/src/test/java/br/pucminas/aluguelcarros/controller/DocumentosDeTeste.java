package br.pucminas.aluguelcarros.controller;

import java.util.concurrent.atomic.AtomicInteger;

/** CPFs e CNPJs válidos e diferentes a cada chamada, compartilhados pelos testes de API (o banco H2 é o mesmo). */
final class DocumentosDeTeste {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(100_000_000);

    private DocumentosDeTeste() {
    }

    static String cpfValido() {
        String base = String.valueOf(SEQUENCIA.incrementAndGet());
        int d1 = digito(base, new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2});
        int d2 = digito(base + d1, new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
        return base + d1 + d2;
    }

    static String cnpjValido() {
        String base = String.format("%08d", SEQUENCIA.incrementAndGet() % 100_000_000) + "0001";
        int d1 = digito(base, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int d2 = digito(base + d1, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return base + d1 + d2;
    }

    /** Lista JSON com N rendimentos de R$ 1.500,50. */
    static String rendimentos(int quantidade) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < quantidade; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"entidadeEmpregadora\":\"Empresa ").append(i + 1).append("\",\"valor\":1500.50}");
        }
        return json.append(']').toString();
    }

    private static int digito(String numeros, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (numeros.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
