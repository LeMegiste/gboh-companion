package ch.megiste.gboh.game;

import static ch.megiste.gboh.army.UnitStatus.NONE;
import static ch.megiste.gboh.army.UnitStatus.UnitState.ELIMINATED;
import static ch.megiste.gboh.army.UnitStatus.UnitState.OK;
import static ch.megiste.gboh.army.UnitStatus.UnitState.RALLIED;
import static ch.megiste.gboh.army.UnitStatus.UnitState.ROUTED;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitCategory;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.game.GameStatus.Rules;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;
import ch.megiste.gboh.util.MissileStatusHelper;

public class UnitChanger {
	private static final Set<UnitState> BEFORE_ROUTING = EnumSet.of(OK, RALLIED);
	private static final Set<UnitState> BEFORE_ELIMINATED = EnumSet.of(OK, RALLIED, ROUTED);
	private static final Set<UnitState> BEFORE_RALLIED = EnumSet.of(ROUTED);
	private Map<UnitState, Set<UnitState>> stateTransitions = new HashMap<>();

	{
		stateTransitions.put(RALLIED, BEFORE_RALLIED);
		stateTransitions.put(ROUTED, BEFORE_ROUTING);
		stateTransitions.put(OK, EnumSet.of(ROUTED, UnitState.values()));
		stateTransitions.put(ELIMINATED, BEFORE_ELIMINATED);

	}

	private Console console;

	private GameStatus gameStatus;

	public UnitChanger(final Console console, final GameStatus gameStatus) {
		this.console = console;
		this.gameStatus = gameStatus;
	}

	public void addHits(final Unit u, final int c) {
		int newHits = computeNewHits(u, c);
		StateChange sc = new StateChange(u);
		sc.hits = newHits;
		changeStateInternal(u, sc);

	}

	public int computeNewHits(final Unit u, final int c) {
		final UnitStatus status = u.getStatus();

		int newHits = status.hits + c;
		if (newHits < 0) {
			newHits = 0;
		}
		return newHits;
	}

	private void handleRampage(final Unit u) {
		String unitName = Log.lotUnit(u);
		for (int i = 0; i < 4; i++) {
			final int r = getGameStatus().getDice().roll();
			if (r == 0) {
				console.logFormat("RAMPAGE! Dice rolled [%d]. %s is closing to the nearest friendly unit!", r,
						unitName);
			} else if (r < 7) {
				console.logFormat("RAMPAGE! Dice rolled [%d]. %s is going in direction %d (refer to map)!", r, unitName,
						r);
			} else if (i == 0) {
				console.logFormat("RAMPAGE! Dice rolled [%d]. %s is chasing from the unit causing the rampage!", r,
						unitName);
			} else {
				console.logFormat("RAMPAGE! Dice rolled [%d]. The mahout finally killed the poor %s!", r, unitName);
				break;
			}
		}

	}

	public void changeState(final Unit u, final Integer newHits, final UnitState newState) {
		changeState(u, newHits, newState, null);
	}

	public void missileDepletion(final Unit u, final MissileType missileType) {
		final MissileStatus currentStatus = u.getMissileStatus().get(missileType);
		final MissileStatus newStatus;
		if (currentStatus == MissileStatus.FULL && u.getKind() != UnitKind.LG) {
			newStatus = MissileStatus.LOW;
		} else if (currentStatus == MissileStatus.FULL && u.getKind() == UnitKind.LG) {
			newStatus = MissileStatus.NO;
		} else if (currentStatus == MissileStatus.LOW) {
			newStatus = MissileStatus.NO;
		} else {

			newStatus = u.getMissileStatus().get(missileType);
		}

		final Map<MissileType, MissileStatus> missileStatus = new HashMap<>(u.getMissileStatus());
		missileStatus.put(missileType, newStatus);
		changeState(u, null, null, missileStatus);

	}

	public void changeState(final Unit u, final Integer hits, final UnitState state,
			final Map<MissileType, MissileStatus> missileStatus) {
		changeStateInternal(u, hits, state, missileStatus, null, null, false);
	}

	public static class StateChange {

		private final Unit u;

		public StateChange(final Unit u) {
			this.u = u;
		}

		public Integer hits;
		public UnitState state;
		public Map<MissileType, MissileStatus> missileStatus;
		public String stackedOn;
		public String stackedUnder;
		public boolean undo;
	}

	private void changeStateInternal(final Unit u, final Integer hits, final UnitState state,
			final Map<MissileType, MissileStatus> missileStatus, final String stackedOn, final String stackedUnder,
			final boolean undo) {
		StateChange sc = new StateChange(u);
		sc.hits = hits;
		sc.missileStatus = missileStatus;
		sc.stackedOn = stackedOn;
		sc.state = state;
		sc.stackedUnder = stackedUnder;
		sc.undo = undo;
		changeStateInternal(u, sc);
	}

	private void changeStateInternal(final Unit u, final StateChange sc) {
		final Queue<StateChange> q = new LinkedList<>();
		q.add(sc);
		changeStateInternal(q);
	}

