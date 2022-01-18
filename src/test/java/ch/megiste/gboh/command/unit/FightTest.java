package ch.megiste.gboh.command.unit;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.megiste.gboh.army.Combat;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;
import ch.megiste.gboh.command.unit.Fight.Position;
import ch.megiste.gboh.command.unit.Fight.Superiority;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class FightTest {

	private Fight fc;
	private Dice dice;
	private UnitChanger unitChanger;
	private Console console;

	@Before
	public void init() {
		fc = new Fight();
		unitChanger = mock(UnitChanger.class);
		fc.setUnitChanger(unitChanger);
		console = mock(Console.class);
		doAnswer(inv -> {
			String s = inv.getArgumentAt(0, String.class);
			System.out.println(s);

			return null;
		}).when(console).logNL(anyString());

		fc.setConsole(console);

		dice = mock(Dice.class);
		fc.setDice(dice);
	}

	@Test
	public void findSuperiority() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		Assert.assertEquals(Superiority.AS, fc.findSuperiority(lg1, hi1));

		Unit hc = new Unit(UnitKind.HC, SubClass.NONE, "Nobles", "1", "CHC1", 7, 3, MissileType.NONE);
		Assert.assertEquals("LG is DS against HC only if it is not missile NO", Superiority.DS,
				fc.findSuperiority(hc, lg1));

		lg1.getStatus().missileStatus = "J=NO";
		Assert.assertEquals("LG is DS against HC only if it is not missile NO", Superiority.NONE,
				fc.findSuperiority(hc, lg1));

	}

	@Test
	public void computeColumnShift() {

		Assert.assertEquals(0, fc.computeColumnShift(7, 7));
		Assert.assertEquals(1, fc.computeColumnShift(7, 3));
		Assert.assertEquals(0, fc.computeColumnShift(5, 7));
		Assert.assertEquals(5, fc.computeColumnShift(20, 3));
		Assert.assertEquals(-2, fc.computeColumnShift(1, 3));
	}

	@Test
	public void fight1() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);
		Unit lg3 = new Unit(UnitKind.LG, SubClass.Ha, "12", "c", "1Haac", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		Unit hi2 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "2", "MHI1", 8, 7, MissileType.NONE);

		when(dice.roll()).thenReturn(5);

		fc.execute(Arrays.asList(lg1), Arrays.asList(hi1), null);

		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(lg1, 2);
		impact.put(hi1, 4);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));

	}

	@Test
	public void fight2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);
		Unit lg3 = new Unit(UnitKind.LG, SubClass.Ha, "12", "c", "1Haac", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		Unit hi2 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "2", "MHI1", 8, 7, MissileType.NONE);

		when(dice.roll()).thenReturn(1);

		fc.execute(Arrays.asList(lg1, lg2), Arrays.asList(hi1), null);
		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(lg1, 2);
		impact.put(lg2, 1);
		impact.put(hi1, 4);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));
	}

	@Test
	public void nearCollapseCheck() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);
		lg1.getStatus().hits = 4;
		lg2.getStatus().hits = 4;

		final Map<Unit, Integer> impactPerUnits = new HashMap<>();
		impactPerUnits.put(lg1, 2);
		impactPerUnits.put(lg2, 2);
		when(dice.roll()).thenReturn(1, 8);

		fc.nearCollapseCheck(impactPerUnits, lg1);
		fc.nearCollapseCheck(impactPerUnits, lg2);

		Assert.assertEquals(Integer.valueOf(1), impactPerUnits.get(lg1));
		Assert.assertEquals(Integer.valueOf(4), impactPerUnits.get(lg2));
	}

	@Test
	public void computeImpactPerUnitNormalDispatch() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		Combat c = new Combat(Arrays.asList(lg1, lg2), Collections.singletonList(hi1), new HashMap<>());
		final Map<Unit, Integer> impactPerUnits = fc.computeImpactPerUnit(c, 2, 2, Position.FRONT);
		Assert.assertEquals(Integer.valueOf(1), impactPerUnits.get(lg1));
		Assert.assertEquals(Integer.valueOf(1), impactPerUnits.get(lg2));
		Assert.assertEquals(Integer.valueOf(2), impactPerUnits.get(hi1));

	}

	@Test
	public void computeImpactPerUnitPostChockCollapse1() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		lg1.getStatus().hits = 6;
		hi1.getStatus().hits = 7;

		Combat c = new Combat(lg1, hi1);
		final Map<Unit, Integer> impactPerUnits = fc.computeImpactPerUnit(c, 2, 2, Position.FRONT);
		Assert.assertEquals(Integer.valueOf(0), impactPerUnits.get(lg1));
		Assert.assertEquals(Integer.valueOf(2), impactPerUnits.get(hi1));

	}

	@Test
	public void computeImpactPerUnitPostChockCollapse2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		lg1.getStatus().hits = 6;
		hi1.getStatus().hits = 7;

		Combat c = new Combat(lg1, hi1);
		final Map<Unit, Integer> impactPerUnits = fc.computeImpactPerUnit(c, 3, 2, Position.FRONT);
		Assert.assertEquals(Integer.valueOf(3), impactPerUnits.get(lg1));
		Assert.assertEquals(Integer.valueOf(0), impactPerUnits.get(hi1));

	}

	@Test
	public void wholeFight() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);

		lg1.getStatus().hits = 4;
		hi1.getStatus().hits = 3;

		when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);
		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(lg1, 4);

		impact.put(hi1, 3);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));
	}

	public static Map<Unit, Integer> buildImpact(Unit u, int i) {
		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(u, i);
		return impact;
	}

	public static Map<Unit, Integer> buildImpact(Unit u, int i, Unit u2, int i2) {
		final Map<Unit, Integer> impact = buildImpact(u, i);
		impact.put(u2, i2);
return impact;
	}

	public static Map<Unit, Integer> buildImpact(Unit u, int i, Unit u2, int i2, Unit u3, int i3) {
		final Map<Unit, Integer> impact = buildImpact(u, i, u2, i2);
		impact.put(u3, i3);
		return impact;
	}

	@Test
	public void skirmishersAttacks1() {
		Unit ag1 = new Unit(UnitKind.SKp, null, "Agrianian", "1", "Ag1", 6, 1, MissileType.J);

		Unit li = new Unit(UnitKind.LI, SubClass.NONE, "Levy", "1", "CLI1", 4, 3, MissileType.NONE);

		when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);

		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(ag1, 3);
		impact.put(li, 1);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));

	}

	@Test
	public void skirmishersAttacks2() {
		Unit ag1 = new Unit(UnitKind.SK, null, "Mac archers", "1", "SK1", 3, 1, MissileType.A);

		Unit li = new Unit(UnitKind.LI, SubClass.NONE, "Levy", "1", "CLI1", 4, 3, MissileType.NONE);

		when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);

	}

	@Test
	public void skirmishersAttacks3() {
		Unit ag1 = new Unit(UnitKind.SK, null, "Mac archers", "1", "SK1", 3, 1, MissileType.A);

		Unit li = new Unit(UnitKind.CH, SubClass.NONE, "Chariots", "1", "CH1", 4, 3, MissileType.A);

		when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);

		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(ag1, 4);
		impact.put(li, 3);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));

	}

	@Test
	public void dispatchHitsForPhalanxAgainstTwoUnits() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);

		Unit ph1 = new Unit(UnitKind.PH, SubClass.NONE, "Macedonian Phalanx", "1", "PH1", 7, 10, MissileType.NONE);

		lg1.getStatus().hits = 4;
		lg2.getStatus().hits = 4;

		when(dice.roll()).thenReturn(9);

		fc.execute(Arrays.asList(ph1), Arrays.asList(lg1, lg2), null);

		final Map<Unit, Integer> impact = new HashMap<>();
		impact.put(lg1, 3);
		impact.put(lg2, 1);
		impact.put(ph1, 2);
		Mockito.verify(unitChanger).applyImpactOnUnits(eq(impact));

	}

	@Test
	public void routingStackedPhalanxes() {
		Unit ph1 = new Unit(UnitKind.PH, SubClass.NONE, "Macedonian Phalanx", "1", "PH1", 7, 10, MissileType.NONE);
		Unit ph2 = new Unit(UnitKind.PH, SubClass.NONE, "Macedonian Phalanx", "2", "PH2", 7, 10, MissileType.NONE);
		ph1.getStatus().hits = 3;
		ph2.getStatus().hits = 3;

		ph2.stackUnder(ph1.getUnitCode());
		ph1.stackOn(ph2.getUnitCode());

		Unit hi1 = new Unit(UnitKind.HI, SubClass.HO, "Argos hoplites", "1", "AHI1", 7, 10, MissileType.NONE);

		final GameStatus gameStatus = mock(GameStatus.class);
		unitChanger = new UnitChanger(console, gameStatus);
		fc.setUnitChanger(unitChanger);

		when(gameStatus.getUnitFromCode("PH1")).thenReturn(ph1);
		when(gameStatus.getUnitFromCode("PH2")).thenReturn(ph2);
		when(gameStatus.getStackedUnit(ph1)).thenReturn(ph2);
		when(gameStatus.getStackedUnit(ph2)).thenReturn(ph1);

		when(dice.roll()).thenReturn(9, 9, 9);

		fc.execute(Arrays.asList(hi1), Arrays.asList(ph1),
				Collections.singletonList(new Modifier<>(ModifierDefinition.f, true)));

		Assert.assertEquals(UnitState.ROUTED, ph1.getState());
		Assert.assertEquals(UnitState.ROUTED, ph2.getState());
	}

}