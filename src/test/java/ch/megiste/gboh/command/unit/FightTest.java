package ch.megiste.gboh.command.unit;

import static org.mockito.Matchers.eq;

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
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.command.unit.Fight.Position;
import ch.megiste.gboh.command.unit.Fight.Superiority;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class FightTest {

	private Fight fc;
	private Dice dice;
	private UnitChanger unitChanger;

	@Before
	public void init() {
		fc = new Fight();
		unitChanger = Mockito.mock(UnitChanger.class);
		fc.setUnitChanger(unitChanger);
		final Console console = Mockito.mock(Console.class);
		fc.setConsole(console);

		dice = Mockito.mock(Dice.class);
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

		lg1.getStatus().missileStatus = MissileStatus.NO;
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

		Mockito.when(dice.roll()).thenReturn(5);

		fc.execute(Arrays.asList(lg1), Arrays.asList(hi1), null);
		Mockito.verify(unitChanger).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(4));

	}

	@Test
	public void fight2() {
		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);
		Unit lg3 = new Unit(UnitKind.LG, SubClass.Ha, "12", "c", "1Haac", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		Unit hi2 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "2", "MHI1", 8, 7, MissileType.NONE);

		Mockito.when(dice.roll()).thenReturn(1);

		fc.execute(Arrays.asList(lg1, lg2), Arrays.asList(hi1), null);
		Mockito.verify(unitChanger).addHits(eq(lg1), eq(2));
		Mockito.verify(unitChanger).addHits(eq(lg2), eq(1));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(4));

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
		Mockito.when(dice.roll()).thenReturn(1, 8);

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

		Mockito.when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(lg1), Collections.singletonList(hi1), null);
		Mockito.verify(unitChanger).addHits(eq(lg1), eq(4));
		Mockito.verify(unitChanger).addHits(eq(hi1), eq(3));

	}

	@Test
	public void skirmishersAttacks1() {
		Unit ag1 = new Unit(UnitKind.SKp, null, "Agrianian", "1", "Ag1", 6, 1, MissileType.J);

		Unit li = new Unit(UnitKind.LI, SubClass.NONE, "Levy", "1", "CLI1", 4, 3, MissileType.NONE);

		Mockito.when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);
		Mockito.verify(unitChanger).addHits(eq(ag1), eq(3));
		Mockito.verify(unitChanger).addHits(eq(li), eq(1));

	}

	@Test
	public void skirmishersAttacks2() {
		Unit ag1 = new Unit(UnitKind.SK, null, "Mac archers", "1", "SK1", 3, 1, MissileType.A);

		Unit li = new Unit(UnitKind.LI, SubClass.NONE, "Levy", "1", "CLI1", 4, 3, MissileType.NONE);

		Mockito.when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);

	}

	@Test
	public void skirmishersAttacks3() {
		Unit ag1 = new Unit(UnitKind.SK, null, "Mac archers", "1", "SK1", 3, 1, MissileType.A);

		Unit li = new Unit(UnitKind.CH, SubClass.NONE, "Chariots", "1", "CH1", 4, 3, MissileType.A);

		Mockito.when(dice.roll()).thenReturn(5, 7, 8);

		fc.execute(Collections.singletonList(ag1), Collections.singletonList(li), null);
		Mockito.verify(unitChanger).addHits(eq(ag1), eq(4));
		Mockito.verify(unitChanger).addHits(eq(li), eq(3));

	}

}