package ch.megiste.gboh.command.game;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class NextDice extends GameCommand {
	public NextDice() {
		super("records the next values as the next dice throws");
	}

	@Override
	public String getKey() {
		return "ND";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		if (commandArgs.isEmpty()) {
			console.logNL("No dice throw to record. Please provide the numbers between 0 and 9 separated by commas");
			return;
		}
		String strElems = Joiner.on(",").join(commandArgs);
		List<String> nextDices = Splitter.on(",").omitEmptyStrings().splitToList(strElems);
		for (String str : nextDices) {
			if (str.length() == 1 && Character.isDigit(str.charAt(0))) {
				int d = Integer.parseInt(str);
				if(d<0 || d>9){
					console.logNL("Please only provide values between 0 and 9");
					continue;
				}
				dice.nextThrow(d);
			}
		}
	}
}
