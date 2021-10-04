package ch.megiste.gboh.game;

import static ch.megiste.gboh.army.UnitStatus.UnitState.DEPLETED;
import static ch.megiste.gboh.army.UnitStatus.UnitState.ELIMINATED;
import static ch.megiste.gboh.army.UnitStatus.UnitState.OK;
import static ch.megiste.gboh.army.UnitStatus.UnitState.RALLIED;
import static ch.megiste.gboh.army.UnitStatus.UnitState.ROUTED;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;

public class UnitChanger {
	private static final Set<UnitState> BEFORE_ROUTING = EnumSet.of(DEPLETED, OK, RALLIED);
	private static final Set<UnitState> BEFORE_RALLIED = EnumSet.of(ROUTED);
	private Map<UnitState, Set<UnitState>> stateTransitions = new HashMap<>();

	{
		stateTransitions.put(RALLIED, BEFORE_RALLIED);
		stateTransitions.put(ROUTED, BEFORE_ROUTING);
		stateTransitions.put(OK, EnumSet.of(ROUTED, UnitState.values()));
		stateTransitions.put(ELIMINATED, EnumSet.of(ROUTED));
		stateTransitions.put(DEPLETED, EnumSet.of(RALLIED));

	}

	private Console console;

	private GameStatus gameStatus;

	public UnitChanger(final Console console, final GameStatus gameStatus) {
		this.console = console;
		this.gameStatus = gameStatus;
	}

	public void addHit(Unit u) {
		addHits(u, 1);
	}

	public void addHits(final Unit u, final int c) {
		final UnitStatus status = u.getStatus();
		UnitState newState;
		int newHits;
		if (u.getState() == ROUTED) {
			newState = ELIMINATED;
			newHits = u.getOriginalTq();
		} else {
			newHits = status.hits + c;

			if (newHits >= u.getOriginalTq()) {
				newState = UnitState.ROUTED;
			} else if (newHits < 0) {
				newHits = 0;
				newState = null;
			} else {
				newState = null;
			}
		}
		changeState(u, newHits, newState, null);
	}

	public void missileDepletion(final Unit u) {
		final UnitStatus status = u.getStatus();
		final MissileStatus newStatus;
		if (u.getMissileStatus() == MissileStatus.FULL && u.getKind() != UnitKind.LG) {
			newStatus = MissileStatus.LOW;
		} else if (u.getMissileStatus() == MissileStatus.FULL && u.getKind() == UnitKind.LG) {
			newStatus = MissileStatus.NO;
		} else if (u.getMissileStatus() == MissileStatus.LOW) {
			newStatus = MissileStatus.NO;
		} else {
			newStatus = status.missileStatus;
		}
		changeState(u, null, null, newStatus);

	}

	public void changeState(final Unit u, final Integer hits, final UnitState state,
			final MissileStatus missileStatus) {
		changeStateInternal(u, hits, state, missileStatus, false);
	}

	private void changeStateInternal(final Unit u, final Integer hits, final UnitState state,
			final MissileStatus missileStatus, final boolean undo) {
		final UnitStatus status = u.getStatus();
		final UnitStatus before = Helper.clone(status);
		List<String> statusList = new ArrayList<>();
		if (hits != null && hits != status.hits) {
			int diff = hits - status.hits;
			if (diff == 1) {
				statusList.add("took 1 hit.");
			} else if (diff > 1) {
				statusList.add("took " + diff + " hits.");
			} else if (diff == -1) {
				statusList.add("was removed 1 hit.");
			} else {
				statusList.add("was removed " + Math.abs(diff) + " hits.");
			}

			if (hits > u.getOriginalTq()) {
				status.hits = u.getOriginalTq();
			} else {
				status.hits = hits;
			}

		}
		if (missileStatus != null && missileStatus != status.missileStatus) {
			statusList.add("is missile " + missileStatus);
			status.missileStatus = missileStatus;
		} else if (u.getMissile() == MissileType.NONE) {
			status.missileStatus = MissileStatus.NEVER;
		}

		if (state != null && state != before.state) {
			if (!undo) {
				Preconditions.checkArgument(stateTransitions.get(state).contains(before.state),
						"Unit must be in state " + Joiner.on(" or ").join(stateTransitions.get(state))
								+ " before passing to state: " + state);
			}
			statusList.add("is " + state.name());
			status.state = state;
		}
		String changeMessage = Joiner.on(" It ").join(statusList);
		console.logNL(Log.buildStaticDesc(u) + " " + changeMessage);
		gameStatus.recordChange(before, u);

	}

	public void changeStateForUndo(final String unitCode, final UnitStatus status) {
		final Unit u = gameStatus.findUnitByCode(unitCode);
		changeStateInternal(u, status.hits, status.state, status.missileStatus, true);
	}

	public void changeStateNoCheck(final Unit u, final int hits, final UnitState state,
			final MissileStatus missileState) {
		changeStateInternal(u, hits, state, missileState, true);
	}
}
