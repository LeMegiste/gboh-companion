package ch.megiste.gboh.util;

import org.apache.commons.lang3.ArrayUtils;

public class CommandLineParsingException extends Exception {
	public CommandLineParsingException(final String message) {
		super(message);
	}

	public CommandLineParsingException(final String key, Object v1, Object... var) {
		super(String.format(key, ArrayUtils.addAll(new Object[]{v1},var)));
	}
}
