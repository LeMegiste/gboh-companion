package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.MissileWeapon;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;
import ch.megiste.gboh.util.Helper;

public class ReactionFire extends UnitCommand {

	private MissileFire missileFire;

	public ReactionFire() {
		super("The attacker does a reaction fire on the defender. If the attacker has many missiles, all of tjem are fired.");
	}

	@Override
	public String getKey() {
		return "RF";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<Modifier<?>> modifiers) {
		if (attackers.size() != 1) {
			console.logNL("Only 1 unit can reaction fire.");
			return;
		}
		if (defenders.size() != 1) {
			console.logNL("Only 1 unit can be reaction-fired at.");
			return;
		}

		Unit attacker = attackers.get(0);
		final List<Modifier<?>> missileFireModifiers = new ArrayList<>();
		missileFireModifiers.add(new Modifier<>(ModifierDefinition.norf,true));
		missileFire.execute(attackers,defenders,missileFireModifiers);
		if(attacker.getMissiles().size()>1){
			missileFireModifiers.add(new Modifier<>(ModifierDefinition.sec,true));
			missileFire.execute(attackers,defenders,missileFireModifiers);
		}

	}

	@Override
	public boolean hasTargetUnits() {
		return true;
	}

	@Override
	public List<String> getSynonyms() {
		return Collections.singletonList("RFire");
	}

	public void setMissileFire(final MissileFire missileFire) {
		this.missileFire = missileFire;
	}
}
