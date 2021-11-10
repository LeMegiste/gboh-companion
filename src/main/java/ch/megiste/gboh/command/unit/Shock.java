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

			boolean noMissiles = getBooleanModifier(modifiers, CommandModifier.nmf);
			int stackedAttackers = getIntModifier(modifiers, CommandModifier.sa,-1);
			int stackedDefenders = getIntModifier(modifiers, CommandModifier.sb,-1);
			final Unit mainAttacker = c.getMainAttacker();
			final Unit mainDefender = c.getMainDefender();

			if(!noMissiles){
				//Missile fire if possible.
				final List<String> modifiersForMissile;
				if (modifiers != null) {
					modifiersForMissile = new ArrayList<>(modifiers);
				} else {
					modifiersForMissile = new ArrayList<>();
				}
				modifiersForMissile.add("norf");
				int count=0;
				for (Unit attacker : c.getAttackers()) {
					if(count==stackedAttackers){
						continue;
					}
					count++;	
					if (attacker.getMissile() != MissileType.NONE
							&& attacker.getStatus().missileStatus != MissileStatus.NO) {
						missileFire.execute(Collections.singletonList(attacker), Collections.singletonList(mainDefender),
								modifiersForMissile);
					}
				}
				count=0;
				//Reaction fire
				for (Unit defender : c.getDefenders()) {
					if(count==stackedDefenders){
						continue;
					}
					count++;
					if (defender.getMissile() != MissileType.NONE
							&& defender.getStatus().missileStatus != MissileStatus.NO) {
						missileFire.execute(Collections.singletonList(defender), Collections.singletonList(mainAttacker),
								modifiersForMissile);
					}
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

			boolean noTqCheckForAttacker = mainDefender.getKind() == UnitKind.SK || allDefendersRouted;
			boolean noTqCheckForDefender =
					mainAttacker.getKind() == UnitKind.LI && (mainDefender.getKind() == UnitKind.LG
							|| mainDefender.getKind() == UnitKind.PH || mainDefender.getKind() == UnitKind.HI
					);

			int count=0;
			for (Unit attacker : c.getAttackers()) {
				diffPerUnit.put(attacker, 0);
				if(count==stackedAttackers){
						continue;
				}
				count++;
				if (noTqCheckForAttacker) {
				
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
			count=0;
			for (Unit defender : c.getDefenders()) {
				diffPerUnit.put(defender, 0);
				if(count==stackedDefenders){
						continue;
				}
				count++;
				if(noTqCheckForDefender){
					
					continue;
				}
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
			List<String> modifiersForFight;
			if(modifiers==null){
				modifiersForFight =new ArrayList<>();
			} else {
				modifiersForFight =new ArrayList<>(modifiers);
			}
			modifiersForFight.add("m");
			fight.execute(c.getAttackers(), c.getDefenders(), modifiersForFight);

		}
	}

	private void logPreshock(final Unit u, final int roll, final List<String> diceModifiers) {
		final String rollModifiers = Helper.buildModifiersLog("", diceModifiers);

		String m = String.format("Pre-shock for %s. Dice rolls %d%s! (TQ=%d).", Log.buildStaticDesc(u), roll,
				rollModifiers, u.getTq());
		console.logNL(m);
	}
}
