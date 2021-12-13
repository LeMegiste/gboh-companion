package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.CommandModifier;

public abstract class UnitCommand extends Command {

	protected UnitCommand(final String description) {
		super(description);
	}

	public abstract String getKey();

	public abstract void execute(List<Unit> attackers, List<Unit> defenders, final List<String> modifiers);

	protected int getIntModifier(final List<String> modifiers, final CommandModifier key, final int defaultValue) {
		if (modifiers == null) {
			return defaultValue;
		}
		final Optional<String> optMod = modifiers.stream().filter(m -> m.startsWith(key.name() + "=")).findFirst();
		if (optMod.isPresent()) {
			String strNumb = optMod.get().substring(key.name().length());
			final String[] elems = strNumb.split("=");
			if (elems.length != 2) {
				console.logNL("Invalid int value:" + elems[1] + ".");

				return defaultValue;
			}

			return Integer.parseInt(elems[1]);
		}

		return defaultValue;
	}

	protected <T extends Enum<T>> T getEnumModifier(final List<String> modifiers, final CommandModifier key,
			Class<T> clazz, final T defaultValue) {
		if (modifiers == null) {
			return defaultValue;
		}
		final Optional<String> optMod = modifiers.stream().filter(m -> m.startsWith(key.name() + "=")).findFirst();
		if (optMod.isPresent()) {
			String str = optMod.get().substring(key.name().length());
			final String[] elems = str.split("=");
			if (elems.length != 2) {
				return defaultValue;
			}
			try {
				return Enum.valueOf(clazz, elems[1]);
			} catch (Exception e) {
				console.logNL("Invalid value:" + elems[1] + ". Possible values: " + Joiner.on(", ")
						.join(clazz.getEnumConstants()) + ".");

				return defaultValue;
			}
		}
		return defaultValue;

	}

	protected boolean getBooleanModifier(final List<String> modifiers, final CommandModifier key) {
		if (modifiers == null) {
			return false;
		}
		final Optional<String> optMod = modifiers.stream().filter(m -> m.equals(key.name())).findFirst();
		return optMod.isPresent();
	}

	public void logAfterCommand(final List<Unit> attackers, final List<Unit> defenders) {
		for (Unit u : attackers) {
			console.logNL("Final status: " + Log.logUnitDetailed(u));
		}
		if (defenders != null) {
			for (Unit u : defenders) {
				console.logNL("Final status: " + Log.logUnitDetailed(u));
			}
		}
	}

	protected List<CommandModifier> possibleModifiers = new ArrayList<>();

	public List<CommandModifier> getPossibleModifiers() {
		return possibleModifiers;
	}
}
