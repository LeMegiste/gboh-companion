package ch.megiste.gboh.army;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.game.GameStatus;

public class UnitStatusTest {

	private XStream xstream;

	@Before
	public void init() {
		xstream = GameStatus.initXStream();
	}

	@Test
	public void testSerialization() {
		UnitStatus status = new UnitStatus();
		status.hits = 2;
		status.missileStatus = "J=LOW";


		String str = xstream.toXML(status);

		UnitStatus deserialized = (UnitStatus) xstream.fromXML(str);

		Assert.assertEquals(status,deserialized);
	}

}