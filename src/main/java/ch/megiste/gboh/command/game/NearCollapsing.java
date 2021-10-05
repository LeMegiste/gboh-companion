package ch.megiste.gboh.command.game;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.game.GameStatus;

public class NearCollapsing extends GameCommand {

	public NearCollapsing() {
		super("Lists all units (or of a given army) that 2 or 1 hit from collapsing.");
	}

	@Override
	public String getKey() {
		return "NEAR";
	}

	@Override
	public void execute(final GameStatus gs, final List<String> commandArgs) {
		console.logNL("Listing nearly collapsing units:");
		List<Unit> filteredUnits;
		if (CollectionUtils.isNotEmpty(commandArgs)) {
			filteredUnits = gs.findUnits(commandArgs.get(0)).foundUnits;
		} else {
			filteredUnits = gs.getAllUnits();
		}
		if (filteredUnits == null) {
			console.logNL("No units found.");
			return;
		}
		List<Unit> unitsNearlyCollapsing =
				filteredUnits.stream().filter(this::isNearlyCollapsing).collect(Collectors.toList());
		if (unitsNearlyCollapsing.isEmpty()) {
			console.logNL("No units found.");
		}
		for (Unit u : unitsNearlyCollapsing) {
			console.logNL(Log.logUnit(u));
		}
	}

	public boolean isNearlyCollapsing(Unit u) {
		if (u.getState() == UnitState.ROUTED || u.getState() == UnitState.ELIMINATED) {
			return false;
		}
		return (u.getOriginalTq() - u.getHits()) <= 2;
	}
}
