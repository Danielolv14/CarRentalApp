package br.pucminas.aluguelcarros.exception;

/** Violação de uma regra do domínio (ex.: cancelar um pedido já contratado). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
