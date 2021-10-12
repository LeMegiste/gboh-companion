package ch.megiste.gboh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.CommandResolver;
import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.Console;

public class CommandProcessor {

	private Console console;
	private GameStatus gameStatus;
	private CommandResolver commandResolver;

	public CommandProcessor(final Console console, final GameStatus gameStatus, final CommandResolver commandResolver) {
		this.console = console;
		this.gameStatus = gameStatus;
		this.commandResolver = commandResolver;
	}

	public void processInput(final String userInput) {
		gameStatus.nextCommand(userInput);

		List<String> inputs = Arrays.asList(translateCommandline(userInput));

		if (inputs.isEmpty()) {
			return;
		}

		String firstPart = inputs.get(0);

		GameCommand gc = commandResolver.resolveGameCommand(firstPart);
		if (gc != null) {

			final List<String> commandArgs;
			if (inputs.size() > 1) {
				commandArgs = inputs.subList(1, inputs.size() );
			} else {
				commandArgs = new ArrayList<>();
			}
			gc.execute(gameStatus, commandArgs);
			return;
		}

		//Else we are designating units
		final String unitCommand;
		if (inputs.size() > 1) {
			unitCommand = inputs.get(1);
		} else {
			unitCommand = Log.LOG;
		}

		UnitCommand uc = commandResolver.resolveUnitCommand(unitCommand);
		if (uc == null) {
			console.logNL("Unknown command: " + unitCommand);
			return;
		}

		final FindUnitsResult res = gameStatus.findUnits(firstPart);
		if (res.unknownValues.size() > 0) {
			console.logNL("Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));
			return;
		}
		final List<String> modifiers = new ArrayList<>();
		List<Unit> destinationUnits = new ArrayList<>();
		if (inputs.size() > 2) {
			final List<String> inputsAfterCommand = inputs.subList(2, inputs.size());
			Optional<String> optDestinationUnitsQuery =
					inputsAfterCommand.stream().filter(s -> !s.startsWith("-")).findFirst();
			if (optDestinationUnitsQuery.isPresent()) {
				final FindUnitsResult res2 = gameStatus.findUnits(optDestinationUnitsQuery.get());
				if (res2.unknownValues.size() > 0) {
					console.logNL("Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));
					return;
				}
				destinationUnits.addAll(res2.foundUnits);
			}

			final List<String> mods = inputsAfterCommand.stream().filter(s -> s.startsWith("-")).map(s -> s.substring(1))
					.collect(Collectors.toList());
			modifiers.addAll(mods);

		}

		uc.execute(res.foundUnits, destinationUnits, modifiers);
		uc.logAfterCommand(res.foundUnits, destinationUnits);

	}

	private static String[] translateCommandline(final String toProcess) {
		if (toProcess == null || toProcess.length() == 0) {
			// no command? no string
			return new String[0];
		}

		// parse with a simple finite state machine

		final int normal = 0;
		final int inQuote = 1;
		final int inDoubleQuote = 2;
		int state = normal;
		final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
		final ArrayList<String> list = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean lastTokenHasBeenQuoted = false;

		while (tok.hasMoreTokens()) {
			final String nextTok = tok.nextToken();
			switch (state) {
			case inQuote:
				if ("\'".equals(nextTok)) {
					lastTokenHasBeenQuoted = true;
					state = normal;
				} else {
					current.append(nextTok);
				}
				break;
			case inDoubleQuote:
				if ("\"".equals(nextTok)) {
					lastTokenHasBeenQuoted = true;
					state = normal;
				} else {
					current.append(nextTok);
				}
				break;
			default:
				if ("\'".equals(nextTok)) {
					state = inQuote;
				} else if ("\"".equals(nextTok)) {
					state = inDoubleQuote;
				} else if (" ".equals(nextTok)) {
					if (lastTokenHasBeenQuoted || current.length() != 0) {
						list.add(current.toString());
						current = new StringBuilder();
					}
				} else {
					current.append(nextTok);
				}
				lastTokenHasBeenQuoted = false;
				break;
			}
		}

		if (lastTokenHasBeenQuoted || current.length() != 0) {
			list.add(current.toString());
		}

		if (state == inQuote || state == inDoubleQuote) {
			throw new IllegalArgumentException("Unbalanced quotes in "
					+ toProcess);
		}

		final String[] args = new String[list.size()];
		return list.toArray(args);
	}
}
