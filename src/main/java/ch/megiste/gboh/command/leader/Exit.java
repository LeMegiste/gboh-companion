package ch.megiste.gboh.command.leader;

import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.army.Leader;

public class Exit extends LeaderCommand {
	public Exit() {
		super("Allows a leader to exit the battlefield");
	}

	@Override
	public void execute(final Leader leader) {
		console.logFormat("%s exits the battlefield",LogLeader.logLeader(leader));
		leadersHandler.exit(leader);
	}

	@Override
	public String getKey() {
		return "EXI";
	}


	@Override
	public List<String> getSynonyms() {
		return Collections.singletonList("Exit");
	}
}
