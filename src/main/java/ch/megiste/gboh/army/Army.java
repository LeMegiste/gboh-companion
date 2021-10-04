package ch.megiste.gboh.army;

import java.util.ArrayList;
import java.util.List;

import ch.megiste.gboh.army.UnitStatus.UnitState;

public class Army {

	private String name;
	private List<Unit> units ;

	public Army(final String name, final List<Unit> units) {
		this.name = name;
		this.units = units;
	}

	public String getName() {
		return name;
	}

	public List<Unit> getUnits() {
		return units;
	}

	public int routPoints(){
		return units.stream().filter(u->u.getState()== UnitState.ELIMINATED).mapToInt(u->u.getRountPoints()).sum();
	}
}
