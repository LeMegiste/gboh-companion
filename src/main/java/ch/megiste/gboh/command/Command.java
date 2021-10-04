package ch.megiste.gboh.command;

import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public abstract class Command implements Comparable {

	protected Command(final String description) {
		this.description = description;
	}

	protected Console console;

	protected UnitChanger unitChanger;

	public void setUnitChanger(final UnitChanger unitChanger) {
		this.unitChanger = unitChanger;
	}

	protected Dice dice = new Dice();

	public Dice getDice() {
		return dice;
	}

	public void setDice(final Dice dice) {
		this.dice = dice;
	}

	public void setConsole(final Console console) {
		this.console = console;
	}

	protected String description;

	public String getDescription() {
		return description;
	}

	public abstract String getKey();

	@Override
	public int compareTo(final Object o) {
		if (o instanceof UnitCommand) {
			UnitCommand uc = (UnitCommand) o;
			return this.getKey().compareTo(uc.getKey());
		}
		return 1;
	}
}
