package ch.megiste.gboh.command;

import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;

public enum ModifierDefinition {
	f("Flank attack"),
	b("Back attack"),
	r("Range",Integer.class),
	norf("No reaction fire"),
	cs("Column shift",Integer.class),
	ls("Leader shift",Integer.class),
	m("Moved"),
	fury("Barbarian fury"),
	hits("Hits",Integer.class),
	state("State", UnitState.class),
	nmf("No missile fire"),
	sec("Secondary missile"),
	missile("Missile status", MissileStatus.class), mod("Modifier",Integer.class);

	private Class<?> objectClass = null;


	private final String description;

	ModifierDefinition(final String description) {
		this.description = description;
	}

	ModifierDefinition(final String description, Class<?> objectClass) {
		this(description);
		this.objectClass = objectClass;
	}

	public String getDescription() {
		return description;
	}

	public Class<?> getObjectClass() {
		return objectClass;
	}
}
