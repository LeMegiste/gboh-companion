package ch.megiste.gboh.command.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.leader.LeaderCommand;
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
			console.logNL(String.format("\t%s%s: %s",gc.getKey(), buildSynonyms(gc),gc.getDescription()));
		}

		final List<LeaderCommand> sortedLeaderCommands =  new ArrayList<>( commandsHolder.getLeaderCommands());
		Collections.sort(sortedLeaderCommands);
		console.logNL("List of leader commands");

		for(LeaderCommand c : sortedLeaderCommands){
			console.logNL(String.format("\t%s%s: %s",c.getKey(),buildSynonyms(c),c.getDescription()));
		}

		final List<UnitCommand> sortedUnitCommands =  new ArrayList<>( commandsHolder.getUnitCommands());
		Collections.sort(sortedUnitCommands);
		console.logNL("List of unit commands");

		for(UnitCommand c : sortedUnitCommands){
			console.logNL(String.format("\t%s%s: %s",c.getKey(),buildSynonyms(c),c.getDescription()));
		}
	}

	private String buildSynonyms(final Command c) {
		if(c.getSynonyms().isEmpty()){
			return "";
		} else {
			return " ("+ Joiner.on(",").join(c.getSynonyms())+")";
		}
	}
}
