package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;

import ch.megiste.gboh.army.Combat;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.CommandModifier;
import ch.megiste.gboh.util.Helper;

public class Fight extends UnitCommand {

	private Map<Integer, Map<Integer, Integer>> attackerImpactResults;
	private Map<Integer, Map<Integer, Integer>> defenderImpactResults;

	static enum Superiority {
		AS(3), NONE(2), DS(1), DSp(1);

		final int order;

		Superiority(final int order) {
			this.order = order;
		}
	}

	static enum Position {
		FRONT, FLANK, BACK;
	}

	private Map<UnitKind, Map<UnitKind, Superiority>> superiorities = new HashMap<>();
	private Map<UnitKind, Map<UnitKind, Integer>> columnsFront = new HashMap<>();
	private Map<UnitKind, Map<UnitKind, Integer>> columnsFlank = new HashMap<>();
	private Map<UnitKind, Map<UnitKind, Integer>> columnsBack = new HashMap<>();

	public Fight() {
		super("Fight! Resolves the shock combat between attackers and defenders");


		Helper helper = new Helper();
		buildSuperiorities(helper);

		buildColumns(helper, columnsFront, "columns_front.txt");
		buildColumns(helper, columnsFlank, "columns_flank.txt");
		buildColumns(helper, columnsBack, "columns_back.txt");

		buildImpacts(helper);

	}

	private void buildColumns(final Helper helper, Map<UnitKind, Map<UnitKind, Integer>> columns, String fileName) {
		final List<CSVRecord> records = helper.getCsvFromResource(fileName);

		for (UnitKind attacker : UnitKind.values()) {
			Map<UnitKind, Integer> superiorityMap = new HashMap<>();
			columns.put(attacker, superiorityMap);
			for (UnitKind defender : UnitKind.values()) {
				final Optional<CSVRecord> optSource = records.stream().filter(r -> {
					String defName = defender.name();
					if (defName == "RC") {
						defName = "HC";
					}
					return r.get("Source").equals(defName);
				}).findFirst();
				if (!optSource.isPresent()) {
					throw new RuntimeException("Unable to find defender:" + defender.name());
				}
				CSVRecord rec = optSource.get();

				String attackerName = attacker.name();
				if (attackerName.equals("RC")) {
					attackerName = "HC";
				}
				if (attacker == UnitKind.SK) {
					continue;
				}
				String strSup = rec.get(attackerName);

				superiorityMap.put(defender, Integer.parseInt(strSup));
			}
		}
	}

	private void buildSuperiorities(final Helper helper) {
		final List<CSVRecord> records = helper.getCsvFromResource("superiority.txt");

		for (UnitKind attacker : UnitKind.values()) {
			Map<UnitKind, Superiority> superiorityMap = new HashMap<>();
			superiorities.put(attacker, superiorityMap);
			for (UnitKind defender : UnitKind.values()) {
				CSVRecord rec = records.stream().filter(r -> r.get("Source").equals(defender.name())).findFirst().get();

				Superiority sup = Superiority.NONE;
				if (rec.isSet(attacker.name())) {
					String strSup = rec.get(attacker.name());

					if (strSup != null && !strSup.isEmpty()) {
						sup = Superiority.valueOf(strSup);
					}
				}
				superiorityMap.put(defender, sup);
			}
		}
	}

	private void buildImpacts(final Helper helper) {

		attackerImpactResults = computeImpacts(helper, "attacker_impact.txt");
		defenderImpactResults = computeImpacts(helper, "defender_impact.txt");

	}

	private Map<Integer, Map<Integer, Integer>> computeImpacts(final Helper helper, final String fileName) {
		final List<CSVRecord> records = helper.getCsvFromResource(fileName);

		Map<Integer, Map<Integer, Integer>> results = new HashMap<>();

		for (int d = 0; d <= 9; d++) {
			final String diceColumn = "" + d;
			final Optional<CSVRecord> optRec =
					records.stream().filter(r -> r.get("Dice").equals(diceColumn)).findFirst();
			if (!optRec.isPresent()) {
				throw new RuntimeException("Unable to find record for dice: " + d);
			}
			results.put(d, new HashMap<>());

			for (int c = 1; c <= 13; c++) {
				int impact = Integer.parseInt(optRec.get().get("" + c));
				results.get(d).put(c, impact);
			}
		}
		return results;
	}

