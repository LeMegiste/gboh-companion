package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.Command;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;

public abstract class UnitCommand extends Command {

	protected UnitCommand(final String description) {
		super(description);
	}

	public abstract String getKey();

	public abstract void execute(List<Unit> attackers, List<Unit> defenders, final List<Modifier<?>> modifiers);

	protected int getIntModifier(final List<Modifier<?>> modifiers, final ModifierDefinition key, final int defaultValue) {
		if (modifiers == null) {
			return defaultValue;
		}
		return modifiers.stream().filter(m->m.getDefinition()==key).mapToInt(m->(Integer)m.getValue()).findFirst().orElse(defaultValue);


	}

	protected <T extends Enum<T>> T getEnumModifier(final List<Modifier<?>> modifiers, final ModifierDefinition key,
			final T defaultValue) {

		if (modifiers == null) {
			return defaultValue;
		}
		return modifiers.stream().filter(m->m.getDefinition()==key).map(m->(T)m.getValue()).findFirst().orElse(defaultValue);



	}

	protected boolean getBooleanModifier(final List<Modifier<?>> modifiers, final ModifierDefinition key) {
		if (modifiers == null) {
			return false;
		}
		return modifiers.stream().anyMatch(m -> m.getDefinition() == key);
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

	protected List<ModifierDefinition> possibleModifiers = new ArrayList<>();

	public List<ModifierDefinition> getPossibleModifiers() {
		return possibleModifiers;
	}

	/**
	 * Returns true if the command has a target.
	 * @return
	 */
	public boolean hasTargetUnits(){
		return false;
	}
}
