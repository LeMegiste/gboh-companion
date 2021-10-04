package ch.megiste.gboh.util;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

public class TerminalConsole implements Console {
	private final Terminal terminal;
	private final LineReader reader;

	public TerminalConsole(final Terminal terminal, final LineReader reader) {
		this.terminal = terminal;
		this.reader = reader;
	}

	@Override
	public void logNL(final String str) {
		terminal.writer().println("\t" + str);
	}

	@Override
	public String readLine(final String prompt) {
		return reader.readLine("\t"+prompt);
	}
}
