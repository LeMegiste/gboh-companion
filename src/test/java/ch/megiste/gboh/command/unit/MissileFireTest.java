package ch.megiste.gboh.command.unit;

import static ch.megiste.gboh.command.ModifierDefinition.f;
import static ch.megiste.gboh.command.ModifierDefinition.m;
import static ch.megiste.gboh.command.ModifierDefinition.r;
import static ch.megiste.gboh.command.Modifiers.boolMod;
import static ch.megiste.gboh.command.Modifiers.intMod;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;
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
		verify(unitChanger, never()).addHits(any(), eq(1));
		logLines.clear();

		cmd.execute(Arrays.asList(sk), Arrays.asList(lg), null);

		s = Joiner.on("\n").join(logLines);
		Assert.assertEquals(
				"Balearic SK 1 is firing at Legio XII Hastati a\n" + "Dice rolls: [3]! Legio XII Hastati a is hit! \n"
						+ "Legio XII Hastati a is firing in reaction at Balearic SK 1\n"
						+ "Dice rolls: [3]! Balearic SK 1 is hit! (modifiers: +2 because target is SK)", s);
		verify(unitChanger).addHits(eq(lg),eq(1));

	}

	@Test
	public void executeWithModifiers() {
		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		Unit lg = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		when(dice.roll()).thenReturn(2, 2);

		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Arrays.asList(new Modifier<>(
				ModifierDefinition.m,true),new Modifier<>(ModifierDefinition.r,2)));
		verify(unitChanger, never()).addHits(any(), eq(1));
		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Collections.singletonList(new Modifier(
				m,true)));
		verify(unitChanger).addHits(eq(lg), eq(1));

	}

	@Test
	public void missileLowMissileNo() {
		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		Unit lg = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		when(dice.roll()).thenReturn(9, 9, 9, 9);
		final GameStatus gameStatus = mock(GameStatus.class);
		UnitChanger changer = new UnitChanger(consolerLogger, gameStatus);
		cmd.setUnitChanger(changer);


		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Arrays.asList(boolMod(m), intMod(r,1)));
		verify(unitChanger, never()).addHits(any(), eq(1));
		Assert.assertEquals(MissileStatus.LOW, sk.getMainMissileStatus());

		cmd.execute(Collections.singletonList(sk), Collections.singletonList(lg), Collections.singletonList(boolMod(m)));
		verify(unitChanger, never()).addHits(any(), eq(1));
		Assert.assertEquals(MissileStatus.NO, sk.getMainMissileStatus());

	}

	@Test
	public void firingAtElephants(){
		Unit sk = new Unit(UnitKind.SKp, SubClass.NONE, "Agrianian", "1", "Ag1", 6, 1, MissileType.J);
		Unit el = new Unit(UnitKind.EL, SubClass.Indian, "India", "1", "EL1", 6, 5, Arrays.asList(MissileType.J,MissileType.ES));

		//Fire and double reaction fire
		when(dice.roll()).thenReturn(4, 3, 3);

		cmd.execute(Collections.singletonList(sk), Arrays.asList(el), null);
		verify(unitChanger).addHits(eq(el),eq(2));
		verify(unitChanger,times(2)).addHits(eq(sk),eq(1));


	}

	@Test
	public void firingAtElephantsFromFlank(){
		Unit sk = new Unit(UnitKind.SKp, SubClass.NONE, "Agrianian", "1", "Ag1", 6, 1, MissileType.J);
		Unit el = new Unit(UnitKind.EL, SubClass.Indian, "India", "1", "EL1", 6, 5, Arrays.asList(MissileType.J,MissileType.ES));

		//Fire and double reaction fire
		when(dice.roll()).thenReturn(4, 3, 3);

		cmd.execute(Arrays.asList(sk), Arrays.asList(el), Collections.singletonList(new Modifier<>(f,true)));
		verify(unitChanger).addHits(eq(el),eq(2));


	}

}