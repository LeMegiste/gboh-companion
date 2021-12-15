package ch.megiste.gboh.game;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.game.GameStatus.FindUnitsFiletr;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class GameStatusTest {

	public static final String MY_BATTLE = "My battle";
	private GameStatus gs;
	private Path baseDir;
	private Dice dice;

	@Before
	public void init() throws IOException {
		gs = new GameStatus();
		final Path p = Files.createTempFile("Test_", ".txt");
		baseDir = p.getParent().resolve("" + System.currentTimeMillis()).resolve(MY_BATTLE);
		Files.createDirectories(baseDir);
		Files.walk(baseDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		Files.createDirectories(baseDir);
		final Path backupPath = Paths.get(".").resolve("backup").resolve(MY_BATTLE);
		Files.createDirectories(backupPath);
		Files.walk(backupPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		Files.createDirectories(backupPath);


		copyArmyFile("Army_Roman.tsv");
		copyArmyFile("Army_Carthaginian.tsv");

		dice = mock(Dice.class);
		gs.setDice(dice);

		return;

	}

	private void copyArmyFile(final String armyName) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(armyName);
		Path armyRoman = baseDir.resolve(armyName);
		Files.deleteIfExists(armyRoman);
		Files.copy(inputStream, armyRoman);
	}

	@Test
	public void testLoading() throws IOException {
		gs.load(baseDir);
		final List<Unit> allUnits = gs.getAllUnits();
		Assert.assertEquals(102, allUnits.size());

	}

	@Test
	public void testLoadingAndPersisting() throws IOException {
		gs.load(baseDir);
		final List<Unit> allUnits = gs.getAllUnits();
		Assert.assertEquals(102, allUnits.size());
		UnitChanger uc = new UnitChanger(mock(Console.class), gs);
		final Unit u = allUnits.get(1);
		uc.addHits(u, 1);
		gs.persistGame();

		gs.load(baseDir);

		Unit u2 = gs.findUnitByCode(u.getUnitCode());
		Assert.assertTrue(u2!= u);
		Assert.assertEquals(1, u2.getHits());
	}

	@Test
	public void unitIsMatchingQuery(){
		Unit u = new Unit(UnitKind.LG, SubClass.Co,"12","a","12Coa",6,3, MissileType.NONE);
		Assert.assertTrue(gs.unitIsMatchingQuery("12Coa",u, FindUnitsFiletr.UNITS_ONLY));
		Assert.assertTrue(gs.unitIsMatchingQuery("12.*",u, FindUnitsFiletr.UNITS_ONLY));
		Assert.assertTrue(gs.unitIsMatchingQuery(".*Co.*",u, FindUnitsFiletr.UNITS_ONLY));
		Assert.assertFalse(gs.unitIsMatchingQuery(".*Cx.*",u, FindUnitsFiletr.UNITS_ONLY));

	}

	@Test
	public void orderLeaders() {
		Leader l1 = new Leader("LEO","Leonidas",5,5, true);
		Leader l2 = new Leader("JAQ","Jacquos",3,5, true);
		Leader l2b = new Leader("TBR","Thomasses",3,5, true);
		Leader l3 = new Leader("PTO","Ptolemy",5,5, true);
		Leader l3b = new Leader("PHI","Philip",5,5, true);
		Leader l4 = new Leader("ALE","Alex",7,9, true);

		when(dice.roll()).thenReturn(1);
		final List<Leader> res = gs.orderLeaders(Arrays.asList(l1, l2,l2b), Arrays.asList(l3, l3b,l4));
		Assert.assertEquals(Arrays.asList(l2,l2b,l3,l1,l3b,l4),res);
		when(dice.roll()).thenReturn(2);
		final List<Leader> res2 = gs.orderLeaders(Arrays.asList(l1, l2,l2b), Arrays.asList(l3, l3b,l4));
		Assert.assertEquals(Arrays.asList(l2,l2b,l1,l3,l3b,l4),res2);



	}
}