	private void changeStateInternal(Queue<StateChange> q) {

		if (q.isEmpty()) {
			return;
		}

		StateChange sc = q.poll();
		Unit u = sc.u;
		final UnitStatus status = u.getStatus();
		final UnitStatus before = Helper.clone(status);
		List<String> statusList = new ArrayList<>();

		if (sc.hits != null && sc.hits != status.hits) {
			int diff = sc.hits - status.hits;
			if (diff == 1) {
				statusList.add("took 1 hit.");
			} else if (diff > 1) {
				statusList.add("took " + diff + " hits.");
			} else if (diff == -1) {
				statusList.add("was removed 1 hit.");
			} else {
				statusList.add("was removed " + Math.abs(diff) + " hits.");
			}

			if (status.state == ROUTED && diff > 0) {
				StateChange scNext = new StateChange(u);
				scNext.state = ELIMINATED;
				q.add(scNext);
			} else if ((status.state == OK || status.state == RALLIED) && sc.hits >= u.getOriginalTq()) {
				UnitState newState = ROUTED;
				final UnitCategory unitCategory = u.getKind().getUnitCategory();
				if (u.getKind() == UnitKind.SK || unitCategory == UnitCategory.Chariots
						|| unitCategory == UnitCategory.Elephants) {
					if (unitCategory == UnitCategory.Elephants) {
						console.logFormat("%s is RAMPAGING!", Log.lotUnit(u));
						handleRampage(u);
					}
					newState = ELIMINATED;
				}

				StateChange scNext = new StateChange(u);
				scNext.state = newState;
				q.add(scNext);
			}

			if (sc.hits > u.getOriginalTq()) {
				status.hits = u.getOriginalTq();
			} else {
				status.hits = sc.hits;
			}

		}
		if (sc.missileStatus != null && !MissileStatusHelper.missileStatusToString(sc.missileStatus)
				.equals(status.missileStatus)) {
			if (sc.missileStatus.size() > 0) {
				statusList.add("is missile " + sc.missileStatus.values().iterator().next());
			}
			status.missileStatus = MissileStatusHelper.missileStatusToString(sc.missileStatus);
		} else if (u.getMainMissile() == MissileType.NONE) {
			status.missileStatus = "";
		}

		boolean stateChanged = false;
		if (sc.state != null && sc.state != before.state) {
			stateChanged = true;
			if (!sc.undo) {
				Preconditions.checkArgument(stateTransitions.get(sc.state).contains(before.state),
						"Unit must be in state " + Joiner.on(" or ").join(stateTransitions.get(sc.state))
								+ " before passing to state: " + sc.state);
			}
			statusList.add("is " + sc.state.name());
			status.state = sc.state;
			if (sc.state == RALLIED) {
				status.depleted = true;
			}

		}
		String changeMessage = Joiner.on(" It ").join(statusList);
		if (!Strings.isNullOrEmpty(changeMessage.trim())) {
			console.logNL(Log.lotUnit(u) + " " + changeMessage);
		}
		if (sc.stackedOn != null) {
			status.stackOn = sc.stackedOn;
		}
		if (sc.stackedUnder != null) {
			status.stackUnder = sc.stackedUnder;
		}

		if (!before.equals(u.getStatus())) {
			gameStatus.recordChange(before, u);
		}
		if (stateChanged && sc.state == ROUTED && u.isStacked()) {
			final Unit stackedUnit = gameStatus.getStackedUnit(u);
			Preconditions.checkNotNull(stackedUnit);
			if (stackedUnit.getState() != ROUTED && stackedUnit.getState() != ELIMINATED) {
				StateChange scNext1 = new StateChange(stackedUnit);
				scNext1.state = ROUTED;
				q.add(scNext1);

				StateChange scNext2 = new StateChange(u);
				scNext2.stackedOn = NONE;
				scNext2.stackedUnder = NONE;
				q.add(scNext2);

				StateChange scNext3 = new StateChange(stackedUnit);
				scNext3.stackedOn = NONE;
				scNext3.stackedUnder = NONE;
				q.add(scNext3);
			}
		}
		if (!q.isEmpty()) {
			changeStateInternal(q);
		}
	}

	public void applyImpactOnUnits(Map<Unit, Integer> diffPerUnits) {
		LinkedList<StateChange> queue = new LinkedList<>();
		for (Map.Entry<Unit, Integer> e : diffPerUnits.entrySet()) {
			if (e.getValue() > 0) {
				int newHits = computeNewHits(e.getKey(), e.getValue());
				StateChange sc = new StateChange(e.getKey());
				sc.hits = newHits;
				queue.add(sc);

			}
		}
		changeStateInternal(queue);
	}

	public void changeStateForUndo(final String unitCode, final UnitStatus status) {
		final Unit u = gameStatus.findUnitByCode(unitCode);
		changeStateInternal(u, status.hits, status.state,
				MissileStatusHelper.missileStatusFromString(status.missileStatus), status.stackOn, status.stackUnder,
				true);
	}

	public void changeStateNoCheck(final Unit u, final int hits, final UnitState state,
			final Map<MissileType, MissileStatus> missileState) {
		changeStateInternal(u, hits, state, missileState, null, null, true);
	}

	public Rules getCurrentRules() {
		return gameStatus.getRules();
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void stack(final String on, final String under) {
		Unit onUnit = gameStatus.findUnitByCode(on);
		Unit underUnit = gameStatus.findUnitByCode(under);

		changeStateInternal(onUnit, null, null, null, under, null, false);
		changeStateInternal(underUnit, null, null, null, null, on, false);
	}

	public void unStack(final String on, final String under) {
		Unit onUnit = gameStatus.findUnitByCode(on);
		Unit underUnit = gameStatus.findUnitByCode(under);

		changeStateInternal(onUnit, null, null, null, NONE, null, false);
		changeStateInternal(underUnit, null, null, null, null, NONE, false);
	}

	public void eliminated(final Unit u) {
		changeStateInternal(u, null, ELIMINATED, null, null, null, false);
	}
}
