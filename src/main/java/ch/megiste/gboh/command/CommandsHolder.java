package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.command.unit.UnitCommand;

public class CommandsHolder {

	private final List<UnitCommand> unitCommands = new ArrayList<>();

	private final List<GameCommand> gameCommands = new ArrayList<>();

	private final List<LeaderCommand> leaderCommands = new ArrayList<>();

	public List<UnitCommand> getUnitCommands() {
		return unitCommands;
	}

	public List<GameCommand> getGameCommands() {
		return gameCommands;
	}

	public List<LeaderCommand> getLeaderCommands() {
		return leaderCommands;
	}

	public List<Command> getAllCommands() {
		List<Command> all = new ArrayList<>();
		all.addAll(unitCommands);
		all.addAll(gameCommands);
		all.addAll(leaderCommands);

		return all;
	}
}
