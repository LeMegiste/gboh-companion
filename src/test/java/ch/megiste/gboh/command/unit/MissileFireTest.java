package ch.megiste.gboh.command.unit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.command.unit.MissileFire;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class MissileFireTest {

	private MissileFire cmd;
	private Dice dice;
	private UnitChanger unitChanger;
	private Console consolerLogger;
	private List<String> logLines = new ArrayList<>();

	@Before
	public void init() {
		cmd = new MissileFire();
		dice = mock(Dice.class);
		unitChanger = mock(UnitChanger.class);
		consolerLogger = mock(Console.class);

		cmd.setConsole(consolerLogger);
		cmd.setDice(dice);
		cmd.setUnitChanger(unitChanger);

		doAnswer(inv -> {
			String s = inv.getArgumentAt(0, String.class);
			System.out.println(s);
			logLines.add(s);
			return null;
		}).when(consolerLogger).logNL(anyString());
	}

	@Test
	public void logging() {
		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		Unit lg = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		when(dice.roll()).thenReturn(8, 8, 3, 3);

		cmd.execute(Arrays.asList(sk), Arrays.asList(lg), null);

		String s = Joiner.on("\n").join(logLines);
		Assert.assertEquals("Balearic SK 1 is firing at Legio XII Hastati a\n" + "Dice rolls: [8]! Missed! \n"
				+ "Legio XII Hastati a is firing in reaction at Balearic SK 1\n"
				+ "Dice rolls: [8]! Missed! (modifiers: +2 because target is SK)", s);
		verify(unitChanger, never()).addHit(any());
		logLines.clear();

		cmd.execute(Arrays.asList(sk), Arrays.asList(lg), null);

		s = Joiner.on("\n").join(logLines);
		Assert.assertEquals(
				"Balearic SK 1 is firing at Legio XII Hastati a\n" + "Dice rolls: [3]! Legio XII Hastati a is hit! \n"
						+ "Legio XII Hastati a is firing in reaction at Balearic SK 1\n"
						+ "Dice rolls: [3]! Balearic SK 1 is hit! (modifiers: +2 because target is SK)", s);
		verify(unitChanger).addHit(eq(lg));

	}

	@Test
	public void executeWithModifiers() {
		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		Unit lg = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		when(dice.roll()).thenReturn(2, 2);

		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Arrays.asList("m", "r=2"));
		verify(unitChanger, never()).addHit(any());
		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Collections.singletonList("m"));
		verify(unitChanger).addHit(eq(lg));

	}

	@Test
	public void missileLowMissileNo() {
		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		Unit lg = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		when(dice.roll()).thenReturn(9, 9, 9, 9);
		final GameStatus gameStatus = mock(GameStatus.class);
		UnitChanger changer = new UnitChanger(consolerLogger, gameStatus);
		cmd.setUnitChanger(changer);


		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Arrays.asList("m", "r1"));
		verify(unitChanger, never()).addHit(any());
		Assert.assertEquals(MissileStatus.LOW, sk.getMissileStatus());

		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Collections.singletonList("m"));
		verify(unitChanger, never()).addHit(any());
		Assert.assertEquals(MissileStatus.NO, sk.getMissileStatus());

	}

}