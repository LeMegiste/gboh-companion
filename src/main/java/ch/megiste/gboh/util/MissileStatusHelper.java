package ch.megiste.gboh.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.util.Strings;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;

public class MissileStatusHelper {
	public static String missileStatusToString(final Map<MissileType, MissileStatus> ms) {
		if(MapUtils.isEmpty(ms)){
			return "";
		}
		return Joiner.on(",").withKeyValueSeparator("=").join(ms);
	}

	public static Map<MissileType, MissileStatus> missileStatusFromString(final String missileStatus) {
		if (Strings.isEmpty(missileStatus)) {
			return new HashMap<>();
		}
		Map<MissileType, MissileStatus> out = new HashMap<>();
		final Map<String, String> mapStrings = Splitter.on(",").withKeyValueSeparator("=").split(missileStatus);
		for (Entry<String, String> e : mapStrings.entrySet()) {
			out.put(MissileType.valueOf(e.getKey()), MissileStatus.valueOf(e.getValue()));
		}
		return out;
	}

	public static Map<MissileType, MissileStatus> putMissileStatusToState(final Unit u, final MissileStatus ms) {
		final Map<MissileType, MissileStatus> missileStatus = new HashMap<>();
		for(MissileType missileType:u.getMissiles()){

			missileStatus.put(missileType, ms);
		}
		return missileStatus;
	}
}
