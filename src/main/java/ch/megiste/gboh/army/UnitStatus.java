package ch.megiste.gboh.army;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("UnitStatus")
public class UnitStatus implements Serializable {

	public static final String NONE = "NONE";
	@XStreamAsAttribute
	public int hits = 0;


	public enum UnitState {
		OK, DEPLETED, RALLIED, ROUTED, ELIMINATED;

	}

	@XStreamAsAttribute
	public UnitState state = UnitState.OK;

	public enum MissileStatus {
		FULL, LOW, NO, NEVER;
	}

	@XStreamAsAttribute
	public MissileStatus missileStatus = MissileStatus.FULL;

	@XStreamAsAttribute
	public String stackOn = NONE;

	@XStreamAsAttribute
	public String stackUnder = NONE;



}
