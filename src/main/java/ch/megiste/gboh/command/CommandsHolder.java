package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.List;

import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.unit.UnitCommand;

public class CommandsHolder {

	private final List<UnitCommand> unitCommands = new ArrayList<>();

	private final List<GameCommand> gameCommands = new ArrayList<>();

	public List<UnitCommand> getUnitCommands() {
		return unitCommands;
	}

	public List<GameCommand> getGameCommands() {
		return gameCommands;
	}
}
