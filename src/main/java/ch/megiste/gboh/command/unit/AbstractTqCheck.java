package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;

public abstract class AbstractTqCheck extends UnitCommand {
	protected AbstractTqCheck(final String description) {
		super(description);
	}

	protected int minHits = 0;
	protected int maxHits = Integer.MAX_VALUE;

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<String> modifiers) {
		for (Unit u : attackers) {
			int r = dice.roll();
			console.logNL("Dice rolled: " + r);
			int hits = Math.max(r - u.getOriginalTq(), 0);

			if (hits < minHits) {
				hits = minHits;
			}
			if (hits > maxHits) {
				hits = maxHits;
			}
			unitChanger.addHits(u, hits);

		}
	}
}
