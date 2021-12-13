package ch.megiste.gboh.army;

import static ch.megiste.gboh.army.UnitStatus.NONE;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

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

	public void stackOn(final String unitCode) {
		if (Strings.isEmpty(unitCode)) {
			status.stackOn = NONE;
		}
		status.stackOn = unitCode;
	}

	public String getStackedOn() {
		return status.stackOn;
	}

	public void stackUnder(final String unitCode) {
		if (Strings.isEmpty(unitCode)) {
			status.stackUnder = NONE;
		}
		status.stackUnder = unitCode;
	}

	public String getStackedUnder() {
		return status.stackUnder;
	}

	public boolean isStacked() {
		return isStackedOn() || isStackedUnder();
	}

	public boolean isStackedOn() {
		String str = status.stackOn;
		return !NONE.equals(str) && Strings.isNotEmpty(str);

	}

	public boolean isStackedUnder() {
		String str = status.stackUnder;
		return !NONE.equals(str) && Strings.isNotEmpty(str);

	}

	public boolean isStackedWith(final Unit other) {
		return isStacked() && (Objects.equals(getStackedOn(), other.getStackedUnder()) || Objects
				.equals(getStackedUnder(), other.getStackedOn()));
	}

	public boolean isDepleted() {
		return getState() == UnitState.DEPLETED;
	}

	public enum MissileWeapon {
		Bows, Slings, Artillery, Javelin, none;

	}

	public enum MissileType {
		NONE(MissileWeapon.none), S(MissileWeapon.Slings), BS(MissileWeapon.Slings), J(MissileWeapon.Javelin, 7,
				false), MJ(MissileWeapon.Javelin, 7, false), A(MissileWeapon.Bows), MA(MissileWeapon.Bows), O(
				MissileWeapon.Artillery), IA(MissileWeapon.Bows);

		private boolean lessPreciseAfterMovement = true;
		private int missileShortageLevel = 9;
		private final MissileWeapon weapon;

		MissileType(final MissileWeapon weapon) {

			this.weapon = weapon;
		}

		MissileType(final MissileWeapon weapon, final boolean lessPreciseAfterMovement) {
			this(weapon);
			this.lessPreciseAfterMovement = lessPreciseAfterMovement;
		}

		MissileType(final MissileWeapon weapon, final int missileShortageLevel,
				final boolean lessPreciseAfterMovement) {
			this(weapon);
			this.lessPreciseAfterMovement = lessPreciseAfterMovement;
			this.missileShortageLevel = missileShortageLevel;
		}

		public String getDescription() {
			if (weapon != MissileWeapon.none) {
				return weapon.name();
			} else {
				return "";
			}
		}

		public boolean lessPreciseAfterMovement() {
			return lessPreciseAfterMovement;
		}

		public int getMissileShortageLevel() {
			return missileShortageLevel;
		}

		public MissileWeapon getWeapon() {
			return weapon;
		}
	}

	public enum UnitKind {

		LG(UnitCategory.Infantry), HI(UnitCategory.Infantry), MI(UnitCategory.Infantry), LI(UnitCategory.Infantry), SK(
				UnitCategory.Skirmishers), HC(UnitCategory.Cavalry), LC(UnitCategory.Cavalry), RC(
				UnitCategory.Cavalry), BI(UnitCategory.Infantry), EL(UnitCategory.Elephants), PH(
				UnitCategory.Infantry), LP(UnitCategory.Infantry), LN(UnitCategory.Cavalry), CH(
				UnitCategory.Chariots), SKp(UnitCategory.Skirmishers), OX(UnitCategory.Artillery);

		private UnitCategory unitCategory;

		UnitKind(UnitCategory unitCategory) {
			this.unitCategory = unitCategory;
		}

		public UnitCategory getUnitCategory() {
			return unitCategory;
		}
	}

	public enum UnitCategory {
		Infantry, Cavalry, Skirmishers, Chariots, Elephants, Artillery;

	}

	public enum SubClass {
		Ha("Hastati"), Pr("Principes"), CE("Cohorte Extraordinary"), Co("Cohorte"), TR("Triarii"), Ve("Velites"), RC(
				"Roman cavalry"), HO("Hoplite"), CAT("Cataphracted"), CA("Cardaces"), NONE("");
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
		return Log.lotUnit(this);
	}
}
