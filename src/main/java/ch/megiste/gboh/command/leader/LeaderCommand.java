package ch.megiste.gboh.command.leader;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.CommandsHolder;

public abstract class LeaderCommand extends Command {
	private CommandsHolder commandsHolder;

	LeaderCommand(final String description) {
		super(description);
	}

	public void setCommandsHolder(final CommandsHolder commandsHolder) {
		this.commandsHolder = commandsHolder;
	}

	abstract public void execute(Leader leader);
}
