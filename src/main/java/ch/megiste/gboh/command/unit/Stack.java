package ch.megiste.gboh.command.unit;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import ch.megiste.gboh.army.Unit;

public class Stack extends UnitCommand {
	private static final String KEY = "ST";

	public Stack() {
		super("Stacks the two units together. <firstUnit> " + KEY
				+ " <secondUnit>. The first one goes behind the second one.");
	}

	@Override
	public String getKey() {

		return KEY;
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		Unit under = attackers.get(0);
		if (CollectionUtils.isEmpty(defenders)) {
			console.logNL("Usage is: <firstUnit> " + KEY
					+ " <secondUnit>. The first unit goes behind the second one.");
			return;
		}
		Unit top = defenders.get(0);
		if(under.isStacked()){
			console.logNL(""+Log.logUnitDetailed(under)+ " is already stacked. Cannot be stacked again.");
			return;
		}
		if(top.isStacked()){
			console.logNL(""+Log.logUnitDetailed(top)+ " is already stacked. Cannot be stacked again.");
			return;
		}
		console.logNL("Stacking "+Log.logUnitDetailed(under) + " under " + Log.logUnitDetailed(top));

		unitChanger.stack(top.getUnitCode(),under.getUnitCode());
	}
}
