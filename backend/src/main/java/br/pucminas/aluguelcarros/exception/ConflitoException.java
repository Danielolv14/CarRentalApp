package br.pucminas.aluguelcarros.exception;

/** Dado que precisa ser único já existe (login, CPF, CNPJ...). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
