package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class RoutPoints extends GameCommand {
	public RoutPoints() {
		super("Computes the current rout points for both armies");
	}

	@Override
	public String getKey() {
		return "ROUT_POINTS";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		console.logNL(String.format("%s - rout points: %d",gs.getArmy1().getName(),gs.computeArmy1RoutPoints()));
		console.logNL(String.format("%s - rout points: %d",gs.getArmy2().getName(),gs.computeArmy2RoutPoints()));
	}
}
