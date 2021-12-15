package ch.megiste.gboh.army;

import java.util.ArrayList;
import java.util.List;

import ch.megiste.gboh.army.UnitStatus.UnitState;

public class Army {

	private String name;
	private List<Unit> units;
	private List<Leader> leaders = new ArrayList<>();
	private Integer routPoints;

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

	public List<Leader> getLeaders() {
		return leaders;
	}

	public Integer getRoutPoints() {
		return routPoints;
	}

	public void setRoutPoints(final Integer routPoints) {
		this.routPoints = routPoints;
	}
}


