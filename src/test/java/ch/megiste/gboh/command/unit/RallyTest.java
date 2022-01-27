package ch.megiste.gboh.command.unit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class RallyTest {
	Rally r = new Rally();
	private UnitChanger unitChanger;
	private Dice dice;

	@Before
	public void init() {
		final Console console = Mockito.mock(Console.class);

		final GameStatus gs = Mockito.mock(GameStatus.class);
		when(gs.areLeadersUsed()).thenReturn(true);
		final Leader l = new Leader("BOS","BOSS",4,6,true);
		when(gs.getCurrentLeader()).thenReturn(l);
		unitChanger = Mockito.spy(new UnitChanger(console, gs));
		dice = Mockito.mock(Dice.class);
		r.setUnitChanger(unitChanger);
		r.setConsole(console);
		r.setDice(dice);
		r.setGameStatus(gs);
	}

	@Test
	public void rallySuccess1() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 8;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(3,1);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.RALLIED, lg1.getStatus().state);
		Assert.assertEquals(1, lg1.getStatus().hits);

	}

	@Test
	public void rallySuccess2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 12;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(3,3);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.RALLIED, lg1.getStatus().state);
		Assert.assertEquals(2, lg1.getStatus().hits);

	}

	@Test
	public void rallyFailure1() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 8;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(7);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.ROUTED, lg1.getStatus().state);
		Assert.assertEquals(8, lg1.getStatus().hits);

	}

	@Test
	public void rallyFailure2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 8;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(8,1);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.ELIMINATED, lg1.getStatus().state);


	}

	@Test
	public void rallyPhalanx() {
		Unit ph1 = new Unit(UnitKind.PH, SubClass.Ha, "Macedonian", "1", "PH1", 7, 10, MissileType.NONE);
		ph1.getStatus().hits = 8;
		ph1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(2,1);
		r.execute(Collections.singletonList(ph1), null, null);

		Assert.assertEquals(UnitState.RALLIED, ph1.getStatus().state);
		Assert.assertEquals(1, ph1.getStatus().hits);

	}

	@Test
	public void rallyPhalanxFailure() {
		Unit ph1 = new Unit(UnitKind.PH, SubClass.Ha, "Macedonian", "1", "PH1", 7, 10, MissileType.NONE);
		ph1.getStatus().hits = 8;
		ph1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(6,1);
		r.execute(Collections.singletonList(ph1), null, null);

		Assert.assertEquals(UnitState.ELIMINATED, ph1.getStatus().state);

	}

}