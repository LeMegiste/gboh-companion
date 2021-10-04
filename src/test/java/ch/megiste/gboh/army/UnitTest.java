package ch.megiste.gboh.army;

import org.junit.Assert;
import org.junit.Test;

import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;

public class UnitTest {

	@Test
	public void getLegio() {
		Unit u =new Unit(UnitKind.LG, SubClass.Ha,"12","a","1Haa",7,3, MissileType.NONE);
		Assert.assertEquals("Legio XII",u.getLegio());

		u =new Unit(UnitKind.LG, SubClass.Ha,"3","a","1Haa",7,3, MissileType.NONE);
		Assert.assertEquals("Legio III",u.getLegio());
	}
}