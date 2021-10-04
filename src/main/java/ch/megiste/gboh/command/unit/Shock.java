package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.megiste.gboh.army.Combat;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.CommandModifier;
import ch.megiste.gboh.util.Helper;

public class Shock extends UnitCommand {

	private Fight fight = new Fight();
	private MissileFire missileFire = new MissileFire();

	public Shock() {
		super("Shock ! Full shock combat (fire, pre-shock, shock) between attackers and defenders");
	}

	public void setFight(final Fight fight) {
		this.fight = fight;
	}

	public void setMissileFire(final MissileFire missileFire) {
		this.missileFire = missileFire;
	}

	@Override
	public String getKey() {
		return "S";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		final List<Combat> combats = fight.buildCombats(attackers, defenders);
		for (Combat c : combats) {

			//Missile fire if possible.
			final List<String> modifiersForMissile;
			if (modifiers != null) {
				modifiersForMissile = new ArrayList<>(modifiers);
			} else {
				modifiersForMissile = new ArrayList<>();
			}
			modifiersForMissile.add("norf");
			final Unit mainDefender = c.getMainDefender();
			for (Unit attacker : c.getAttackers()) {
				if (attacker.getMissile() != MissileType.NONE
						&& attacker.getStatus().missileStatus != MissileStatus.NO) {
					missileFire.execute(Collections.singletonList(attacker), Collections.singletonList(mainDefender),
							modifiersForMissile);
				}
			}
			//Reaction fire
			final Unit mainAttacker = c.getMainAttacker();
			for (Unit defender : c.getDefenders()) {
				if (defender.getMissile() != MissileType.NONE
						&& defender.getStatus().missileStatus != MissileStatus.NO) {
					missileFire.execute(Collections.singletonList(defender), Collections.singletonList(mainAttacker),
							modifiersForMissile);
				}
			}

			if (c.isOver()) {
				continue;
			}

			//If all units eliminated, stop combat.

			//Pre shock TQ check
			//Attacker checks
			final boolean ferociousBarbarians =
					mainAttacker.getKind() == UnitKind.BI && getBooleanModifier(modifiers, CommandModifier.fury);
			boolean allDefendersRouted = c.getDefenders().stream().allMatch(u -> u.getState() == UnitState.ROUTED);

			Map<Unit, Integer> diffPerUnit = new HashMap<>();

			for (Unit attacker : c.getAttackers()) {
				if (allDefendersRouted) {
					diffPerUnit.put(attacker, 0);
					continue;
				}
				int r = dice.roll();
				int shift = 0;
				List<String> diceModifiers = new ArrayList<>();
				if (ferociousBarbarians && attacker.getKind() == UnitKind.BI) {
					shift = -1;
					diceModifiers.add("-1 for barbarian ferocity");
				}
				int diff = Math.max(0, r - attacker.getTq() + shift);

				logPreshock(attacker, r, diceModifiers);

				diffPerUnit.put(attacker, diff);
			}
			for (Unit defender : c.getDefenders()) {
				int r = dice.roll();
				int shift = 0;

				List<String> diceModifiers = new ArrayList<>();
				if (ferociousBarbarians) {
					shift = 2;
					diceModifiers.add("+2 for barbarian ferocity");
				}

				logPreshock(defender, r, diceModifiers);

				int diff = Math.max(0, r - defender.getTq() + shift);
				diffPerUnit.put(defender, diff);

			}

			int attackerDiffOnRouting = diffPerUnit.get(mainAttacker) + mainAttacker.getHits() - mainAttacker.getTq();
			int defenderDiffOnRouting = diffPerUnit.get(mainDefender) + mainDefender.getHits() - mainDefender.getTq();

			if (attackerDiffOnRouting >= 0 && defenderDiffOnRouting >= 0) { //Both units would collapose
				if (defenderDiffOnRouting >= attackerDiffOnRouting) { //Defender only is routing
					int impact = -1 - mainAttacker.getHits() + mainAttacker.getTq();
					diffPerUnit.put(mainAttacker, impact);
				} else {
					int impact = -1 - mainDefender.getHits() + mainDefender.getTq();
					diffPerUnit.put(mainDefender, impact);
				}

			}
			fight.applyImpactOnUnits(c,diffPerUnit);

			if (c.isOver()) {
				continue;
			}

			//Fight
			fight.execute(c.getAttackers(), c.getDefenders(), modifiersForMissile);

		}
	}

	private void logPreshock(final Unit u, final int roll, final List<String> diceModifiers) {
		final String rollModifiers = Helper.buildModifiersLog("", diceModifiers);

		String m = String.format("Pre-shock for %s. Dice rolls %d%s! (TQ=%d).", Log.buildStaticDesc(u), roll,
				rollModifiers, u.getTq());
		console.logNL(m);
	}
}
