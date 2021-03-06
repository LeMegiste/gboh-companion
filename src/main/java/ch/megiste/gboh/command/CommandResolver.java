package ch.megiste.gboh.command;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.megiste.gboh.command.game.GameCommand;
import ch.megiste.gboh.command.leader.LeaderCommand;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.game.LeadersHandler;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.ClassLocation;
import ch.megiste.gboh.util.ClassLocator;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;
import ch.megiste.gboh.util.GbohError;

public class CommandResolver {

	private CommandsHolder commandsHolder = new CommandsHolder();

	public GameCommand resolveGameCommand(final String command) {
		return commandsHolder.getGameCommands().stream().filter(c -> c.isExecutableFrom(command)).findFirst()
				.orElse(null);
	}

	public Set<ClassLocation> getAllClassLocations(String packageName, String... otherPackages) throws Exception {
		ClassLocator locator = new ClassLocator(packageName);
		final Set<ClassLocation> classLocations = new java.util.HashSet<>();
		classLocations.addAll(locator.getAllClassLocations());
		for (String p : otherPackages) {
			locator = new ClassLocator(p);
			classLocations.addAll(locator.getAllClassLocations());
		}

		return classLocations;
	}

	public CommandResolver(Console console, UnitChanger uc, final LeadersHandler leadersHandler) {
		try {

			final Set<ClassLocation> classLocations =
					getAllClassLocations("ch.megiste.gboh.command", "ch.megiste.gboh.command.game",
							"ch.megiste.gboh.command.unit", "ch.megiste.gboh.command.leader");
			Dice dice = new Dice();
			for (ClassLocation cl : classLocations) {
				Class<?> clazz = Class.forName(cl.getClassName());
				if (Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				if (UnitCommand.class.isAssignableFrom(clazz)) {
					UnitCommand c = (UnitCommand) clazz.getConstructor().newInstance();
					c.setConsole(console);
					c.setUnitChanger(uc);
					c.setDice(dice);
					c.setLeadersHandler(leadersHandler);
					c.setGameStatus(uc.getGameStatus());
					commandsHolder.getUnitCommands().add(c);

				}
				linkUnitCommands();
				if (GameCommand.class.isAssignableFrom(clazz)) {
					GameCommand c = (GameCommand) clazz.getConstructor().newInstance();
					c.setConsole(console);
					c.setUnitChanger(uc);
					c.setDice(dice);
					c.setLeadersHandler(leadersHandler);
					c.setGameStatus(uc.getGameStatus());
					commandsHolder.getGameCommands().add(c);
					c.setCommandsHolder(commandsHolder);

				}
				if (LeaderCommand.class.isAssignableFrom(clazz)) {
					LeaderCommand c = (LeaderCommand) clazz.getConstructor().newInstance();
					c.setConsole(console);
					c.setUnitChanger(uc);
					c.setDice(dice);
					c.setLeadersHandler(leadersHandler);
					c.setGameStatus(uc.getGameStatus());
					commandsHolder.getLeaderCommands().add(c);
					c.setCommandsHolder(commandsHolder);

				}

				checkLetters();

			}
		} catch (Exception e) {
			throw new GbohError(e);
		}
	}

	private void checkLetters() {
		List<String> keys = new ArrayList<>();
		for (Command uc : commandsHolder.getAllCommands()) {
			if (keys.contains(uc.getKey())) {
				throw new GbohError("Another command already uses the key " + uc.getKey());
			} else {
				keys.add(uc.getKey());
			}
			for (String s : uc.getSynonyms()) {
				if (keys.contains(s)) {
					throw new GbohError("Another command already uses the synonym " + s);
				} else {
					keys.add(s);
				}

			}
		}

	}

	private void linkUnitCommands() {
		try {
			Map<Class<? extends UnitCommand>, UnitCommand> commandsByClass = new HashMap();

			for (UnitCommand c : commandsHolder.getUnitCommands()) {
				commandsByClass.put(c.getClass(), c);
			}
			for (UnitCommand c : commandsHolder.getUnitCommands()) {
				Class<?> clazz = c.getClass();
				for (Method m : clazz.getMethods()) {
					if (m.getName().startsWith("set") && m.getParameterCount() == 1 && UnitCommand.class
							.isAssignableFrom(m.getParameterTypes()[0])) {
						m.invoke(c, commandsByClass.get(m.getParameterTypes()[0]));
					}
				}
			}
		} catch (Exception e) {
			throw new GbohError(e);
		}
	}

	public UnitCommand resolveUnitCommand(final String command) {
		return commandsHolder.getUnitCommands().stream().filter(c -> c.isExecutableFrom(command)).findFirst()
				.orElse(null);
	}

	public LeaderCommand resolveLeaderCommand(final String command) {
		return commandsHolder.getLeaderCommands().stream().filter(c -> c.isExecutableFrom(command)).findFirst()
				.orElse(null);
	}
}
