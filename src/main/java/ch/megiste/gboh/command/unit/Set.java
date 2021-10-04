package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.CommandModifier;

public class Set extends UnitCommand {
	public Set() {
		super("Sets the counter values for the units.");
	}

	@Override
	public String getKey() {
		return "SET";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		for (Unit u : attackers) {
			int hits = getIntModifier(modifiers, CommandModifier.hits, u.getHits());
			UnitState state = getEnumModifier(modifiers, CommandModifier.state, UnitState.class, u.getState());
			MissileStatus missileState =
					getEnumModifier(modifiers, CommandModifier.missile, MissileStatus.class, u.getMissileStatus());
			unitChanger.changeStateNoCheck(u, hits, state, missileState);
		}

	}
}
