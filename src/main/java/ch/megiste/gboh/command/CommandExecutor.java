package ch.megiste.gboh.command;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.util.Console;

public abstract class CommandExecutor<T extends Command> {

	protected List<String> inputs;

	protected T command;

	protected Console console;

	protected GameStatus gameStatus;

	protected CommandExecutor(final List<String> inputs, final T command, final Console console) {
		this.inputs = inputs;
		this.command = command;
		this.console = console;
		gameStatus = command.getGameStatus();
	}

	public abstract void executeCommand();
}
