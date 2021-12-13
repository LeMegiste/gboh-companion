package ch.megiste.gboh.command.unit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
		unitChanger = Mockito.spy(new UnitChanger(console, gs));
		dice = Mockito.mock(Dice.class);
		r.setUnitChanger(unitChanger);
		r.setConsole(console);
		r.setDice(dice);
		r.setGameStatus(gs);
	}

	@Test
	public void rally() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 8;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(1);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.RALLIED, lg1.getStatus().state);
		Assert.assertEquals(1, lg1.getStatus().hits);

	}

	@Test
	public void rally2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 12;
		lg1.getStatus().state = UnitState.ROUTED;
		when(dice.roll()).thenReturn(3);
		r.execute(Collections.singletonList(lg1), null, null);

		Assert.assertEquals(UnitState.RALLIED, lg1.getStatus().state);
		Assert.assertEquals(2, lg1.getStatus().hits);

	}
}