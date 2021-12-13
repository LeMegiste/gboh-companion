package ch.megiste.gboh.command.game;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.game.GameStatus;
import ch.megiste.gboh.game.PersistableGameState.CommandHistory;
import ch.megiste.gboh.game.PersistableGameState.UnitChange;
import ch.megiste.gboh.game.UnitChanger;
import ch.megiste.gboh.util.Console;
import ch.megiste.gboh.util.Helper;

public class UndoTest {

	private Undo u;
	private Console mockedConsole;
	private GameStatus gs;

	@Before
	public void init() {
		u = new Undo();
		mockedConsole = mock(Console.class);
		u.setConsole(mockedConsole);

		gs = spy(new GameStatus());
		u.setUnitChanger(new UnitChanger(mockedConsole, gs));
		u.setGameStatus(gs);
	}

	@Test
	public void execute() {
		doAnswer(inv -> {
			String str = inv.getArgumentAt(0, String.class);
			System.out.println(str);
			return null;
		}).when(mockedConsole).logNL(anyString());

		Unit sk = new Unit(UnitKind.SK, SubClass.NONE, "Balearic", "1", "BSK1", 5, 1, MissileType.S);
		sk.getStatus().hits = 2;

		doReturn(Collections.singletonList(sk)).when(gs).getAllUnits();

		final CommandHistory ch1 = new CommandHistory(1, "CMD1",1);
		final UnitStatus before = new UnitStatus();
		before.missileStatus = MissileStatus.FULL;
		before.state = UnitState.OK;
		before.hits = 0;
		final UnitStatus after = Helper.clone(before);
		after.hits = 1;
		final UnitChange change = new UnitChange(sk.getUnitCode(), before, after);
		ch1.getChanges().add(change);
		gs.getState().commandHistories.add(ch1);

		final CommandHistory ch2 = new CommandHistory(2, "CMD2",1);
		final UnitStatus before2 = Helper.clone(after);
		final UnitStatus after2 = Helper.clone(before2);
		before.hits = 2;
		final UnitChange change2 = new UnitChange(sk.getUnitCode(), before2, after2);
		ch2.getChanges().add(change2);
		gs.getState().commandHistories.add(ch2);

		Assert.assertEquals(2, sk.getHits());
		when(mockedConsole.readLine(Undo.WHICH_COMMAND_TO_UNDO)).thenReturn("U1");

		u.execute(null);

		Assert.assertEquals(1, sk.getHits());

	}

}