package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Exit extends GameCommand {

	public Exit() {
		super("SAVE the game and exits");
	}

	@Override
	public String getKey() {
		return "EXIT";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		gs.persistGame();
		gs.persistGeneralProperties();
		System.exit(0);

	}
}
