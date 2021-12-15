package ch.megiste.gboh.command.game;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.command.leader.Elite;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.LeadersHandler;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Dice;

public class EndOrderPhaseTest {

	private EndOrderPhase c;
	private List<Leader> allLeaders=new ArrayList<>();
	private GameStatus gs;
	private Console console;
	private LeadersHandler lh;
	private Dice d;
	private Elite elite;

	@Before
	public void init(){
		allLeaders.size();
		c=new EndOrderPhase();
		console = mock(Console.class);
		c.setConsole(console);

		gs = new GameStatus(){

			@Override
			public List<Leader> getOrderedLeaders() {
				return allLeaders;
			}

			@Override
			public boolean areLeadersUsed() {
				return true;
			}
		};
		lh = new LeadersHandler(console,gs);
		c.setGameStatus(gs);
		c.setLeadersHandler(lh);
		d = mock(Dice.class);
		c.setDice(d);

		elite = new Elite();

	}

	@Test
	public void executeOneExcutionEach() {
		Leader l1 = new Leader("LEO","Leonidas",5,5, true);
		Leader l2 = new Leader("JAQ","Jacquos",3,5, true);
		Leader l3 = new Leader("PTO","Ptolemy",5,5, true);
		Leader l4 = new Leader("ALE","Alex",7,9, true);
		when(console.readLine(anyString())).thenReturn("n");

		allLeaders.addAll(Arrays.asList(l1,l2,l3,l4));
		gs.activateNextLeader();
		finishLeader(l1, l2);
		finishLeader(l2, l3);
		finishLeader(l3, l4);
		finishLeader(l4, null);
	}

	void finishLeader(final Leader l1, final Leader l2) {
		Assert.assertEquals(l1,gs.getCurrentLeader());
		c.execute(null);
		Assert.assertTrue(l1.isFinished());
		Assert.assertEquals(1,l1.getNbActivations());
		Assert.assertEquals(l2,gs.getCurrentLeader());
	}

	@Test
	public void allPossibleMomentums() {
		Leader l1 = new Leader("LEO","Leonidas",5,5, true);
		Leader l2 = new Leader("JAQ","Jacquos",3,5, true);
		Leader l3 = new Leader("PTO","Ptolemy",5,5, true);
		Leader l4 = new Leader("ALE","Alex",7,9, true);

		when(console.readLine(anyString())).thenReturn("y");
		when(d.roll()).thenReturn(0);


		allLeaders.addAll(Arrays.asList(l1,l2,l3,l4));
		gs.activateNextLeader();
		finishLeaderWithMomentums(l1, l2);
		finishLeaderWithMomentums(l2, l3);
		finishLeaderWithMomentums(l3, l4);
		finishLeaderWithMomentums(l4, null);
	}

	void finishLeaderWithMomentums(final Leader l1, final Leader l2) {
		Assert.assertEquals(l1,gs.getCurrentLeader());
		c.execute(null);
		Assert.assertEquals(1,l1.getNbActivations());
		assertFalse(l1.isFinished());
		c.execute(null);
		Assert.assertEquals(2,l1.getNbActivations());
		assertFalse(l1.isFinished());

		c.execute(null);

		Assert.assertTrue(l1.isFinished());
		Assert.assertEquals(3,l1.getNbActivations());
		Assert.assertEquals(l2,gs.getCurrentLeader());
	}

	@Test
	public void withElitePhase() {
		Leader l1 = new Leader("LEO","Leonidas",5,5, true);
		Leader l2 = new Leader("JAQ","Jacquos",3,5, true);
		Leader l3 = new Leader("PTO","Ptolemy",5,5, true);
		Leader l4 = new Leader("ALE","Alex",7,9, true);

		when(console.readLine(anyString())).thenReturn("y");
		when(d.roll()).thenReturn(0);


		allLeaders.addAll(Arrays.asList(l1,l2,l3,l4));
		gs.activateNextLeader();
		finishLeaderWithMomentums(l1, l2);
		finishLeaderWithMomentums(l2, l3);
		finishLeaderWithMomentums(l3, l4);
		finishLeaderWithMomentums(l4, null);
	}

	@Test
	public void withTrump() {
		Leader l1 = new Leader("LEO","Leonidas",5,5, true);
		Leader l2 = new Leader("JAQ","Jacquos",3,5, true);
		Leader l3 = new Leader("PTO","Ptolemy",5,5, true);
		Leader l4 = new Leader("ALE","Alex",7,9, true);

		when(console.readLine(anyString())).thenReturn("y");
		when(d.roll()).thenReturn(0);


		allLeaders.addAll(Arrays.asList(l1,l2,l3,l4));
		gs.activateNextLeader();
		finishLeaderWithMomentums(l1, l2);
		finishLeaderWithMomentums(l2, l3);
		finishLeaderWithMomentums(l3, l4);
		finishLeaderWithMomentums(l4, null);
	}


}