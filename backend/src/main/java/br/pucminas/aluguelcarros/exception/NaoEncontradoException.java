package br.pucminas.aluguelcarros.exception;

/** Registro que não existe (ou que não pertence a quem pediu). Vira HTTP 404. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
