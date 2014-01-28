package de.learnlib.algorithms.features.observationtable;

public class NoSuchRowException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1L;

	public NoSuchRowException() {
	}

	public NoSuchRowException(String s) {
		super(s);
	}


}
