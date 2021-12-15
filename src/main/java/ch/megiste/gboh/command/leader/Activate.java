package ch.megiste.gboh.command.leader;

import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.command.unit.Log;

public class Activate extends LeaderCommand {
	public Activate() {
		super("The leader becomes the current active leader");
	}

	@Override
	public void execute(final Leader leader) {
		console.logFormat("%s is now active instead of %s", LogLeader.logLeader(leader),
				LogLeader.logLeader(gameStatus.getCurrentLeader()));
		leadersHandler.endOrderPhase(gameStatus.getCurrentLeader());
		gameStatus.setCurrentLeader(leader);
	}

	@Override
	public String getKey() {
		return "ACT";
	}


	@Override
	public List<String> getSynonyms() {
		return Collections.singletonList("Activate");
	}
}
