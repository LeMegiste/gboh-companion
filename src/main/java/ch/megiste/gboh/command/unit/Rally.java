package ch.megiste.gboh.command.unit;

import java.util.Arrays;
import java.util.List;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.leader.LogLeader;

public class Rally extends UnitCommand {

	private List<Integer> rallyValuesWeakUnits = Arrays.asList(1, 1, 2, 2, 2, 2, 2, 2, 3, 3);
	private List<Integer> rallyValuesStrongUnits = Arrays.asList(1, 1, 2, 2, 2, 2, 3, 3, 4, 4);

	public Rally() {
		super("Rallies the current unit");
	}

	@Override
	public String getKey() {
		return "R";
	}

	@Override
	public void execute(final List<Unit> candidatesToRally, final List<Unit> defenders, final List<String> modifiers) {
		for (Unit u : candidatesToRally) {
			if (u.getStatus().state != UnitState.ROUTED) {
				console.logNL("Impossible to rally " + Log.lotUnit(u) + " as it is not in state ROUTED");
				return;
			}

		}
		for (Unit u : candidatesToRally) {

			if (getGameStatus().areLeadersUsed()) {
				int rallyRoll = dice.roll();
				int rallyRollModified = rallyRoll;
				String modifier="";
				if(u.isDepleted()){
					modifier = " +1 because the unit is depleted";
					rallyRollModified++;
				}
				final Leader l = gameStatus.getCurrentLeader();
				if(rallyRollModified<= l.getInitiative()+1){
					console.logFormat("Dice rolled [%d]%s. %s rallied %s!",rallyRoll,modifier, LogLeader.logLeader(l),Log.lotUnit(u));
					rallySuccess(u);
				} else if(rallyRollModified>u.getOriginalTq()){
					unitChanger.eliminated(u);
					console.logFormat("Dice rolled [%d]%s. Rally attempt failed %s is eliminated!",rallyRoll,modifier, Log.lotUnit(u));

				} else {

					console.logFormat("Dice rolled [%d]%s. Rally attempt failed %s is routing further!",rallyRoll,modifier, Log.lotUnit(u));
				}


			} else {
				rallySuccess(u);
			}



		}
		for (Unit u : candidatesToRally) {
			console.logNL(Log.logUnitDetailed(u));
		}
	}

	void rallySuccess(final Unit u) {
		int hits;
		int roll = dice.roll();
		if (u.getOriginalTq() <= 5) {
			hits = rallyValuesWeakUnits.get(roll);
		} else {
			hits = rallyValuesStrongUnits.get(roll);
		}

		unitChanger.changeState(u, hits, UnitState.RALLIED, MissileStatus.NO);
	}
}
