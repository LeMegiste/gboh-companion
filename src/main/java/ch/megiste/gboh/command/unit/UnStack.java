package ch.megiste.gboh.command.unit;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import ch.megiste.gboh.army.Unit;

public class UnStack extends UnitCommand {
	private static final String KEY = "US";

	public UnStack() {
		super("Unstacks the two units together. <firstUnit> " + KEY
				+ " <secondUnit>.");
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
		if(!under.isStacked()){
			console.logNL(""+Log.logUnitDetailed(under)+ " is not stacked. Cannot be unstacked.");
			return;
		}
		if(!top.isStacked()){
			console.logNL(""+Log.logUnitDetailed(top)+ " is not stacked. Cannot be unstacked.");
			return;
		}
		if(!under.isStackedWith(top)){
			console.logNL(""+Log.logUnitDetailed(under)+ " and "+Log.logUnitDetailed(top)+" are not stacked together. Cannot be unstacked.");
			return;
		}
		console.logNL("Unstacking "+Log.logUnitDetailed(under) + " and " + Log.logUnitDetailed(top));


		unitChanger.unStack(top.getUnitCode(),under.getUnitCode());
	}
}