	@Override
	public String getKey() {
		return "F";
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {

		List<Combat> combats = buildCombats(attackers, defenders);
		boolean flank = getBooleanModifier(modifiers, CommandModifier.f);
		boolean back = getBooleanModifier(modifiers, CommandModifier.b);
		Position position;
		if (back) {
			position = Position.BACK;
		} else if (flank) {
			position = Position.FLANK;
		} else {
			position = Position.FRONT;
		}

		for (Combat c : combats) {
			Superiority sup;
			final Unit mainAttacker = c.getMainAttacker();
			final Unit mainDefender = c.getMainDefender();
			if (position != Position.FRONT) {
				sup = Superiority.AS;
				console.logNL(String.format("%s is AS for better position.", Log.buildStaticDesc(mainAttacker)));
			} else {

				sup = findSuperiority(mainAttacker, mainDefender);
				if (sup == Superiority.AS) {
					console.logNL(
							String.format("%s is AS for better weapon system.", Log.buildStaticDesc(mainAttacker)));
				} else if (sup == Superiority.DS) {
					console.logNL(
							String.format("%s is DS for better weapon system.", Log.buildStaticDesc(mainDefender)));
				}
			}

			final UnitKind attackerKind = mainAttacker.getKind();
			final UnitKind defenderKind = mainDefender.getKind();

			final int originalColumn = computeColumnFromTableAndPosition(position, attackerKind, defenderKind);
			int column = originalColumn;
			if (column == 0) {
				console.logNL("Impossible fight." + attackerKind + " against " + defenderKind);
				return;
			}

			//Adapt with size ratio

			List<String> colModifiers = new ArrayList<>();

			int sumSizeAttackers = c.getAttackers().stream().mapToInt(u -> u.getSize()).sum();
			int sumSizeDefenders = c.getDefenders().stream().mapToInt(u -> u.getSize()).sum();

			int columnShift = computeColumnShift(sumSizeAttackers, sumSizeDefenders);
			if (columnShift != 0) {
				colModifiers.add(String
						.format("%d due to size ratio %d/%d", columnShift, sumSizeAttackers, sumSizeDefenders));
			}

			if (mainAttacker.getState() == UnitState.DEPLETED) {
				columnShift--;
				colModifiers.add("-1 attacket depleted");
			}
			if (mainDefender.getState() == UnitState.DEPLETED) {
				columnShift++;
				colModifiers.add("+1 defender depleted");
			}
			int manualColumShift = getIntModifier(modifiers, CommandModifier.cs, 0);
			if (manualColumShift != 0) {
				columnShift += manualColumShift;
				colModifiers.add(String.format("%d additional", manualColumShift));
			}


			column += columnShift;
			if (column > 13) {
				column = 13;
			} else if (column < 1) {
				column = 1;
			}

			//
			List<String> rollModifiers = new ArrayList<>();

			int leaderShift = getIntModifier(modifiers, CommandModifier.ls, 0);
			if (leaderShift != 0) {
				rollModifiers.add("" + leaderShift + " for leader influence");
			}
			//dice roll
			int originalRoll = dice.roll();
			int r = originalRoll;
			r += leaderShift;
			if (r > 9) {
				r = 9;
			} else if (r < 0) {
				r = 0;
			}

			//
			int attackerImpact = attackerImpactResults.get(r).get(column);
			int defenderImpact = defenderImpactResults.get(r).get(column);

			if (sup == Superiority.AS) {
				defenderImpact = 2 * defenderImpact;
			} else if (sup == Superiority.DS) {
				attackerImpact = 3 * attackerImpact;
			}
			String rollModifiersString = Helper.buildModifiersLog("", rollModifiers);
			String columnModifiersString = Helper.buildModifiersLog("column " + originalColumn, colModifiers);
			console.logNL(String.format("Shock! Dice rolls %d%s on column %d%s. Result %d/%d", originalRoll,
					rollModifiersString, column, columnModifiersString, defenderImpact, attackerImpact));

			//Repartition of impact
			Map<Unit, Integer> impactPerUnit = computeImpactPerUnit(c, attackerImpact, defenderImpact, position);

			//Apply all changes
			applyImpactOnUnits(c, impactPerUnit);

		}
		//Mark all units as missile no (if relevant) and log them
		for (Unit u : attackers) {
			missileDepletionForUnitsInvolvedInShock(u);
		}
		for (Unit u : defenders) {
			missileDepletionForUnitsInvolvedInShock(u);
		}


	}



	private void missileDepletionForUnitsInvolvedInShock(final Unit u) {
		if (u.getMissile() != MissileType.NONE) {
			unitChanger.changeState(u, null, null, MissileStatus.NO);
		}
	}

	public void applyImpactOnUnits(Combat c, Map<Unit, Integer> diffPerUnits) {
		for (Unit attacker : c.getAttackers()) {

			final Integer d = diffPerUnits.get(attacker);
			if (d > 0) {
				unitChanger.addHits(attacker, d);
			}
		}
		for (Unit defender : c.getDefenders()) {
			final Integer d = diffPerUnits.get(defender);
			if (d > 0) {
				unitChanger.addHits(defender, diffPerUnits.get(defender));
			}
		}
	}

	protected Map<Unit, Integer> dispatchImpact(final int impactToDispatch, List<Unit> impactedUnits) {
		Map<Unit, Integer> impactPerUnit = new HashMap<>();

		int impactDispatched = impactToDispatch / impactedUnits.size();
		int remainder = impactToDispatch % impactedUnits.size();

		for (Unit u : impactedUnits) {
			int impact = impactDispatched;
			if (remainder > 0) {
				impact++;
				remainder--;
			}
			impactPerUnit.put(u, impact);
		}

		return impactPerUnit;
	}

	protected Map<Unit, Integer> computeImpactPerUnit(final Combat c, final int attackerImpact, int defenderImpact,
			final Position position) {
		boolean attackerCollapses = false;
		boolean defenderCollapses = false;

		Map<Unit, Integer> impactPerUnit = new HashMap<>();
		impactPerUnit.putAll(dispatchImpact(attackerImpact, c.getAttackers()));
		impactPerUnit.putAll(dispatchImpact(defenderImpact, c.getDefenders()));

		Unit mainAttacker = c.getMainAttacker();
		Unit mainDefender = c.getMainDefender();

		if (mainAttacker.getHits() + impactPerUnit.get(mainAttacker) >= mainAttacker.getTq()) {
			attackerCollapses = true;
		}
		if (mainDefender.getHits() + impactPerUnit.get(mainDefender) >= mainDefender.getTq()) {
			defenderCollapses = true;
		}

		int diffAttacker = mainAttacker.getHits() + impactPerUnit.get(mainAttacker) - mainAttacker.getTq();
		int diffDefender = mainDefender.getHits() + impactPerUnit.get(mainDefender) - mainDefender.getTq();

		if (attackerCollapses && defenderCollapses) {

			if (diffDefender >= diffAttacker) {//benefits the attacker
				int impactMainAttacker = Math.max(0, mainAttacker.getTq() - 1 - mainAttacker.getHits());
				impactPerUnit.put(mainAttacker, impactMainAttacker);
			} else {
				defenderImpact = Math.max(0, mainDefender.getTq() - 1 - mainDefender.getHits());
				impactPerUnit.put(mainDefender, defenderImpact);
			}
		} else if (!attackerCollapses && !defenderCollapses) {
			//Post collapse check

			if (diffDefender == -1) {
				defenderCollapses = nearCollapseCheck(impactPerUnit, mainDefender);

			}
			if (diffAttacker == -1 && position == Position.FRONT && !defenderCollapses) {
				nearCollapseCheck(impactPerUnit, mainAttacker);
			}
		}
		return impactPerUnit;
	}

	protected boolean nearCollapseCheck(final Map<Unit, Integer> impactPerUnit, final Unit u) {
		final int defenderImpact;
		int postCollaposeRoll = dice.roll();
		if (postCollaposeRoll <= u.getTq()) {
			defenderImpact = Math.max(0, u.getTq() - 2 - u.getHits());
			impactPerUnit.put(u, defenderImpact);
			String msg = String.format("%s is nearly collapsing. Dice rolls: %d. Holds firm!", Log.buildStaticDesc(u),
					postCollaposeRoll);
			console.logNL(msg);
			return false;

		} else {
			defenderImpact = impactPerUnit.get(u) + 2;
			impactPerUnit.put(u, defenderImpact);
			String msg = String.format("%s is nearly collapsing. Dice rolls: %d. collapses!", Log.buildStaticDesc(u),
					postCollaposeRoll);
			console.logNL(msg);
			return true;
		}
	}

	int computeColumnShift(final int sumSizeAttackers, final int sizeDef) {
		int columnShift = 0;
		if (sumSizeAttackers > sizeDef) {
			int ratio = sumSizeAttackers / sizeDef;
			columnShift = ratio - 1;
		} else if (sizeDef > sumSizeAttackers) {
			int ratio = sizeDef / sumSizeAttackers;
			columnShift = -1 * (ratio - 1);
		}

		return columnShift;
	}

	private int computeColumnFromTableAndPosition(final Position position, final UnitKind attackerKind,
			final UnitKind defenderKind) {
		int column;
		switch (position) {

		case FLANK:
			column = columnsFlank.get(attackerKind).get(defenderKind);
			break;
		case BACK:
			column = columnsBack.get(attackerKind).get(defenderKind);
			break;
		case FRONT:
		default:
			column = columnsFront.get(attackerKind).get(defenderKind);
			break;
		}
		return column;
	}

	protected List<Combat> buildCombats(final List<Unit> sourceUnits, final List<Unit> destinationUnits) {
		List<Combat> combats = new ArrayList<>();
		Combat c = new Combat(sourceUnits, destinationUnits);
		combats.add(c);
		return combats;
	}

	Superiority findSuperiority(Unit attacker, Unit defender) {
		final Superiority tmpSup = superiorities.get(attacker.getKind()).get(defender.getKind());
		if (tmpSup != Superiority.DSp) {
			return tmpSup;
		} else { //In case of DSp
			if (defender.getMissile() != MissileType.NONE && defender.getMissileStatus() != MissileStatus.NO) {
				return Superiority.DS;
			} else {
				return Superiority.NONE;
			}
		}
	}
}
