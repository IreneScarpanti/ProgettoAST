package com.scarpanti.app.playqueue.transaction.exception;

public class TransactionFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransactionFailedException(String message, Exception cause) {
		super(message, cause);
	}
}
