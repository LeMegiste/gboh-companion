package ch.megiste.gboh.command.game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ch.megiste.gboh.game.GameStatus;

public class Load extends GameCommand {
	public Load() {
		super("Loads the battle in the folder provided");
	}

	@Override
	public String getKey() {
		return "LOAD";
	}

	@Override
	public void execute(final GameStatus gs, final List<String> commandArgs) {
		if (commandArgs.isEmpty() || commandArgs.get(0).length() == 0) {
			console.logNL("Provide a battle path as parameter");
			return;
		}
		Path p = Paths.get(commandArgs.get(0));
		if (Files.exists(p) && Files.isDirectory(p)) {
			gs.load(p);
		} else {
			console.logNL("" + p + " is not a valid battle path.");
		}

	}
}
