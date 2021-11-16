package ch.megiste.gboh.command.unit;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.asm.tree.MethodNode;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class ShockTest {

	private Fight fc;
	private Dice dice;
	private UnitChanger unitChanger;
	private Shock sh;
	private List<String> logLines = new ArrayList<>();

	@Before
	public void init() {
		sh = new Shock();

		fc = new Fight();
		MissileFire mf = new MissileFire();
		final Console console = Mockito.mock(Console.class);
		fc.setConsole(console);

		final GameStatus gs = Mockito.mock(GameStatus.class);
		unitChanger = Mockito.spy(new UnitChanger(console, gs));
		dice = Mockito.mock(Dice.class);
		fc.setUnitChanger(unitChanger);

		mf.setConsole(console);
		mf.setUnitChanger(unitChanger);
		mf.setDice(dice);

		sh.setConsole(console);
		sh.setUnitChanger(unitChanger);
		sh.setDice(dice);
		sh.setFight(fc);
		sh.setMissileFire(mf);

		fc.setDice(dice);

		doAnswer(inv -> {
			String s = inv.getArgumentAt(0, String.class);
			System.out.println(s);
			logLines.add(s);
			return null;
		}).when(console).logNL(anyString());
	}

	@Test
	public void basicShock() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		//First roll: missile fire 5 (hit)
		//Second roll: TQ check. Roman 9, carth 8
		//Fight. 9
		//Near collapse carth:8
		Mockito.when(dice.roll()).thenReturn(5, 9, 8, 9, 5);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);
		Mockito.verify(unitChanger, Mockito.times(2)).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(1));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(5));
	}

	@Test
	public void bothCollapseDuringPreShock() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 5;
		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 6, 7, MissileType.NONE);
		hi1.getStatus().hits = 5;
		//Roll 9 on missile fire (missed)
		//Roll 9 on pre shock (+2)
		//Roll 9 on pre shock (+4)

		Mockito.when(dice.roll()).thenReturn(9, 9, 9);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(1));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(3));

		//Only the hi collapses.
		Assert.assertEquals(UnitState.ROUTED, hi1.getStatus().state);
		Assert.assertEquals(UnitState.OK, lg1.getStatus().state);
		Assert.assertEquals(6, lg1.getHits());

	}

	@Test
	public void againstRoutedUnit1() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		hi1.getStatus().state = UnitState.ROUTED;

		//First roll: missile fire 5 (hit). Hi collapses.
		//Combat is over
		Mockito.when(dice.roll()).thenReturn(5, 9, 8, 9);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);
		Mockito.verify(unitChanger, Mockito.never()).addHits(eq(lg1), anyInt());
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(1));

		Assert.assertEquals(UnitState.ELIMINATED, hi1.getState());
		Assert.assertEquals(0, lg1.getHits());
	}

	@Test
	public void againstRoutedUnit2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		hi1.getStatus().state = UnitState.ROUTED;

		//First roll: missile fire 9 (missed).
		//Pre shock TQ 3 HI collapses.
		//Combat is over
		Mockito.when(dice.roll()).thenReturn(9, 3);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);

		Mockito.verify(unitChanger).addHits(eq(hi1), eq(2));

		Assert.assertEquals(UnitState.ELIMINATED, hi1.getState());
		Assert.assertEquals(0, lg1.getHits());
	}

	@Test
	public void againstRoutedUnit3() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		hi1.getStatus().state = UnitState.ROUTED;

		//First roll: missile fire 9 (missed).
		//Pre shock TQ 0 HI holds !
		//Dice thrown 6 (legio takes 2, HI eliminated)
		//Combat is over
		Mockito.when(dice.roll()).thenReturn(9, 0, 6);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);

		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(4));

		Assert.assertEquals(UnitState.ELIMINATED, hi1.getState());
		Assert.assertEquals(2, lg1.getHits());
	}

	@Test
	public void againstRoutedUnit4() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit sk1 = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		sk1.getStatus().state = UnitState.ROUTED;

		//First roll: missile fire 9 (missed).
		//Pre shock TQ 3 SK collapses.
		//Combat is over
		Mockito.when(dice.roll()).thenReturn(9, 3, 1);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(sk1), null);

		Mockito.verify(unitChanger).addHits(eq(sk1), eq(2));

		Assert.assertEquals(UnitState.ELIMINATED, sk1.getState());
		Assert.assertEquals(0, lg1.getHits());
	}

	@Test
	public void frontChargeWithReactionFire() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit samnite = new Unit(UnitKind.LG, SubClass.NONE, "Samnite Main", "a", "SMaa", 7, 7, MissileType.NONE);

		//1. roll attacker: missile fire 5 (hit)
		//2. roll defender: missile fire 3 (hit)
		//3. roll TQ check 5 (success)
		//4. roll TQ check 4 (success)
		//5. roll fight. 5 (2/2)
		Mockito.when(dice.roll()).thenReturn(5, 3, 5, 4, 5);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(samnite), null);
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(1));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(samnite), eq(1));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(samnite), eq(2));
	}

	@Test
	public void flankChargeNoReactionFire() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit samnite = new Unit(UnitKind.LG, SubClass.NONE, "Samnite Main", "a", "SMaa", 7, 4, MissileType.NONE);

		//1. roll attacker: missile fire 5 (hit)
		//2. roll TQ check 5 (success)
		//3. roll TQ check 4 (success)
		//4. roll fight. 5 (2/4)
		Mockito.when(dice.roll()).thenReturn(5, 5, 4, 3);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(samnite), Collections.singletonList("f"));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(samnite), eq(1));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(samnite), eq(4));
	}

	@Test
	public void shockWithReactionFireOnly() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		lg1.getStatus().missileStatus = MissileStatus.NO;
		Unit samnite = new Unit(UnitKind.LG, SubClass.NONE, "Samnite Main", "a", "SMaa", 7, 7, MissileType.NONE);

		//1. roll defender: missile fire 3 (hit)
		//2. roll TQ check 5 (success)
		//3. roll TQ check 4 (success)
		//4. roll fight. 5 (2/2)
		Mockito.when(dice.roll()).thenReturn(3, 5, 4, 5);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(samnite), null);
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(1));
		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(lg1), eq(2));

		Mockito.verify(unitChanger, Mockito.times(1)).addHits(eq(samnite), eq(2));
		Assert.assertEquals("After shock combat, missile capable units are missile no", MissileStatus.NO,
				samnite.getMissileStatus());
	}

	@Test
	public void logging() {

		logLines.clear();
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		//First roll: missile fire 5 (hit)
		//Second roll: TQ check. Roman 9, carth 8
		//Fight. 9
		//Near collapse carth:8
		Mockito.when(dice.roll()).thenReturn(5, 8, 3, 7, 5);
		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), Collections.singletonList("cs-1"));

		List<String> expectedLines = Splitter.on("\n").splitToList(
				"Legio XII Hastati a is firing at Mercenary HI 1\n" + "Dice rolls: 5! Mercenary HI 1 is hit! \n"
						+ "Mercenary HI 1 took 1 hit.\n" + "Pre-shock for Legio XII Hastati a. Dice rolls 8! (TQ=7).\n"
						+ "Pre-shock for Mercenary HI 1. Dice rolls 3! (TQ=8).\n" + "Legio XII Hastati a took 1 hit.\n"
						+ "Legio XII Hastati a is AS for better weapon system.\n"
						+ "Shock! Dice rolls 7 on column 4 (column 6, -1 due to size ratio 3/7, -1 additional). Result 4/2\n"
						+ "Legio XII Hastati a took 2 hits.\n" + "Mercenary HI 1 took 4 hits.\n"
						+ "Final status: [Legio XII Hastati a] TQ=7 size=3 - 3 hits\n"
						+ "Final status: [Mercenary HI 1] TQ=8 size=7 - 5 hits");

		for (int i = 0; i < Math.min(expectedLines.size(), logLines.size()); i++) {
			String expected = expectedLines.get(0);
			String log = logLines.get(0);
			Assert.assertEquals(log, expected);
		}

	}

	@Test
	public void loggingWithARout() {

		logLines.clear();
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		hi1.getStatus().hits = 6;
		//First roll: missile fire 5 (hit)
		//Second roll: TQ check. Roman 8, carth 8
		//Fight. 9

		Mockito.when(dice.roll()).thenReturn(5, 8, 3, 7);
		sh.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);

		List<String> expectedLines = Splitter.on("\n").splitToList(
				"Legio XII Hastati a is firing at Mercenary HI 1\n" + "Dice rolls: 5! Mercenary HI 1 is hit! \n"
						+ "Mercenary HI 1 took 1 hit.\n" + "Pre-shock for Legio XII Hastati a. Dice rolls 8! (TQ=7).\n"
						+ "Pre-shock for Mercenary HI 1. Dice rolls 3! (TQ=8).\n" + "Legio XII Hastati a took 1 hit.\n"
						+ "Legio XII Hastati a is AS for better weapon system.\n"
						+ "Shock! Dice rolls 7 on column 5 (column 6, -1 due to size ratio 3/7). Result 4/2\n"
						+ "Legio XII Hastati a took 2 hits.\n" + "Mercenary HI 1 took 4 hits. It is ROUTED\n"
						+ "Legio XII Hastati a is missile NO\n"
						+ "Final status: [Legio XII Hastati a] TQ=7 size=3 - 3 hits is MISSILE NO\n"
						+ "Final status: [Mercenary HI 1] TQ=8 size=7 - ROUTED");

		for (int i = 0; i < Math.min(expectedLines.size(), logLines.size()); i++) {
			String expected = expectedLines.get(i);
			String log = logLines.get(i);
			Assert.assertEquals(expected, log);
		}

	}

	@Test
	public void shockAgainstSkirmishers() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit sk1 = new Unit(UnitKind.SK, SubClass.NONE, "Skirmishers", "1", "NSK1", 5, 1, MissileType.S);

		//Roll 5 - missed
		//Roll 1 - hit
		//SK makes pre shock TQ, rolls 6 (1 hit)
		//Fight. 0
		Mockito.when(dice.roll()).thenReturn(5, 1, 6, 0);

		sh.execute(Collections.singletonList(lg1), Collections.singletonList(sk1), null);
		Assert.assertEquals(2, lg1.getHits());
		Assert.assertEquals(5, sk1.getHits());
		Assert.assertEquals(UnitState.ROUTED, sk1.getState());
	}

	@Test
	public void skirmishersAttacks3() {
		Unit sk1 = new Unit(UnitKind.SK, null, "Mac archers", "1", "SK1", 3, 1, MissileType.A);
		Unit sk2 = new Unit(UnitKind.SK, null, "Mac archers", "2", "SK2", 3, 1, MissileType.A);

		Unit ch = new Unit(UnitKind.CH, SubClass.NONE, "Chariots", "1", "CH1", 4, 3, MissileType.A);

		Mockito.when(dice.roll()).thenReturn(9, 9, 9, 5, 4, 7, 8);

		sh.execute(Arrays.asList(sk1, sk2), Collections.singletonList(ch), Arrays.asList("f"));
		Mockito.verify(unitChanger).addHits(eq(sk1), eq(2));
		Mockito.verify(unitChanger,times(2)).addHits(eq(sk2), eq(1));
		Mockito.verify(unitChanger).addHits(eq(ch), eq(3));

	}
}
