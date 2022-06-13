package ch.megiste.gboh.command.unit;

import java.util.List;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.command.Modifier;
import ch.megiste.gboh.command.ModifierDefinition;

public abstract class AbstractTqCheck extends UnitCommand {
	protected AbstractTqCheck(final String description) {
		super(description);
	}

	protected int minHits = 0;
	protected int maxHits = Integer.MAX_VALUE;

	@Override
	public void execute(final List<Unit> attackers, final List<Unit> defenders, final List<Modifier<?>> modifiers) {
		for (Unit u : attackers) {
			int r = dice.roll();

			int mod = getIntModifier(modifiers, ModifierDefinition.mod, 0);
			final String modifierText;
			if (mod > 0) {
				modifierText = " (+" + mod + ")";
			} else if (mod < 0) {
				modifierText = " (" + mod + ")";
			} else {
				modifierText = "";
			}
			console.logNL("Dice rolled: [" + r + "]" + modifierText);

			int hits = Math.max(r + mod - u.getOriginalTq(), 0);

			if (hits < minHits) {
				hits = minHits;
			}
			if (hits > maxHits) {
				hits = maxHits;
			}
			if (hits > 0) {
				unitChanger.addHits(u, hits);
			}

		}
	}
}
