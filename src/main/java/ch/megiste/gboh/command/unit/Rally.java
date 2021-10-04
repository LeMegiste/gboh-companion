package ch.megiste.gboh.command.unit;

import java.util.Arrays;
import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;

public class Rally extends UnitCommand {

	private List<Integer> rallyValuesWeakUnits = Arrays.asList(1, 1, 2, 2, 2, 2, 2, 2, 3, 3);
	private List<Integer> rallyValuesStrongUnits = Arrays.asList(1, 1, 2, 2, 2, 2, 3, 3, 4, 4);

	public Rally() {
		super("Rallies the current unit");
	}

	@Override
	public String getKey() {
		return "R";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		for (Unit u : attackers) {
			if (u.getStatus().state != UnitState.ROUTED) {
				console.logNL("Impossible to rally " + Log.buildStaticDesc(u) + " as it is not in state ROUTED");
				return;
			}

		}
		for (Unit u : attackers) {

			int hits;
			int roll = dice.roll();
			if (u.getOriginalTq() <= 5) {
				hits = rallyValuesWeakUnits.get(roll);
			} else {
				hits = rallyValuesStrongUnits.get(roll);
			}

			unitChanger.changeState(u, hits, UnitState.RALLIED, MissileStatus.NO);
		}
		for (Unit u : attackers) {
			console.logNL(Log.logUnit(u));
		}
	}
}
