package ch.megiste.gboh.army;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import ch.megiste.gboh.army.Unit.MissileType;

@XStreamAlias("UnitStatus")
public class UnitStatus implements Serializable {

	public static final String NONE = "NONE";
	@XStreamAsAttribute
	public int hits = 0;


	public enum UnitState {
		OK, RALLIED, ROUTED, ELIMINATED;

	}

	@XStreamAsAttribute
	public UnitState state = UnitState.OK;

	public enum MissileStatus {
		FULL, LOW, NO, NEVER;
	}

	@XStreamAsAttribute
	public String missileStatus = null;

	@XStreamAsAttribute
	public String stackOn = NONE;

	@XStreamAsAttribute
	public String stackUnder = NONE;

	@XStreamOmitField
	public boolean depleted = false;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UnitStatus)) {
			return false;
		}
		final UnitStatus that = (UnitStatus) o;
		return hits == that.hits && state == that.state && Objects.equals(missileStatus, that.missileStatus) && Objects
				.equals(stackOn, that.stackOn) && Objects.equals(stackUnder, that.stackUnder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hits, state, missileStatus, stackOn, stackUnder);
	}
}
