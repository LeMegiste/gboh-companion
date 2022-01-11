package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Reset extends GameCommand {
	public Reset() {
		super("Resets the current game");
	}

	@Override
	public String getKey() {
		return "RESET";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		String val = console.readLine(
				"This command will reset the current game to turn 1, command 1.\nAll progress will be lost.\nDo you want to proceed? [y/n]\n>>");
		if (!"y".equals(val)) {
			return;
		}

		console.logNL("Backing up current game");
		gs.persistGame(true);

gs.resetGame();
	}
}
