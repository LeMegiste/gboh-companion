package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.List;

import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.util.Console;

public class GameCommandExecutor extends CommandExecutor<GameCommand> {
	public GameCommandExecutor(final List<String> inputs, final GameCommand command, final Console console) {
		super(inputs, command, console);
	}

	@Override
	public void executeCommand() {
		final List<String> commandArgs;
		if (inputs.size() > 1) {
			commandArgs = inputs.subList(1, inputs.size());
		} else {
			commandArgs = new ArrayList<>();
		}
		command.execute(commandArgs);
	}
}
