package ch.megiste.gboh.command.game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Battles extends GameCommand {
	public Battles() {
		super("List the battles available in *battles* directory");
	}

	@Override
	public String getKey() {
		return "BATTLES";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		Path battlesDir = Paths.get(".").resolve("battles");
		try {
			final Stream<Path> dirs = Files.list(battlesDir);
			List<String> battleNames = new ArrayList<>();
			dirs.forEach(p -> {
				if (Files.isDirectory(p)) {
					battleNames.add(p.getFileName().toString());
				}
			});
			Collections.sort(battleNames);
			console.logFormat("%d. None", 0);
			for (int i = 0; i < battleNames.size(); i++) {
				console.logFormat("%d. %s", (i + 1), battleNames.get(i));
			}
			String s = console.readLine("Which battle to you choose?");
			int i = 0;
			try {
				i = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				i = 0;
			}
			if (i == 0) {
				return;
			}
			if(i>battleNames.size()){
				return;
			}
			String battle = battleNames.get(i - 1);
			final Path resolve = battlesDir.resolve(battle);
			if (Files.exists(resolve)) {
				gameStatus.load(resolve);
			}

		} catch (IOException e) {
			e.printStackTrace();
			console.logNL("Error accessing " + battlesDir.toAbsolutePath().toString() + ". " + e.getMessage());
		}
	}
}
