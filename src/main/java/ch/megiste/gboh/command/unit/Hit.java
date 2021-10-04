package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;

public abstract class Hit extends UnitCommand {

	protected Hit(final int count) {
		super(count > 0 ? "Adds " + count + " hit"+ (count>1?"s":"")+" to the unit" : "Removes " + count + " hit"+ (count>1?"s":"")+" from the unit");
		this.count = count;
	}

	private final int count;

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		attackers.forEach(u -> unitChanger.addHits(u, count));
	}
}
