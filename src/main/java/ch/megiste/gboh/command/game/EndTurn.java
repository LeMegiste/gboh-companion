package ch.megiste.gboh.command.game;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.game.GameStatus;

public class EndTurn extends GameCommand {

	public EndTurn() {
		super("Ends the current turn");
	}

	@Override
	public String getKey() {
		return "END_TURN";
	}

	@Override
	public void execute(final GameStatus gs, final List<String> commandArgs) {
		String val = console.readLine(
				"This command will end the current turn.\nAll rallied units will become depleted.\nDo you want to proceed ?[y/n]\n>>");
		if (!"y".equals(val)) {
			return;
		}
		final List<Unit> ralliedUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.RALLIED).collect(Collectors.toList());
		for (Unit u : ralliedUnits) {
			unitChanger.changeState(u, null, UnitState.DEPLETED, null);
			console.logNL(Log.logUnit(u));
		}
		final Predicate<Unit> forRefill =
				u -> u.getMissile() != MissileType.NONE && u.getMissileStatus() != MissileStatus.FULL && u.getState()!=UnitState.ROUTED && u.getState()!=UnitState.ELIMINATED;
		final List<String> candidatesForRefill =
				gs.getAllUnits().stream().filter(forRefill).map(Unit::getUnitCode).collect(Collectors.toList());
		final String allRefills = Joiner.on(",").join(candidatesForRefill);
		String query = console.readLine(
				"The following units are candidate to refill their missiles. Please enter the codes of the valid ones, or ALL. "
						+ allRefills+"\n>>");
		final List<Unit> refill;
		if ("ALL".equals(query)) {
			refill = gs.getAllUnits().stream().filter(forRefill).collect(Collectors.toList());
		} else {
			refill = gs.findUnits(query).foundUnits;
		}
		for (Unit u : refill) {
			unitChanger.changeState(u, null, null, MissileStatus.FULL);
		}

		console.logNL("---Eliminated units");
		final List<Unit> eliminatedUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.ELIMINATED).collect(Collectors.toList());
		for (Unit u : eliminatedUnits) {
			console.logNL(Log.logUnit(u));
		}

		console.logNL("---Routing units");
		final List<Unit> routingUnits =
				gs.getAllUnits().stream().filter(u -> u.getState() == UnitState.ROUTED).collect(Collectors.toList());
		for (Unit u : routingUnits) {
			console.logNL(Log.logUnit(u));
		}
		console.logNL(String.format("%s - rout points: %d",gs.getArmy1().getName(),gs.computeArmy1RoutPoints()));
		console.logNL(String.format("%s - rout points: %d",gs.getArmy2().getName(),gs.computeArmy1RoutPoints()));




		gs.endOfTurn();
		gs.persistGame(true);





		return;
	}
}
