package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.CommandModifier;
import ch.megiste.gboh.util.Helper;

public class MissileFire extends UnitCommand {

	private Map<MissileType, List<Integer>> table = new HashMap<>();

	public MissileFire() {
		super("The attacker fires on the defender");
		Helper h = new Helper();
		final List<CSVRecord> records = h.getCsvFromResource("missile.txt");
		for (CSVRecord r : records) {
			MissileType mt = Helper.readEnum(r.get("Type"), MissileType.class, MissileType.NONE);
			List<Integer> values = new ArrayList<>();
			for (int i = 1; i < 6; i++) {

				final String header = "" + i;
				if (r.isSet(header)) {
					String val = r.get(header);
					values.add(Integer.parseInt(val));
				} else {
					break;
				}

			}
			table.put(mt, values);
		}
	}

	@Override
	public String getKey() {
		return "M";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		if (attackers.size() != 1) {
			console.logNL("Only 1 unit can missile fire.");
			return;
		}
		if (defenders.size() != 1) {
			console.logNL("Only 1 unit can be fired at.");
			return;
		}
		Unit attacket = attackers.get(0);
		Unit target = defenders.get(0);
		final String attackerName = Log.buildStaticDesc(attacket);

		if (attacket.getMissile() == MissileType.NONE) {
			console.logNL("Unit " + attackerName + " cannot missile fire");
			return;
		}
		if (attacket.getState() == UnitState.ROUTED) {
			console.logNL("Unit " + attackerName + " cannot missile fire as it is routed");
			return;
		}
		if (attacket.getMissileStatus() == MissileStatus.NO) {
			console.logNL("Unit " + attackerName + " cannot missile fire as it is missile no");
			return;
		}
		if (attacket.getMissileStatus() == MissileStatus.NO) {
			console.logNL("Unit " + attackerName + " cannot missile fire as it is missile NEVER");
			return;
		}

		int range = getIntModifier(modifiers, CommandModifier.r, 1);
		int maxRange = table.get(attacket.getMissile()).size();
		if (range > maxRange) {
			console.logNL("Unit " + attackerName + " cannot fire at this distance");
			return;
		}
		boolean moved = getBooleanModifier(modifiers, CommandModifier.m);
		boolean flank = getBooleanModifier(modifiers, CommandModifier.f);
		boolean back = getBooleanModifier(modifiers, CommandModifier.b);
		boolean norf = getBooleanModifier(modifiers, CommandModifier.norf);

		fire(attacket, target, range, moved, false);

		//Reaction fire
		if (range == 1 && target.getMissile() != MissileType.NONE
				&& target.getStatus().missileStatus != MissileStatus.NO && !flank && !back && !norf) {
			fire(target, attacket, 1, false, true);
		}

	}

	private void fire(final Unit attacker, final Unit target, final int range, final boolean moved,
			final boolean reaction) {
		if(attacker.getMissile()==MissileType.NONE
				|| attacker.getMissileStatus()==MissileStatus.NO
				|| attacker.getMissileStatus()==MissileStatus.NEVER
				|| attacker.getState()==UnitState.ROUTED
				|| attacker.getState()==UnitState.RALLIED
				|| attacker.getState()==UnitState.ELIMINATED){
			return;
		}
		String attackerName = Log.buildStaticDesc(attacker);
		String targetName = Log.buildStaticDesc(target);

		int threshold = table.get(attacker.getMissile()).get(range - 1);

		int roll = dice.roll();

		final String reactionString = reaction ? " in reaction" : "";
		final String rangeText = range == 1 ? "" : " at range " + range;
		String announcementText =
				String.format("%s is firing%s at %s%s", attackerName, reactionString, targetName, rangeText);
		console.logNL(announcementText);

		List<String> modifiers = new ArrayList<>();

		int finalRoll = roll;
		if (target.getKind() == UnitKind.SK) {
			modifiers.add("+2 because target is SK");
			finalRoll = finalRoll + 2;
		}
		if (moved && attacker.getMissile().lessPreciseAfterMovement()) {
			modifiers.add("+1 because movement");
			finalRoll = finalRoll + 1;
		}
		if (attacker.getState() == UnitState.DEPLETED) {
			modifiers.add("+1 because " + attackerName + " depleted");
			finalRoll = finalRoll + 1;
		}

		final String resultText;
		final boolean success;
		if (finalRoll <= threshold) {
			resultText = targetName + " is hit";
			success = true;
		} else {
			success = false;
			resultText = "Missed";
		}

		final String modifiersText = modifiers.size() == 0 ? "" : "(modifiers: " + Joiner.on(",").join(modifiers) + ")";
		String conclusionText = String.format("Dice rolls: %d! %s! %s", roll, resultText, modifiersText);
		console.logNL(conclusionText);
		if (success) {
			unitChanger.addHit(target);
		}

		if (attacker.getMissileStatus() == MissileStatus.LOW || roll >= attacker.getMissile()
				.getMissileShortageLevel()) {
			unitChanger.missileDepletion(attacker);
		}
	}

}
