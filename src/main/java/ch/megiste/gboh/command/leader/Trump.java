package ch.megiste.gboh.command.leader;

import static ch.megiste.gboh.command.leader.LogLeader.logLeader;

import ch.megiste.gboh.army.Leader;

public class Trump extends LeaderCommand {
	public Trump() {
		super("Allows to trump a momentum");
	}

	@Override
	public void execute(final Leader leader) {
		final Leader trumpedLeader = gameStatus.getCurrentLeader();
		if (leader.getInitiative() <= trumpedLeader.getInitiative()) {
			console.logFormat("You must have a greater initiative that the current leader to trump.");
			return;
		}
		if (getGameStatus().getCurrentLeader() == null) {
			console.logNL("No current leader");
		}

		int roll = dice.roll();
		if (roll <= leader.getInitiative()) {

			leadersHandler.markDidTrump(leader);
			console.logFormat("Dice rolled [%d]. Trump success! %s is the next active leader. %s is finished",roll,logLeader(leader),logLeader(
					trumpedLeader));
			leadersHandler.markLeaderAsFinished(trumpedLeader);
			getGameStatus().setCurrentLeader(leader);
		} else {
			console.logFormat("Dice rolled [%d]. Trump failure! %s is finished",roll,logLeader(leader));

			leadersHandler.markLeaderAsFinished(leader);

		}

	}

	@Override
	public String getKey() {
		return "TRUMP";
	}
}
