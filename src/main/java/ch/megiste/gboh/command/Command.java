package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.List;

import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.GameStatus.Rules;
import ch.megiste.gboh.game.LeadersHandler;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public abstract class Command implements Comparable {

	protected Command(final String description) {
		this.description = description;
	}

	protected Console console;

	protected UnitChanger unitChanger;

	protected LeadersHandler leadersHandler;

	protected GameStatus gameStatus;

	public void setUnitChanger(final UnitChanger unitChanger) {
		this.unitChanger = unitChanger;
	}

	protected Dice dice;

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

	public List<String> getSynonyms() {
		return new ArrayList<>();
	}

	public boolean isExecutableFrom(String c) {
		return getKey().equals(c) || getSynonyms().contains(c);
	}

	@Override
	public int compareTo(final Object o) {
		if (o instanceof UnitCommand) {
			UnitCommand uc = (UnitCommand) o;
			return this.getKey().compareTo(uc.getKey());
		}
		return 1;
	}

	public Rules getCurrentRules() {
		return unitChanger.getCurrentRules();
	}

	public void setLeadersHandler(final LeadersHandler leadersHandler) {
		this.leadersHandler = leadersHandler;
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(final GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}
}
