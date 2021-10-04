package ch.megiste.gboh.util;

import java.util.Random;

public class Dice {

	private Random r = new Random();

	public int roll(){
		return r.nextInt(10);
	}
}
