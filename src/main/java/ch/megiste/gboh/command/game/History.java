package ch.megiste.gboh.command.game;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.PersistableGameState.CommandHistory;

public class History extends GameCommand {



	public History() {
		super("Lists the n latest commands (HIST n. Default: 10");
	}

	@Override
	public String getKey() {
		return "HIST";
	}

	@Override
	public void execute(final GameStatus gs, final List<String> commandArgs) {
		final List<CommandHistory> commands = gs.getState().commandHistories;
		int maxDepth = commands.size();

		int depth = 9;
		try {
			if (CollectionUtils.isNotEmpty(commandArgs)) {
				depth = Integer.parseInt(commandArgs.get(0));
			}
		} catch (NumberFormatException e) {
			console.logNL("Usage HIST n with n being the number of commands to display");
			return;
		}

		final int nbCommands = Math.min(maxDepth, depth);
		for (int i = nbCommands; i >= 1; i--) {
			console.logNL("U" + i + " -> " + commands.get(maxDepth - i).description());
		}

	}
}
