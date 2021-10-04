package ch.megiste.gboh.command.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ch.megiste.gboh.army.Combat;
import ch.megiste.gboh.army.Unit;

public class LineFight extends Fight {

	public LineFight() {
		super();
		description = "Line fight - NOT YET IMPLEMENTED";
	}

	@Override
	public String getKey() {
		return "LF";
	}

	protected List<Combat> buildCombats(final List<Unit> sourceUnits, final List<Unit> destinationUnits) {
		List<Combat> combats = new ArrayList<>();
		int diff = sourceUnits.size() - destinationUnits.size();
		Stack<Unit> sources = new Stack<>();
		sources.addAll(sourceUnits);
		Stack<Unit> destinations = new Stack<>();
		destinations.addAll(destinationUnits);
		for (int i = 0; i < destinationUnits.size() - diff; i++) {
			Unit attacker = sources.pop();
			Unit defender = destinations.pop();
			Combat c = new Combat(Collections.singletonList(attacker), Collections.singletonList(defender));
			combats.add(c);
		}
		for (int i = 0; i < diff; i++) {
			Unit attacker1 = sources.pop();
			Unit attacker2 = sources.pop();

			Unit defender = destinations.pop();
			Combat c = new Combat(Arrays.asList(attacker2,attacker1),Collections.singletonList(defender));
			combats.add(c);
		}
		Collections.reverse(combats);
		return combats;
	}
}
