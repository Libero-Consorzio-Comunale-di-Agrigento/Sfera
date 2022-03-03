package it.finmatica.atti.exceptions;

import it.finmatica.gestioneiter.exceptions.IterRuntimeException;

public class AttiRuntimeException extends IterRuntimeException {

	private static final long serialVersionUID = 1L;

	public static final String ERRORE_MODIFICA_CONCORRENTE = "Un utente ha già modificato il dato.";

	public AttiRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttiRuntimeException(String message) {
		super(message);
	}

	public AttiRuntimeException(Throwable cause) {
		super(cause);
	}

}
