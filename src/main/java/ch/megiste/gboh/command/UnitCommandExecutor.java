package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.Console;

public class UnitCommandExecutor extends CommandExecutor<UnitCommand> {
	public UnitCommandExecutor(final List<String> inputs, final UnitCommand command, final Console console) {
		super(inputs, command, console);
	}

	@Override
	public void executeCommand() {
		String firstPart = inputs.get(0);
		final FindUnitsResult res = gameStatus.findUnits(firstPart);
		if (res.unknownValues.size() > 0) {
			console.logNL("Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));
			return;
		}
		final List<String> modifiers = new ArrayList<>();
		List<Unit> destinationUnits = new ArrayList<>();
		if (inputs.size() > 2) {
			final List<String> inputsAfterCommand = inputs.subList(2, inputs.size());
			Optional<String> optDestinationUnitsQuery =
					inputsAfterCommand.stream().filter(s -> !s.startsWith("-")).findFirst();
			if (optDestinationUnitsQuery.isPresent()) {
				final FindUnitsResult res2 = gameStatus.findUnits(optDestinationUnitsQuery.get());
				if (res2.unknownValues.size() > 0) {
					console.logNL("Those units are unknown: " + Joiner.on(", ").join(res2.unknownValues));
					return;
				}
				destinationUnits.addAll(res2.foundUnits);
			}

			final List<String> mods =
					inputsAfterCommand.stream().filter(s -> s.startsWith("-")).map(s -> s.substring(1))
							.collect(Collectors.toList());
			modifiers.addAll(mods);

		}

		command.execute(res.foundUnits, destinationUnits, modifiers);
		command.logAfterCommand(res.foundUnits, destinationUnits);

	}
}
