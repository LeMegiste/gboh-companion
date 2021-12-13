package ch.megiste.gboh.command.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.PersistableGameState.CommandHistory;
import ch.megiste.gboh.game.PersistableGameState.UnitChange;

public class Undo extends GameCommand {

	public static final String WHICH_COMMAND_TO_UNDO = "Which command to undo ?>>";

	public Undo() {
		super("Undoes one of the last 10 commands");
	}

	@Override
	public String getKey() {
		return "UNDO";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		final List<CommandHistory> commands = gs.getState().commandHistories;
		int maxDepth = commands.size();

		final int nbCommands = Math.min(maxDepth, 9);
		for (int i = nbCommands; i >= 1; i--) {
			console.logNL("U" + i + " -> " + commands.get(maxDepth - i).description());
		}
		String val = console.readLine(WHICH_COMMAND_TO_UNDO);
		String valWithoutU = val.replace("U", "");
		if (valWithoutU.length() != 1 || !Character.isDigit(valWithoutU.charAt(0))) {
			console.logNL("Invalid answer:" + val);
		} else {
			CommandHistory commandHistory = commands.get(maxDepth - Integer.parseInt(valWithoutU));
			console.logNL("Cancelling: " + commandHistory.description());
			List<UnitChange> changesReversed = new ArrayList<>(commandHistory.getChanges());
			Collections.reverse(changesReversed);
			for (UnitChange uc : changesReversed) {
				unitChanger.changeStateForUndo(uc.getUnitCode(),uc.getBefore());
			}
		}
	}
}
