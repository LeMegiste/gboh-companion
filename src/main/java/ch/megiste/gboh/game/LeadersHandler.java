package ch.megiste.gboh.game;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.army.LeaderStatus;
import ch.megiste.gboh.command.leader.LogLeader;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;

public class LeadersHandler {

	private final Console console;
	private GameStatus gameStatus;

	public LeadersHandler(final Console console, final GameStatus gameStatus) {
		this.gameStatus = gameStatus;
		this.console = console;
	}

	public void markDidTrump(final Leader leader) {
		final LeaderStatusChange sc = new LeaderStatusChange();
		sc.didTrump=true;
		changeLeaderInternal(leader,sc);
	}

	public void enter(final Leader leader) {
		final LeaderStatusChange lsc = new LeaderStatusChange();
		lsc.present=true;
		changeLeaderInternal(leader,lsc);
	}
	public void exit(final Leader leader) {
		final LeaderStatusChange lsc = new LeaderStatusChange();
		lsc.present=false;
		changeLeaderInternal(leader,lsc);
	}

	private static class LeaderStatusChange {
		Boolean present;
		Integer nbActivations;
		Boolean finished;
		Integer nbOrdersGiven;
		Boolean eliteUsed;
		Boolean didTrump;
	}

	private void changeLeaderInternal(final Leader leader, LeaderStatusChange lsc) {
		final LeaderStatus before = Helper.clone(leader.getStatus());
		if (lsc.nbActivations != null) {
			leader.getStatus().nbActivations = lsc.nbActivations;
		}

		if (lsc.finished != null) {

			if (lsc.finished) {
				console.logFormat("%s is FINISHED", LogLeader.logLeader(leader));
			}
			leader.getStatus().finished = lsc.finished;

		}
		if (lsc.nbOrdersGiven != null) {
			leader.getStatus().nbOrdersGiven = lsc.nbOrdersGiven;
		}
		if (lsc.eliteUsed != null) {
			leader.getStatus().eliteUsed = lsc.eliteUsed;
		}
		if (lsc.didTrump != null) {
			leader.getStatus().didTrump = lsc.didTrump;
		}
		if (lsc.present != null) {
			leader.getStatus().present = lsc.present;
		}
		gameStatus.recordChange(before, leader);
	}

	public void markLeaderAsFinished(final Leader leader) {
		final LeaderStatusChange sc = new LeaderStatusChange();
		sc.finished=true;
		changeLeaderInternal(leader, sc);
	}

	public void endOrderPhase(final Leader leader) {
		int newNbActivations = leader.getStatus().nbActivations + 1;
		Boolean finished = null;
		if (newNbActivations >= 3) {
			finished = true;
		}
		final LeaderStatusChange sc = new LeaderStatusChange();
		sc.finished=finished;
		sc.nbActivations=newNbActivations;

		changeLeaderInternal(leader, sc);
	}

	public void reactivateLeader(final Leader l) {
		if (l.isFinished()) {
			final LeaderStatusChange sc = new LeaderStatusChange();
			sc.nbActivations=2;
			sc.finished=false;
			sc.nbOrdersGiven=0;
			changeLeaderInternal(l, sc);
		}
		gameStatus.setCurrentLeader(l);

	}

	public void flipLeader(final Leader l) {
		final LeaderStatusChange sc = new LeaderStatusChange();

		sc.nbActivations=0;
		sc.finished=false;
		sc.nbOrdersGiven=0;
		sc.eliteUsed=false;
		sc.didTrump=false;
		changeLeaderInternal(l, sc);
	}

	public void markEliteActivationUsed(final Leader leader) {
		final LeaderStatusChange sc = new LeaderStatusChange();
		sc.eliteUsed=true;
		changeLeaderInternal(leader, sc);
	}
}
