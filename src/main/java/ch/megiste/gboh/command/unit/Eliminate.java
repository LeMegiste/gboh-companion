package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.Modifier;

public class Eliminate extends UnitCommand {
	public Eliminate() {
		super("Eliminates the unit(s)");
	}

	@Override
	public String getKey() {
		return "E";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<Modifier<?>> modifiers) {
		for(Unit u: attackers){
			unitChanger.changeState(u,null, UnitState.ELIMINATED);
		}
	}
}
