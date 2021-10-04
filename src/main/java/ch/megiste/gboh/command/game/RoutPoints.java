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
	public void execute(final GameStatus gs, final List<String> commandArgs) {
		console.logNL(String.format("Army %s - rout points: %d",gs.getArmy1().getName(),gs.getArmy1().routPoints()));
		console.logNL(String.format("Army %s - rout points: %d",gs.getArmy2().getName(),gs.getArmy2().routPoints()));
	}
}
