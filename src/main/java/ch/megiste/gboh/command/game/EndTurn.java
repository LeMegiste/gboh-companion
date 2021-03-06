package ch.megiste.gboh.command.game;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.util.MissileStatusHelper;

public class EndTurn extends GameCommand {

	public EndTurn() {
		super("Ends the current turn");
	}

	@Override
	public String getKey() {
		return "END_TURN";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		String val = console.readLine(
				"This command will end the current turn.\nAll rallied units will become depleted.\nDo you want to proceed? [y/n]\n>>");
		if (!"y".equals(val)) {
			return;
		}
		final List<Unit> ralliedUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.RALLIED).collect(Collectors.toList());
		for (Unit u : ralliedUnits) {
			unitChanger.changeState(u, null, UnitState.OK);
			console.logNL(Log.logUnitDetailed(u));
		}
		final Predicate<Unit> forRefill =
				u -> u.getMainMissile() != MissileType.NONE && u.getMainMissileStatus() != MissileStatus.FULL && u.getState()!=UnitState.ROUTED && u.getState()!=UnitState.ELIMINATED;
		final List<String> candidatesForRefill =
				gs.getAllUnits().stream().filter(forRefill).map(Unit::getUnitCode).collect(Collectors.toList());
		final String allRefills = Joiner.on(",").join(candidatesForRefill);
		if(candidatesForRefill.size()>0) {
			String query = console.readLine(
					"The following units are candidate to refill their missiles. Please enter the codes of the valid ones, or ALL. "
							+ allRefills + "\n>>");
			final List<Unit> refill;
			if ("ALL".equals(query)) {
				refill = gs.getAllUnits().stream().filter(forRefill).collect(Collectors.toList());
			} else {
				refill = gs.findUnits(query).foundUnits;
			}
			for (Unit u : refill) {
				unitChanger.changeState(u, null, null, MissileStatusHelper.putMissileStatusToState(u, MissileStatus.FULL));
			}
		}
		if(gs.areLeadersUsed()){
			console.logFormat("--Un-finishing leaders");
			for(Leader l : gs.getOrderedLeaders()){
				leadersHandler.flipLeader(l);
			}
		}


		console.logNL("---Eliminated units");
		final List<Unit> eliminatedUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.ELIMINATED).collect(Collectors.toList());
		for (Unit u : eliminatedUnits) {
			console.logNL(Log.logUnitDetailed(u));
		}

		console.logNL("---Routing units");
		final List<Unit> routingUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.ROUTED).collect(Collectors.toList());
		for (Unit u : routingUnits) {
			console.logNL(Log.logUnitDetailed(u));
		}
		console.logNL(gs.logRoutpointsForArmy(gs.getArmy1()));
		if(gs.isArmyRouting(gs.getArmy1())){
			console.logFormat("%s army is ROUTING",gs.getArmy1().getName());
		}
		console.logNL(gs.logRoutpointsForArmy(gs.getArmy2()));
		if(gs.isArmyRouting(gs.getArmy2())){
			console.logFormat("%s army is ROUTING",gs.getArmy2().getName());
		}





		gs.endOfTurn();
		gs.persistGame(true);





		return;
	}
}
