package ch.megiste.gboh.army;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.megiste.gboh.army.UnitStatus.UnitState;

public class Combat {

	private List<Unit> attackers = new ArrayList<>();
	private List<Unit> defenders = new ArrayList<>();

	private Map<Unit, UnitState> statesBeforeCombat = new HashMap<>();

	public List<Unit> getAttackers() {
		final List<Unit> units = new ArrayList<>(this.attackers);
		units.removeAll(unitsOutOfCombat());
		return units;
	}

	public List<Unit> getDefenders() {
		final List<Unit> units = new ArrayList<>(this.defenders);
		units.removeAll(unitsOutOfCombat());
		return units;
	}

	public Unit getMainAttacker() {
		return getAttackers().get(0);
	}

	public Unit getMainDefender() {
		return getDefenders().get(0);
	}

	public Combat(final List<Unit> attackers, final List<Unit> defenders) {
		this.attackers = attackers;
		this.defenders = defenders;
		storeStates();
	}

	public Combat(Unit attacker, Unit defender) {
		this.attackers = Collections.singletonList(attacker);
		this.defenders = Collections.singletonList(defender);
		storeStates();
	}

	public Combat(Unit attacker1, Unit attacker2, Unit defender) {
		this.attackers = Arrays.asList(attacker1, attacker2);
		this.defenders = Collections.singletonList(defender);
		storeStates();
	}

	private void storeStates() {
		for (Unit u : attackers) {
			statesBeforeCombat.put(u, u.getState());
		}
		for (Unit u : defenders) {
			statesBeforeCombat.put(u, u.getState());
		}
	}

	private List<Unit> unitsOutOfCombat() {
		List<Unit> unitsOutOfCombat = new ArrayList<>();
		for (Unit u : attackers) {
			if (statesBeforeCombat.get(u) != u.getState()) {
				unitsOutOfCombat.add(u);
			}
		}
		for (Unit u : defenders) {
			if (statesBeforeCombat.get(u) != u.getState()) {
				unitsOutOfCombat.add(u);
			}
		}
		return unitsOutOfCombat;
	}

	public boolean isOver() {
		return getAttackers().size() == 0 || getDefenders().size() == 0;
	}
}