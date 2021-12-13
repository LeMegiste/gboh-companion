package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.CommandsHolder;
import ch.megiste.gboh.game.GameStatus;

public abstract class GameCommand extends Command {

	protected GameCommand(final String description) {
		super(description);
	}

	public abstract String getKey();

	public abstract void execute(List<String> commandArgs);

	public void setCommandsHolder(final CommandsHolder commandsHolder) {
		this.commandsHolder = commandsHolder;
	}

	protected CommandsHolder commandsHolder;



}
