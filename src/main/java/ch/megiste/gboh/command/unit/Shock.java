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
import ch.megiste.gboh.command.unit.Fight.Position;
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
		Position position = fight.computePosition(modifiers);
		final List<Combat> combats = fight.buildCombats(attackers, defenders,position);
		for (Combat c : combats) {


			boolean noMissiles = getBooleanModifier(modifiers, CommandModifier.nmf);
			Unit mainAttacker = c.getMainAttacker();
			Unit mainDefender = c.getMainDefender();

			if (!noMissiles) {
				//Missile fire if possible.
				final List<String> modifiersForMissile;
				if (modifiers != null) {
					modifiersForMissile = new ArrayList<>(modifiers);
				} else {
					modifiersForMissile = new ArrayList<>();
				}
				modifiersForMissile.add("norf");
				for (Unit attacker : c.getAttackers()) {
					if (attacker.getMissile() != MissileType.NONE
							&& attacker.getStatus().missileStatus != MissileStatus.NO) {
						missileFire
								.execute(Collections.singletonList(attacker), Collections.singletonList(mainDefender),
										modifiersForMissile);
					}
				}
				//Reaction fire
				for (Unit defender : c.getDefenders()) {

					if (position == Position.BACK) {
						continue;
					}
					if (position == Position.FLANK && attackers.size() == 1) {
						continue;
					}
					Unit reactionFireTarget;
					if (position == Position.FLANK) {
						reactionFireTarget = attackers.get(1);
					} else {
						reactionFireTarget = attackers.get(0);
					}

					if (defender.getMissile() != MissileType.NONE
							&& defender.getStatus().missileStatus != MissileStatus.NO) {
						missileFire.execute(Collections.singletonList(defender),
								Collections.singletonList(reactionFireTarget), modifiersForMissile);
					}
				}
			}

			if (c.isOver()) {
				continue;
			}

			mainAttacker = c.getMainAttacker();
			mainDefender = c.getMainDefender();

			//If all units eliminated, stop combat.

			//Pre shock TQ check
			//Attacker checks
			final boolean ferociousBarbarians =
					mainAttacker.getKind() == UnitKind.BI && getBooleanModifier(modifiers, CommandModifier.fury);
			boolean allDefendersRouted = c.getDefenders().stream().allMatch(u -> u.getState() == UnitState.ROUTED);

			Map<Unit, Integer> diffPerUnit = new HashMap<>();

			boolean noTqCheckForAttacker = mainDefender.getKind() == UnitKind.SK || allDefendersRouted;
			boolean noTqCheckForDefender =
					mainAttacker.getKind() == UnitKind.LI && (mainDefender.getKind() == UnitKind.LG
							|| mainDefender.getKind() == UnitKind.PH || mainDefender.getKind() == UnitKind.HI);

			for (Unit attacker : c.getAttackers()) {
				int diff =
						preshockTQCheckForAttacker(c, ferociousBarbarians, noTqCheckForAttacker, attacker);
				diffPerUnit.put(attacker, diff);
				final Unit stackedUnit = c.getStackedUnit(attacker);
				if (diff > 0 && stackedUnit != null) {
					int diffStackOn =
							preshockTQCheckForAttacker(c, ferociousBarbarians, noTqCheckForAttacker,
									stackedUnit);
					diffPerUnit.put(stackedUnit, diffStackOn);

				}
			}
			for (Unit defender : c.getDefenders()) {
				int diff =
						preshockTQCheckForDefender(c, ferociousBarbarians, noTqCheckForDefender, defender);
				diffPerUnit.put(defender, diff);

				final Unit stackedUnit = c.getStackedUnit(defender);
				if (diff > 0 && stackedUnit != null) {
					int diffStackOn =
							preshockTQCheckForAttacker(c, ferociousBarbarians, noTqCheckForDefender,
									stackedUnit);
					diffPerUnit.put(stackedUnit, diffStackOn);

				}

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
			fight.applyImpactOnUnits(diffPerUnit);

			if (c.isOver()) {
				continue;
			}

			//Fight
			List<String> modifiersForFight;
			if (modifiers == null) {
				modifiersForFight = new ArrayList<>();
			} else {
				modifiersForFight = new ArrayList<>(modifiers);
			}
			modifiersForFight.add("m");
			fight.fightCombat(modifiers,position,c);

		}
	}

	protected int preshockTQCheckForDefender(final Combat c, final boolean ferociousBarbarians,
			final boolean noTqCheckForDefender, final Unit defender) {
		Unit mainAttacker = c.getMainAttacker();
		if (noTqCheckForDefender) {

			return 0;
		}
		int r = dice.roll();
		int shift = 0;

		List<String> diceModifiers = new ArrayList<>();
		if (ferociousBarbarians) {
			shift = 2;
			diceModifiers.add("+2 for barbarian ferocity");
		}
		if (mainAttacker.getKind() == UnitKind.CH) {
			shift = 1;
			diceModifiers.add("+1 because attacked by chariots");
		}
		if (mainAttacker.getKind() == UnitKind.EL) {
			shift = 1;
			diceModifiers.add("+1 because attacked by elephants");
		}
		if (mainAttacker.getKind() == UnitKind.PH && c.isStacked(mainAttacker)) {
			shift = +1;
			diceModifiers.add("+1 because attacked by stacked phalanxes");
		}
		if (c.isStacked(defender)) {
			shift = -1;
			diceModifiers.add("-1 because defender are stacked phalanxes");
		}

		logPreshock(defender, r, diceModifiers);

		int diff = Math.max(0, r - defender.getTq() + shift);
		diff = specialHoldingPossibilityForBigUnits(defender, diff, false);

		return diff;
	}

	protected int preshockTQCheckForAttacker(final Combat c, final boolean ferociousBarbarians,
			final boolean noTqCheckForAttacker, final Unit attacker) {
		Unit mainDefender = c.getMainDefender();
		if (noTqCheckForAttacker) {
			return 0;
		}
		int r = dice.roll();
		int shift = 0;
		List<String> diceModifiers = new ArrayList<>();
		if (ferociousBarbarians && attacker.getKind() == UnitKind.BI) {
			shift = -1;
			diceModifiers.add("-1 for barbarian ferocity");
		}
		if (attacker.getKind() == UnitKind.EL && (mainDefender.getKind() == UnitKind.HI
				|| mainDefender.getKind() == UnitKind.PH)) {
			shift++;
			diceModifiers.add("+1 for elephants against pikes");
		}

		int diff = Math.max(0, r - attacker.getTq() + shift);

		logPreshock(attacker, r, diceModifiers);
		diff = specialHoldingPossibilityForBigUnits(attacker, diff, true);

		return diff;
	}

	protected int specialHoldingPossibilityForBigUnits(final Unit unit, int diff, final boolean isAttacker) {
		if (unit.getSize() >= 10 && unit.getHits() + diff >= unit.getTq()) {

			int excess = unit.getHits() + diff - unit.getTq();
			int d = dice.roll();
			String modif = String.format("+%d for hits above TQ", excess);
			String modif2;
			int mod = excess;
			if (isAttacker) {
				modif2 = ", +3 because unit is attacking";
				mod += 3;
			} else {
				modif2 = "";
			}

			String result;
			if (d + mod > unit.getTq()) {
				result = "unit routs anyhow";
			} else {
				result = "unit holds firm.";
				diff = unit.getTq() - 1 - unit.getHits();
			}
			console.logNL(
					String.format("Double sized units tries to hold. Dice rolls [%d], %s%s. %s", d, modif, modif2, result));

		}
		return diff;
	}

	private void logPreshock(final Unit u, final int roll, final List<String> diceModifiers) {
		final String rollModifiers = Helper.buildModifiersLog("", diceModifiers);

		String m = String.format("Pre-shock for %s. Dice rolls [%d]%s! (TQ=%d).", Log.lotUnit(u), roll,
				rollModifiers, u.getTq());
		console.logNL(m);
	}
}
