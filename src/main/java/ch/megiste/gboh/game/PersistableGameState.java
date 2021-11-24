package ch.megiste.gboh.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ch.megiste.gboh.army.UnitStatus;

@XStreamAlias("GameState")
public class PersistableGameState {

	public int currentTurn = 1;
	public int currentCommand = 0;

	@XStreamImplicit
	public List<CommandHistory> commandHistories = new ArrayList<>();

	@XStreamAlias("Command")
	public static class CommandHistory {

		@XStreamAsAttribute
		private int commandNumber;

		@XStreamAsAttribute
		private String commandText;

		@XStreamAsAttribute
		private int turn;

		@XStreamImplicit
		private List<UnitChange> changes = new ArrayList<>();

		public CommandHistory(final int commandNumber, final String commandText, int turn) {
			this.commandNumber = commandNumber;
			this.commandText = commandText;
			this.turn = turn;
		}

		public List<UnitChange> getChanges() {
			return changes;
		}

		@Override
		public String toString() {
			return "Command " + commandNumber + ": " + commandText + "";
		}

		public String description() {
			final List<String> affectedUnits =
					getChanges().stream().map(UnitChange::getUnitCode).distinct().collect(Collectors.toList());
			return "Command " + commandNumber + ": " + commandText + " affecting " + Joiner.on(",").join(affectedUnits);
		}
	}

	@XStreamAlias("Change")
	public static class UnitChange {

		@XStreamAsAttribute
		private String unitCode;

		private UnitStatus before;
		private UnitStatus after;

		public UnitChange(final String unitCode, final UnitStatus before, final UnitStatus after) {
			this.unitCode = unitCode;
			this.before = before;
			this.after = after;
		}

		public String getUnitCode() {
			return unitCode;
		}

		public UnitStatus getBefore() {
			return before;
		}

		public UnitStatus getAfter() {
			return after;
		}
	}

	public Optional<CommandHistory> getCommandForIndex(int idx) {
		if(commandHistories==null){
			return Optional.empty();
		}
		return commandHistories.stream().filter(cmd -> cmd.commandNumber == idx).findFirst();
	}

}
