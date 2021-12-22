package ch.megiste.gboh.game;

import static ch.megiste.gboh.army.UnitStatus.NONE;
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
				final UnitCategory unitCategory = u.getKind().getUnitCategory();
				if (u.getKind() == UnitKind.SK || unitCategory == UnitCategory.Chariots
						|| unitCategory == UnitCategory.Elephants) {
					if (unitCategory == UnitCategory.Elephants) {
						console.logFormat("%s is RAMPAGING!", Log.lotUnit(u));
						handleRampage(u);
					}
					newState = ELIMINATED;
				}
			} else if (newHits < 0) {
				newHits = 0;
				newState = null;
			} else {
				newState = null;
			}
		}
		changeState(u, newHits, newState);
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

	private void changeStateInternal(final Unit u, final Integer hits, final UnitState state,
			final Map<MissileType, MissileStatus> missileStatus, final String stackedOn, final String stackedUnder,
			final boolean undo) {
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
		if (missileStatus != null && !MissileStatusHelper.missileStatusToString(missileStatus)
				.equals(status.missileStatus)) {
			if (missileStatus.size() > 0) {
				statusList.add("is missile " + missileStatus.values().iterator().next());
			}
			status.missileStatus = MissileStatusHelper.missileStatusToString(missileStatus);
		} else if (u.getMainMissile() == MissileType.NONE) {
			status.missileStatus = "";
		}

		boolean stateChanged = false;
		if (state != null && state != before.state) {
			stateChanged = true;
			if (!undo) {
				Preconditions.checkArgument(stateTransitions.get(state).contains(before.state),
						"Unit must be in state " + Joiner.on(" or ").join(stateTransitions.get(state))
								+ " before passing to state: " + state);
			}
			statusList.add("is " + state.name());
			status.state = state;
			if (state == RALLIED) {
				status.depleted = true;
			}
		}
		String changeMessage = Joiner.on(" It ").join(statusList);
		if (!Strings.isNullOrEmpty(changeMessage.trim())) {
			console.logNL(Log.lotUnit(u) + " " + changeMessage);
		}
		if (stackedOn != null) {
			status.stackOn = stackedOn;
		}
		if (stackedUnder != null) {
			status.stackUnder = stackedUnder;
		}

		if (!before.equals(u.getStatus())) {
			gameStatus.recordChange(before, u);
		}
		if (stateChanged && state == ROUTED && u.isStacked()) {
			final Unit stackedUnit = gameStatus.getStackedUnit(u);
			Preconditions.checkNotNull(stackedUnit);
			if (stackedUnit.getState() != ROUTED && stackedUnit.getState() != ELIMINATED) {
				changeStateInternal(stackedUnit, stackedUnit.getOriginalTq(), ROUTED, null, null, null, undo);
			}
		}
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
