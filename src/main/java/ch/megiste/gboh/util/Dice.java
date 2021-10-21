package ch.megiste.gboh.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Dice {

	private Queue<Integer> nextThrows = new LinkedList<>();

	private Random r = new Random();

	public int roll() {
		if (!nextThrows.isEmpty()) {
			return nextThrows.poll();
		}
		return r.nextInt(10);
	}

	public void nextThrow(final int d) {
		nextThrows.add(d);
	}

}
