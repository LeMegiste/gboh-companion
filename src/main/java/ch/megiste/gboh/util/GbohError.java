package ch.megiste.gboh.util;

public class GbohError extends RuntimeException{

	public GbohError(final Throwable cause) {
		super(cause);
	}

	public GbohError(final String msg) {
	}
}
