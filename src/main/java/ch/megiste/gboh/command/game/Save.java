package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Save extends GameCommand {

	public Save() {
		super("Persists the game in a backup file");
	}

	@Override
	public String getKey() {
		return "SAVE";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
gs.persistGame(true);
	}
}
