package ch.megiste.gboh.command.game;

import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Dump extends GameCommand {

	public Dump() {
		super("Dumps the armies in their current state in the game folder.");
	}

	@Override
	public String getKey() {
		return "DUMP";
	}

	@Override
	public void execute(final GameStatus gs, final List<String> commandArgs) {
gs.dump();
	}
}
