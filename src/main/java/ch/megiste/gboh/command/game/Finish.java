package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Finish extends GameCommand {
	public Finish() {
		super("Marks current leader as finished");
	}

	@Override
	public String getKey() {
		return "FIN";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		if (!gs.areLeadersUsed()) {
			console.logNL("This scenario did not define leaders. The command cannot be executed.");
		}
		if(gs.getCurrentLeader()==null){
			console.logNL("No current leader");
		}
		console.logNL("Marking leader "+gs.getCurrentLeader().getName() + " as finished and switching to next leader");
		leadersHandler.markLeaderAsFinished(gs.getCurrentLeader());

		if (gs.computeNextLeader() == null) {
			console.logNL("There is no leader left to activate. The only command possible is END_TURN");
			return;
		} else {
			gs.activateNextLeader();
		}

	}
}
