package ch.megiste.gboh.command.unit;

import org.junit.Assert;
import org.junit.Test;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;

public class LogTest {

	private Log lc = new Log();

	@Test
	public void logLegioUnit() {
		Unit u = new Unit(UnitKind.LG, SubClass.Ha, "12", "a", "1Haa", 7, 3, MissileType.NONE);

		String expecting = "1Haa [Legio XII Hastati a] TQ=7 size=3";
		Assert.assertEquals(expecting, Log.logUnitDetailed(u));

		u = new Unit(UnitKind.RC, null, "12", "a", "12RCa", 5, 3, MissileType.NONE);
		expecting = "12RCa [Legio XII RC a] TQ=5 size=3";
		Assert.assertEquals(expecting, Log.logUnitDetailed(u));

		u = new Unit(UnitKind.RC, null, "12A", "a", "12ARCa", 5, 3, MissileType.NONE);
		expecting = "12ARCa [Legio XII (Allies) RC a] TQ=5 size=3";
		Assert.assertEquals(expecting, Log.logUnitDetailed(u));

		u = new Unit(UnitKind.HI, SubClass.TR, "12", "c", "1Trc", 8, 2, MissileType.NONE);
		expecting = "1Trc [Legio XII Triarii c] TQ=8 size=2";
		Assert.assertEquals(expecting, Log.logUnitDetailed(u));
	}

	@Test
	public void logBasicUnit() {
		Unit u = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);

		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings)", lc.logUnitDetailed(u));

		u.getStatus().hits=1;

		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings) - 1 hit", lc.logUnitDetailed(u));

		u.getStatus().hits=3;

		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings) - 3 hits", lc.logUnitDetailed(u));

		u.getStatus().hits=0;
		u.getStatus().missileStatus="S=LOW";

		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings) - is MISSILE LOW", lc.logUnitDetailed(u));
		u.getStatus().depleted=true;
		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings) - is MISSILE LOW depleted", lc.logUnitDetailed(u));

		u.getStatus().state=UnitState.ROUTED;
		Assert.assertEquals("BSK1 [Balearic SK 1] TQ=5 size=1 (Slings) - ROUTED", lc.logUnitDetailed(u));

	}


}