package ch.megiste.gboh;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.CommandResolver;
import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.Console;

public class CommandProcessor {

	private Console console;
	private GameStatus gameStatus;
	private CommandResolver commandResolver;

	public CommandProcessor(final Console console, final GameStatus gameStatus, final CommandResolver commandResolver) {
		this.console = console;
		this.gameStatus = gameStatus;
		this.commandResolver = commandResolver;
	}

	public void processInput(final String userInput) {
		gameStatus.nextCommand(userInput);

		List<String> inputs = Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(userInput);

		if (inputs.isEmpty()) {
			return;
		}

		String firstPart = inputs.get(0);

		GameCommand gc = commandResolver.resolveGameCommand(firstPart);
		if (gc != null) {

			final List<String> commandArgs;
			if (inputs.size() > 1) {
				commandArgs = inputs.subList(1, inputs.size() - 1);
			} else {
				commandArgs = new ArrayList<>();
			}
			gc.execute(gameStatus, commandArgs);
			return;
		}

		//Else we are designating units
		final String unitCommand;
		if (inputs.size() > 1) {
			unitCommand = inputs.get(1);
		} else {
			unitCommand = Log.LOG;
		}

		UnitCommand uc = commandResolver.resolveUnitCommand(unitCommand);
		if (uc == null) {
			console.logNL("Unknown command: " + unitCommand);
			return;
		}

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
					console.logNL("Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));
					return;
				}
				destinationUnits.addAll(res2.foundUnits);
			}

			final List<String> mods = inputsAfterCommand.stream().filter(s -> s.startsWith("-")).map(s -> s.substring(1))
					.collect(Collectors.toList());
			modifiers.addAll(mods);

		}

		uc.execute(res.foundUnits, destinationUnits, modifiers);
		uc.logAfterCommand(res.foundUnits, destinationUnits);

	}

}
