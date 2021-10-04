package ch.megiste.gboh;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.google.common.base.Splitter;

import ch.megiste.gboh.command.CommandResolver;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;

public class Main {

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);

		Properties p = Helper.loadProperties();

		//Register command
		Console l = new Console() {

			@Override
			public void logNL(final String str) {

			}

			@Override
			public String readLine(final String prompt) {
				return null;
			}
		};
		GameStatus gs = new GameStatus();
		UnitChanger unitChanger = new UnitChanger(l, gs);

		CommandResolver resolver = new CommandResolver(l, unitChanger);

		String currentBattle = p.getProperty("currentbattle");
		Path battleDir = Paths.get(currentBattle);
		gs.load(battleDir);

		CommandProcessor cp = new CommandProcessor(l, gs, resolver);

		while (true) {

			//final int res = RawConsoleInput.read(false);

			gs.nextCommand("toto");
			//getting user input
			//l.prompt(gs.getPromptString());

			String userInput = scanner.nextLine();

			final List<String> commands = Splitter.on("&&").omitEmptyStrings().trimResults().splitToList(userInput);
			for (String c : commands) {
				cp.processInput(c);
			}

		}
	}

}