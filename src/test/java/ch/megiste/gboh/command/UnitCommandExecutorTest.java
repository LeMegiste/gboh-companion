package ch.megiste.gboh.command;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.command.unit.UnitCommand;
import ch.megiste.gboh.util.CommandLineParsingException;
import ch.megiste.gboh.util.Console;

public class UnitCommandExecutorTest {

	UnitCommandExecutor exec;

	@Before
	public void init(){

	}

	@Test
	public void computeModifiers() throws CommandLineParsingException {
		List<Modifier<?>> res = buildExecAndComputeModifiers(Collections.emptyList(), false);
		Assert.assertEquals(0,res.size());

		res = buildExecAndComputeModifiers(Arrays.asList("HHIA","R"), false);
		Assert.assertEquals(0,res.size());

		try {
			buildExecAndComputeModifiers(Arrays.asList("HHIA","F","toto","PH4"), true);
		} catch (CommandLineParsingException e) {
			Assert.assertEquals("Unknown modifier toto",e.getMessage());
		}


		res = buildExecAndComputeModifiers(Arrays.asList("HHIA","F","b","PH4"), true);
		Assert.assertEquals(1,res.size());
		Assert.assertEquals(ModifierDefinition.b,res.get(0).getDefinition());
		Assert.assertEquals(Boolean.TRUE,res.get(0).getValue());

		res = buildExecAndComputeModifiers(Arrays.asList("LIA4","M","b","r2","PH4"), true);
		Assert.assertEquals(2,res.size());
		Assert.assertEquals(ModifierDefinition.b,res.get(0).getDefinition());
		Assert.assertEquals(Boolean.TRUE,res.get(0).getValue());

		Assert.assertEquals(ModifierDefinition.r,res.get(1).getDefinition());
		Assert.assertEquals(2,res.get(1).getValue());

		res = buildExecAndComputeModifiers(Arrays.asList("LIA4","M","b","r-2","PH4"), true);
		Assert.assertEquals(2,res.size());
		Assert.assertEquals(ModifierDefinition.r,res.get(1).getDefinition());
		Assert.assertEquals(-2,res.get(1).getValue());

		res = buildExecAndComputeModifiers(Arrays.asList("LIA4","M","b","r=2","PH4"), true);
		Assert.assertEquals(2,res.size());
		Assert.assertEquals(ModifierDefinition.r,res.get(1).getDefinition());
		Assert.assertEquals(2,res.get(1).getValue());


		res = buildExecAndComputeModifiers(Arrays.asList("LIA4","SET","hits=4","state=RALLIED"), false);
		Assert.assertEquals(2,res.size());
		Assert.assertEquals(ModifierDefinition.hits,res.get(0).getDefinition());
		Assert.assertEquals(4,res.get(0).getValue());

		Assert.assertEquals(ModifierDefinition.state,res.get(1).getDefinition());
		Assert.assertEquals(UnitState.RALLIED,res.get(1).getValue());

		try {
			buildExecAndComputeModifiers(Arrays.asList("HHIA","M","r=x","PH4"), true);
		} catch (CommandLineParsingException e) {
			Assert.assertEquals("Pattern is r=V or rV where V is an positive or negative integer value",e.getMessage());
		}

		try {
			buildExecAndComputeModifiers(Arrays.asList("LIA4","SET","hits=4","state=DEPLETXED"), false);
		} catch (CommandLineParsingException e) {
			Assert.assertEquals("Pattern is state=V or stateV where V is value in the fallowing list (OK,RALLIED,ROUTED,ELIMINATED)",e.getMessage());
		}


	}

	List<Modifier<?>> buildExecAndComputeModifiers(final List<String> inputs, final boolean withTargets) throws CommandLineParsingException {
		final UnitCommand command = mock(UnitCommand.class);
		doReturn(withTargets).when(command).hasTargetUnits();

		exec = new UnitCommandExecutor(inputs, command,mock(Console.class));
		return exec.computeModifiers();
	}
}