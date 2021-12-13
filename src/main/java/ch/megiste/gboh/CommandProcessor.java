package ch.megiste.gboh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.CommandExecutor;
import ch.megiste.gboh.command.CommandResolver;
import ch.megiste.gboh.command.GameCommandExecutor;
import ch.megiste.gboh.command.LeaderCommandExecutor;
import ch.megiste.gboh.command.UnitCommandExecutor;
import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.command.leader.LogLeader;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.Console;

public class CommandProcessor {

	private Console console;
	private GameStatus gameStatus;
	private CommandResolver commandResolver;
	private boolean explicitSave;

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

		CommandExecutor<?> executor = resolveCommandExecutor(inputs);
		if (executor == null) {
			console.logNL("Unable to understand <" + userInput + ">. Type HELP to get help on existing commands.");
			return;
		}
		executor.executeCommand();
		persistGameIfNeeded();



	}

	private CommandExecutor<? extends Command> resolveCommandExecutor(final List<String> inputs) {
		String firstPart = inputs.get(0);
		GameCommand gc = commandResolver.resolveGameCommand(firstPart);
		if (gc != null) {
			return new GameCommandExecutor(inputs, gc, console);
		}
		final String secondPart;
		if (inputs.size() == 1) {
			final FindUnitsResult foundUnits = gameStatus.findCounters(firstPart);
			if (foundUnits.foundUnits.size() > 0) {
				secondPart = Log.LOG;
			} else if (foundUnits.foundLeaders.size() > 0) {
				secondPart = LogLeader.LL;
			} else {
				return null;
			}
		} else {
			secondPart = inputs.get(1);
		}

		UnitCommand uc = commandResolver.resolveUnitCommand(secondPart);
		if (uc != null) {
			return new UnitCommandExecutor(inputs, uc, console);
		}
		LeaderCommand lc = commandResolver.resolveLeaderCommand(secondPart);
		if (lc != null) {
			return new LeaderCommandExecutor(inputs, lc, console);
		}
		return null;
	}

	public void persistGameIfNeeded() {
		if (!explicitSave) {
			gameStatus.persistGame();
		}
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
			throw new IllegalArgumentException("Unbalanced quotes in " + toProcess);
		}

		final String[] args = new String[list.size()];
		return list.toArray(args);
	}

	public void setExplicitSave(final boolean explicitSave) {
		this.explicitSave = explicitSave;
	}
}
