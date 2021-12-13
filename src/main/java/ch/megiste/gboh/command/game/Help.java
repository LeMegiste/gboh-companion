package ch.megiste.gboh.command.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.command.unit.UnitCommand;

public class Help extends GameCommand {
	public Help() {
		super("Get some help. Usage: HELP <some specific command>");
	}

	@Override
	public String getKey() {
		return "HELP";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		console.logNL("List of generic game commands");
		final List<GameCommand> sortedGameCommands =  new ArrayList<>( commandsHolder.getGameCommands());
		Collections.sort(sortedGameCommands);
		for(GameCommand gc : sortedGameCommands){
			console.logNL(String.format("\t%s : %s",gc.getKey(),gc.getDescription()));
		}

		final List<UnitCommand> sortedUnitCommands =  new ArrayList<>( commandsHolder.getUnitCommands());
		Collections.sort(sortedUnitCommands);
		console.logNL("List of unit commands");

		for(UnitCommand uc : sortedUnitCommands){
			console.logNL(String.format("\t%s : %s",uc.getKey(),uc.getDescription()));
		}
	}
}
