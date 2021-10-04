package ch.megiste.gboh.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DiceTest {

	private Dice d = new Dice();

	@Test
	public void rolls() {
		List<Integer> results = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			final int res = d.roll();
			results.add(res);
		}
		boolean allGreaterOrEqualThanZero = results.stream().allMatch(i->i>=0);
		boolean allLowerThan10 = results.stream().allMatch(i->i<=10);
		Assert.assertTrue(allGreaterOrEqualThanZero);
		Assert.assertTrue(allLowerThan10);

		for(int i=0;i<10;i++){
			final int ii=i;
			boolean containsValue = results.stream().anyMatch(j->j==ii);
			Assert.assertTrue("Dice never threw :"+i,containsValue);
		}

	}

}