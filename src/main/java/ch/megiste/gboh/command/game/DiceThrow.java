package ch.megiste.gboh.command.game;

import java.util.List;

public class DiceThrow extends GameCommand {
	public DiceThrow() {
		super("Just throws a dice and logs the result");
	}

	@Override
	public String getKey() {
		return "D";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		int r = dice.roll();
		console.logNL("Dice rolled: [" + r+"]");
	}
}
