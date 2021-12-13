package ch.megiste.gboh.command;

import java.util.List;

import com.google.common.base.Joiner;

import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.Console;

public class LeaderCommandExecutor extends CommandExecutor<LeaderCommand> {
	public LeaderCommandExecutor(final List<String> inputs, final LeaderCommand command, final Console console) {
		super(inputs, command, console);
	}

	@Override
	public void executeCommand() {
		if (!gameStatus.areLeadersUsed()) {
			console.logNL("This scenario does not use leaders");
			return;
		}
		String firstPart = inputs.get(0);
		final FindUnitsResult res = gameStatus.findLeaders(firstPart);
		if (res.unknownValues.size() > 0) {
			console.logNL("Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));
			return;
		}
		res.foundLeaders.forEach(command::execute);

	}
}
