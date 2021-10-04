package ch.megiste.gboh;

/*
 * Copyright (c) 2002-2021, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.DefaultParser.Bracket;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.OSUtils;
import org.jline.widget.Widgets;

import com.google.common.base.Splitter;

import ch.megiste.gboh.command.CommandResolver;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;
import ch.megiste.gboh.util.TerminalConsole;

/**
 * Demo how to create REPL app with JLine.
 *
 * @author <a href="mailto:matti.rintanikkola@gmail.com">Matti Rinta-Nikkola</a>
 */
public class Repl {

	public static void main(String[] args) {
		try {
			//
			// Parser & Terminal
			//
			DefaultParser parser = new DefaultParser();
			parser.setEofOnUnclosedBracket(Bracket.CURLY, Bracket.ROUND, Bracket.SQUARE);
			parser.setEofOnUnclosedQuote(true);
			parser.setEscapeChars(null);
			parser.setRegexCommand("[:]{0,1}[a-zA-Z!]{1,}\\S*");    // change default regex to support shell commands
			Terminal terminal = TerminalBuilder.builder().build();
			if (terminal.getWidth() == 0 || terminal.getHeight() == 0) {
				terminal.setSize(new Size(120, 40));   // hard coded terminal size when redirecting
			}
			Thread executeThread = Thread.currentThread();
			terminal.handle(Signal.INT, signal -> executeThread.interrupt());
			//
			// Create jnanorc config file for demo
			//
			File file = new File(Repl.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String root = file.getCanonicalPath().replace("classes", "")
					.replaceAll("\\\\", "/"); // forward slashes works better also in windows!
			Path historyFile = Paths.get("history");
			//
			// ScriptEngine and command registries
			//
			//
			// LineReader
			//
			LineReader reader = LineReaderBuilder.builder().terminal(terminal)
					//                    .completer(systemRegistry.completer())
					.parser(parser)
					//                  .highlighter(highlighter)
					.history(new DefaultHistory()).variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
					.variable(LineReader.INDENTATION, 2).variable(LineReader.LIST_MAX, 100)
					.variable(LineReader.HISTORY_FILE, historyFile).option(Option.INSERT_BRACKET, true)
					.option(Option.EMPTY_WORD_OPTIONS, false)
					.option(Option.USE_FORWARD_SLASH, true)             // use forward slash in directory separator
					.option(Option.DISABLE_EVENT_EXPANSION, true).build();
			if (OSUtils.IS_WINDOWS) {
				reader.setVariable(LineReader.BLINK_MATCHING_PAREN,
						0); // if enabled cursor remains in begin parenthesis (gitbash)
			}
			//
			// complete command registries
			//
			//  myCommands.setLineReader(reader);
			//
			// widgets and console initialization
			//
			KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
			keyMap.bind(new Reference(Widgets.TAILTIP_TOGGLE), KeyMap.alt("s"));
			//
			// REPL-loop
			//


			//Register command
			Console console = new TerminalConsole(terminal, reader);
			GameStatus gs = new GameStatus();
			UnitChanger unitChanger = new UnitChanger(console, gs);
			Properties p = gs.loadProperties();

			CommandResolver resolver = new CommandResolver(console, unitChanger);

			final String currentBattle;
			if (args.length > 0) {
				currentBattle = args[0];
			} else {
				currentBattle = p.getProperty("currentBattle");
			}

			if(currentBattle!=null){
				Path battleDir = Paths.get(currentBattle);
				if (!Files.exists(battleDir) || !Files.isDirectory(battleDir)) {
					console.logNL("Invalid directory :" + battleDir.toString());
					System.exit(0);
				}

				gs.load(battleDir);
			}

			CommandProcessor cp = new CommandProcessor(console, gs, resolver);

			while (true) {
				try {
					String line = reader.readLine(gs.getPromptString() + ">>");
					line = parser.getCommand(line).startsWith("!") ? line.replaceFirst("!", "! ") : line;
					final List<String> commands = Splitter.on("&&").omitEmptyStrings().trimResults().splitToList(line);
					for (String c : commands) {
						cp.processInput(c);
					}

					if (line.startsWith("exit")) {
						System.exit(0);
					}
				} catch (UserInterruptException e) {
					// Ignore
				} catch (Exception | Error e) {
					e.printStackTrace();
				}
			}

		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		}
	}
}
