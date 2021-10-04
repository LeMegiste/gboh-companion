package ch.megiste.gboh.command.unit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.megiste.gboh.army.Combat;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;

public class LineFightTest {


	@Test
	public void buildCombats() {
		LineFight fc = new LineFight();

		Unit lg1 = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);
		Unit lg2 = new Unit(UnitKind.LG, SubClass.Ha, "12", "b", "1Haab", 7, 3, MissileType.NONE);
		Unit lg3 = new Unit(UnitKind.LG, SubClass.Ha, "12", "c", "1Haac", 7, 3, MissileType.NONE);

		Unit hi1 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "1", "MHI1", 8, 7, MissileType.NONE);
		Unit hi2 = new Unit(UnitKind.HI, SubClass.NONE, "Mercenary", "2", "MHI1", 8, 7, MissileType.NONE);

		//2 times 1 agains 1
		List<Combat> combats = fc.buildCombats(Arrays.asList(lg1, lg2), Arrays.asList(hi1, hi2));
		Assert.assertEquals(2, combats.size());
		Assert.assertEquals(lg1, combats.get(0).getAttackers().get(0));
		Assert.assertEquals(1, combats.get(0).getAttackers().size());

		Assert.assertEquals(hi1, combats.get(0).getMainDefender());
		Assert.assertEquals(lg2, combats.get(1).getAttackers().get(0));
		Assert.assertEquals(1, combats.get(0).getAttackers().size());
		Assert.assertEquals(hi2, combats.get(1).getMainDefender());

		//2 against 1
		combats = fc.buildCombats(Arrays.asList(lg1, lg2), Collections.singletonList(hi1));
		Assert.assertEquals(1, combats.size());
		Assert.assertEquals(lg1, combats.get(0).getAttackers().get(0));
		Assert.assertEquals(lg2, combats.get(0).getAttackers().get(1));
		Assert.assertEquals(hi1, combats.get(0).getMainDefender());

		//2 against 1 and 1 against 1
		combats = fc.buildCombats(Arrays.asList(lg1, lg2, lg3), Arrays.asList(hi1, hi2));
		Assert.assertEquals(lg1, combats.get(0).getAttackers().get(0));
		Assert.assertEquals(lg2, combats.get(0).getAttackers().get(1));
		Assert.assertEquals(hi1, combats.get(0).getMainDefender());
		Assert.assertEquals(lg3, combats.get(1).getAttackers().get(0));
		Assert.assertEquals(2, combats.get(0).getAttackers().size());
		Assert.assertEquals(1, combats.get(1).getAttackers().size());
		Assert.assertEquals(hi2, combats.get(1).getMainDefender());

	}
}