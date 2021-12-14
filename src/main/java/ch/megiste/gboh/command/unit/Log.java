package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.command.Modifier;

public class Log extends UnitCommand {

	public Log() {
		super("Logs the units");
	}

	public static final String LOG = "LOG";

	@Override
	public String getKey() {
		return LOG;
	}

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<Modifier<?>> modifiers) {

		attackers.stream().map(Log::logUnitDetailed).forEach(s -> console.logNL(s));

	}

	public static String logUnitDetailed(Unit u) {

		final String s = u.getUnitCode() + " [" + lotUnit(u) + "] " + buildStats(u);
		StringBuilder add = new StringBuilder();
		switch (u.getState()) {

		case OK:
		case RALLIED:
			if (u.getHits() == 1) {
				add.append(" ").append(u.getHits()).append(" hit");
			} else if (u.getHits() > 1) {
				add.append(" ").append(u.getHits()).append(" hits");
			}
			if (u.getMainMissileStatus() == MissileStatus.NO) {
				add.append(" is MISSILE NO");
			} else if (u.getMainMissileStatus() == MissileStatus.LOW) {
				add.append(" is MISSILE LOW");
			}
			if (u.isDepleted()) {
				add.append(" depleted");
			}

			break;
		case ROUTED:
		case ELIMINATED:
			add.append(" ").append(u.getState());
			break;
		}
		if (u.isStackedOn()) {
			add.append(" stacked on: ").append(u.getStackedOn());

		}
		if (u.isStackedUnder()) {
			add.append(" stacked under: ").append(u.getStackedUnder());

		}

		if (add.length() > 0) {
			return s + " -" + add.toString();
		} else {
			return s;
		}

	}

	private static String buildStats(final Unit u) {
		if (u.getMainMissile() == MissileType.NONE || u.getKind() == UnitKind.LG) {
			return String.format("TQ=%d size=%d", u.getOriginalTq(), u.getSize());
		} else {
			return String.format("TQ=%d size=%d (%s)", u.getOriginalTq(), u.getSize(), u.getMainMissile().getDescription());
		}
	}

	public static String lotUnit(final Unit u) {
		if (u.getLegio() != null && u.getSubclass() != null) {
			return String.format("%s %s %s", u.getLegio(), u.getSubclass().getDescription(), u.getNumber());
		} else if (u.getLegio() != null) {
			return String.format("%s %s %s", u.getLegio(), u.getKind().name(), u.getNumber());
		} else if (u.getMainMissile() != MissileType.NONE) {
			return String.format("%s %s %s", u.getOrigin(), u.getKind().name(), u.getNumber());
		} else {
			return String.format("%s %s %s", u.getOrigin(), u.getKind().name(), u.getNumber());
		}
	}

	@Override
	public void logAfterCommand(final List<Unit> attackers, final List<Unit> defenders) {
		//Nothing
	}
}
