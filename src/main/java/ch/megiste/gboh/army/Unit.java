package ch.megiste.gboh.army;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.Log;
import ch.megiste.gboh.util.Helper;

public class Unit {
	private UnitKind kind;
	private SubClass subclass;
	private String origin;
	private String number;
	private String unitCode;
	private int tq;
	private int size;
	private MissileType missile;

	public UnitStatus getStatus() {
		return status;
	}

	public void setStatus(final UnitStatus status) {
		this.status = status;
	}

	private UnitStatus status = new UnitStatus();

	public int getOriginalTq() {
		return tq;
	}

	public int getRountPoints() {
		if (kind == UnitKind.SK || kind == UnitKind.EL) {
			return 2;
		} else if (size >= 9) {
			return 2 * getOriginalTq();
		} else {
			return getOriginalTq();
		}

	}

	public enum MissileType {
		NONE(""), S("Slings", true), BS("Slings", true), J("Javelins", 7), MJ("Javelins", 7), A("Bows");

		private String description;
		private boolean lessPreciseAfterMovement = false;
		private int missileShortageLevel = 9;

		MissileType(final String description) {
			this.description = description;
		}

		MissileType(final String description, final boolean lessPreciseAfterMovement) {
			this.description = description;
			this.lessPreciseAfterMovement = lessPreciseAfterMovement;
		}

		MissileType(final String description, final int missileShortageLevel) {
			this.description = description;
			this.missileShortageLevel = missileShortageLevel;
		}

		public String getDescription() {
			return description;
		}

		public boolean lessPreciseAfterMovement() {
			return lessPreciseAfterMovement;
		}

		public int getMissileShortageLevel() {
			return missileShortageLevel;
		}
	}

	public enum UnitKind {
		LG, HI, MI, LI, SK, HC, LC, RC, BI, EL, PH, LP, LN, CH;

	}

	public enum SubClass {
		Ha("Hastati"), Pr("Principes"), CE("Cohorte Extraordinary"), Co("Cohorte"), TR("Triarii"), Ve("Velites"), RC(
				"Roman cavalry"), HO("Hoplite"), CAT("Cataphracted"), CA("Cardaces"),NONE("");
		private String description;

		SubClass(final String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	public Unit(final UnitKind kind, final SubClass subclass, final String origin, final String number,
			final String unitCode, final int tq, final int size, final MissileType missile) {
		this.kind = kind;
		this.subclass = subclass;
		this.origin = origin;
		this.number = number;
		this.unitCode = unitCode;
		this.tq = tq;
		this.size = size;
		if (kind != UnitKind.LG) {
			this.missile = missile;
		} else {
			this.missile = MissileType.J;
		}
		if (this.missile == MissileType.NONE) {
			status.missileStatus = MissileStatus.NEVER;
		}
	}

	public UnitKind getKind() {
		return kind;
	}

	public SubClass getSubclass() {
		return subclass;
	}

	public String getOrigin() {
		return origin;
	}

	public String getNumber() {
		return number;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public int getTq() {
		if (status.state == UnitState.ROUTED) {
			return 1;
		}
		return tq;
	}

	public int getSize() {
		return size;
	}

	public MissileType getMissile() {
		return missile;
	}

	public static Pattern LEGIO_ORIGIN_PATTERN = Pattern.compile("^(\\d+)(A?).*");

	public String getLegio() {
		Matcher m = LEGIO_ORIGIN_PATTERN.matcher(getOrigin());
		if (m.matches()) {
			String legioNumber = m.group(1);
			String legioNumberInRoman = Helper.toRoman(Integer.parseInt(legioNumber));
			String allied = "";
			if (getOrigin().endsWith("A")) {
				allied = " (Allies)";
			}

			return "Legio " + legioNumberInRoman + allied;
		}
		return null;
	}

	public MissileStatus getMissileStatus() {
		return status.missileStatus;
	}

	public int getHits() {
		return status.hits;
	}

	public UnitState getState() {
		return status.state;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Unit)) {
			return false;
		}
		final Unit unit = (Unit) o;
		return tq == unit.tq && size == unit.size && kind == unit.kind && subclass == unit.subclass && Objects
				.equals(origin, unit.origin) && Objects.equals(number, unit.number) && Objects
				.equals(unitCode, unit.unitCode) && missile == unit.missile;
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, subclass, origin, number, unitCode, tq, size, missile);
	}

	@Override
	public String toString() {
		return Log.buildStaticDesc(this);
	}
}
