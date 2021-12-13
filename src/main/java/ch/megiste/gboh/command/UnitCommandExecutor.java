package ch.megiste.gboh.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.GameStatus.FindUnitsResult;
import ch.megiste.gboh.util.CommandLineParsingException;
import ch.megiste.gboh.util.Console;

public class UnitCommandExecutor extends CommandExecutor<UnitCommand> {
	public UnitCommandExecutor(final List<String> inputs, final UnitCommand command, final Console console) {
		super(inputs, command, console);
	}

	@Override
	public void executeCommand() {
		try {
			String firstPart = inputs.get(0);
			final FindUnitsResult res = gameStatus.findUnits(firstPart);
			if (res.unknownValues.size() > 0) {
				throw new CommandLineParsingException(
						"Those units are unknown: " + Joiner.on(", ").join(res.unknownValues));

			}
			final List<String> modifiers = new ArrayList<>();
			List<Unit> destinationUnits = new ArrayList<>();
			if (command.hasTargetUnits() && inputs.size() == 2) {
				throw new CommandLineParsingException(
						"This commands needs to designate target units. Like: %s %s <some modifiers> <targetUnits>",
						firstPart, command.getKey());

			}

			if (command.hasTargetUnits()) {
				String targetUnitsQuery = inputs.get(inputs.size() - 1);
				final FindUnitsResult res2 = gameStatus.findUnits(targetUnitsQuery);
				if (res2.unknownValues.size() > 0) {
					console.logNL("Those units are unknown: " + Joiner.on(", ").join(res2.unknownValues));
					return;
				}
				destinationUnits.addAll(res2.foundUnits);
			}
			List<Modifier<?>> computeModifiers = computeModifiers();


			command.execute(res.foundUnits, destinationUnits, computeModifiers);
			command.logAfterCommand(res.foundUnits, destinationUnits);
		} catch (CommandLineParsingException e) {
			console.logNL(e.getMessage());
			return;
		}

	}

	final Pattern modPattern1 = Pattern.compile("-?([a-z]+)=(\\w+)");
	final Pattern modPattern2 = Pattern.compile("-?([a-z]+)(-?(\\d*))");
	final Pattern modPattern3 = Pattern.compile("-?([a-z]+)(-?([A-Z_]*))");

	List<Modifier<?>> computeModifiers() throws CommandLineParsingException {
		List<Modifier<?>> modifiers = new ArrayList<>();
		List<String> modifiersToParse = new ArrayList<>();
		if (command.hasTargetUnits() && inputs.size() > 3) {

			modifiersToParse.addAll(inputs.subList(2, inputs.size() - 1));

		} else if (!command.hasTargetUnits() && inputs.size() > 2) {
			modifiersToParse.addAll(inputs.subList(2, inputs.size()));

		}
		List<Pattern> patterns = Arrays.asList(modPattern1, modPattern2, modPattern3);
		for (String input : modifiersToParse) {
			Modifier<?> mod = buildModFromInput(patterns, input);

			if (mod != null) {
				modifiers.add(mod);
			}
		}

		return modifiers;
	}

	private Modifier<?> buildModFromInput(final List<Pattern> patterns, final String input)
			throws CommandLineParsingException {

		Matcher m = null;
		for (Pattern p : patterns) {
			m = p.matcher(input);
			if (m.matches()) {
				break;
			}
		}
		if (m == null || !m.matches()) {//NOSONAR
			throw new CommandLineParsingException("Unable to understand this modifier %s", input);
		}

		String parameterName = m.group(1);
		ModifierDefinition def = null;
		try {
			def = ModifierDefinition.valueOf(parameterName);
		} catch (IllegalArgumentException e) {
			throw new CommandLineParsingException("Unknown modifier %s", parameterName);
		}
		if (def.getObjectClass() == null) {
			return new Modifier<Boolean>(def, true);
		} else if (Integer.class.isAssignableFrom(def.getObjectClass()) && m.groupCount() >= 2) {

			try {
				int val = Integer.parseInt(m.group(2));
				return new Modifier<Integer>(def, val);
			} catch (NumberFormatException e) {
				throw new CommandLineParsingException(
						"Pattern is %s=V or %sV where V is an positive or negative integer value", parameterName,parameterName);
			}
		} else if (def.getObjectClass().isEnum() && m.groupCount() >= 2) {
			final Object v; //NOSONAR
			try {
				v = Enum.valueOf((Class<Enum>) def.getObjectClass(), m.group(2));
			} catch (Exception e) {
				final String possibleValues=Joiner.on(",").join(def.getObjectClass().getEnumConstants());
				throw new CommandLineParsingException(
						"Pattern is %s=V or %sV where V is value in the fallowing list (%s)", parameterName,parameterName,possibleValues);
			}
			return new Modifier<Enum>(def, (Enum) v);
		} else {
			return null;
		}
	}
}
