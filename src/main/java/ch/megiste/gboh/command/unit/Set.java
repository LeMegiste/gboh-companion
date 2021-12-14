package ch.megiste.gboh.command.unit;

import java.util.Collections;
import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;

public class Set extends UnitCommand {
	public Set() {
		super("Sets the counter values for the units.");
	}

	@Override
	public String getKey() {
		return "SET";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<Modifier<?>> modifiers) {
		for (Unit u : attackers) {
			int hits = getIntModifier(modifiers, ModifierDefinition.hits, u.getHits());
			UnitState state = getEnumModifier(modifiers, ModifierDefinition.state, u.getState());
			MissileStatus missileState =
					getEnumModifier(modifiers, ModifierDefinition.missile, u.getMainMissileStatus());
			unitChanger.changeStateNoCheck(u, hits, state, Collections.singletonMap(u.getMainMissile(),missileState));
		}

	}
}
