package ch.megiste.gboh.command.leader;

import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.army.Leader;

public class Enter extends LeaderCommand {
	public Enter() {
		super("Allows an absent leader to enter the battlefield");
	}

	@Override
	public void execute(final Leader leader) {
		console.logFormat("%s enters the battlefield",LogLeader.logLeader(leader));
		leadersHandler.enter(leader);
	}

	@Override
	public String getKey() {
		return "ENT";
	}


	@Override
	public List<String> getSynonyms() {
		return Collections.singletonList("Enter");
	}
}
