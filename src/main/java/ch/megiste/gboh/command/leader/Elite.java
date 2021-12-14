package ch.megiste.gboh.command.leader;

import static ch.megiste.gboh.command.leader.LogLeader.logLeader;

import java.util.Arrays;
import java.util.List;

import ch.megiste.gboh.army.Leader;

public class Elite extends LeaderCommand {
	public Elite() {
		super("Uses the current leader in the elite commander phase");
	}

	@Override
	public String getKey() {
		return "ELI";
	}

	@Override
	public List<String> getSynonyms(){
		return Arrays.asList("Elite","ELITE");
	}

	@Override
	public void execute(final Leader leader) {
		if (gameStatus.getAllLeaders().stream().anyMatch(leader1 -> leader1.getStatus().eliteUsed)) {
			console.logNL("Elite activation already used this turn.");
			return;
		}
		if (getGameStatus().getCurrentLeader() == null) {
			console.logNL("No current leader");
		}

		if (gameStatus.getAllLeaders().stream().anyMatch(leader1 -> leader1.getNbActivations() > 0) || gameStatus
				.getAllLeaders().stream().anyMatch(leader1 -> leader1.getStatus().nbOrdersGiven > 0)) {
			console.logNL("At least one leader was already activated.");
			return;
		}

		console.logFormat("%s is using Elite activation",logLeader(leader));
		leadersHandler.markEliteActivationUsed(leader);
		gameStatus.setCurrentLeader(leader);
	}
}
