package net.commoble.exmachina.gametest;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.commoble.exmachina.api.Circuit;
import net.commoble.exmachina.api.CircuitManager;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ExMachina.MODID)
public class ExMachinaGameTest
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	@PrefixGameTestTemplate(false)
	@GameTest(template="empty")
	public static void testCircuit(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		helper.setBlock(BlockPos.ZERO, Blocks.GOLD_BLOCK);
		helper.setBlock(BlockPos.ZERO.above(), Blocks.GLOWSTONE);
		var loadState = helper.getBlockState(BlockPos.ZERO);
		var sourceState = helper.getBlockState(BlockPos.ZERO.above());
		helper.assertTrue(loadState.getBlock() == Blocks.GOLD_BLOCK, "Incorrect load state: " + loadState);
		helper.assertTrue(sourceState.getBlock() == Blocks.GLOWSTONE, "Incorrect source state: " + sourceState);
		Circuit circuit = CircuitManager.get(level).getCircuit(helper.absolutePos(BlockPos.ZERO));
		helper.assertTrue(circuit.isPresent(), "Circuit not present");
		double load = circuit.components().get(helper.absolutePos(BlockPos.ZERO)).getRight().staticLoad();
		double source = circuit.components().get(helper.absolutePos(BlockPos.ZERO.above())).getRight().staticSource();
		double current = circuit.getCurrent();
		double powerConsumption = circuit.getPowerSuppliedTo(helper.absolutePos(BlockPos.ZERO));
		double powerGeneration = circuit.getPowerSuppliedTo(helper.absolutePos(BlockPos.ZERO.above()));
		helper.assertTrue(load == 1D, "Incorrect load: " + load);
		helper.assertTrue(source == 2D, "Incorrect source: " + source);
		helper.assertTrue(current == source / load, "Incorrect current: " + current);
		helper.assertTrue(powerConsumption == source * current, "Incorrect power consumption: " + powerConsumption);
		helper.assertTrue(powerGeneration == -powerConsumption, "Incorrect power generation: " + powerGeneration);
		helper.setBlock(BlockPos.ZERO, Blocks.AIR);
		helper.assertFalse(CircuitManager.get(level).getCircuit(helper.absolutePos(BlockPos.ZERO)).isPresent(), "Circuit still present at load block");
		helper.assertFalse(CircuitManager.get(level).getCircuit(helper.absolutePos(BlockPos.ZERO.above())).isPresent(), "Circuit still present at source block");
		helper.succeed();
	}
	
	@GameTestGenerator
	public static Collection<TestFunction> testBlockStateProperties()
	{
		return GameTestBuilder.spin("testBlockStateProperties", "blockstate_properties", helper -> {
			BlockPos leverPos = new BlockPos(1,2,1);
			BlockState leverState = helper.getBlockState(leverPos);
			helper.assertTrue(leverState.getBlock() == Blocks.LEVER, "Can't find lever at " + leverPos);
			Predicate<BlockPos> hasCircuit = pos -> CircuitManager.get(helper.getLevel()).getCircuit(helper.absolutePos(pos)).isPresent();
			// firstly make sure the're no circuit while lever is unpowered
			helper.assertFalse(hasCircuit.test(leverPos), "Erroneously found circuit at unpowered lever");
			for (Direction d : Direction.values())
			{
				helper.assertFalse(hasCircuit.test(leverPos.relative(d)), "Erroneously found circuit relative to lever: " + d);
			}
			helper.setBlock(leverPos, leverState.setValue(LeverBlock.POWERED, true));
			Direction leverFacing = leverState.getValue(LeverBlock.FACING);
			// test lever is configured to connect up in all cases, and north when facing north (facing + one rotation clockwise from facing)
			helper.assertTrue(hasCircuit.test(leverPos.above()), "No circuit above lever");
			helper.assertFalse(hasCircuit.test(leverPos.below()), "Erroneously found circuit below lever");
			helper.assertTrue(hasCircuit.test(leverPos.relative(leverFacing)), "No circuit facing lever");
			helper.assertFalse(hasCircuit.test(leverPos.relative(Rotation.CLOCKWISE_90.rotate(leverFacing))), "Erroneously found circuit clockwise from lever");
			helper.assertFalse(hasCircuit.test(leverPos.relative(Rotation.CLOCKWISE_180.rotate(leverFacing))), "Erroneously found circuit behind lever");
			helper.assertFalse(hasCircuit.test(leverPos.relative(Rotation.COUNTERCLOCKWISE_90.rotate(leverFacing))), "Erroneously found circuit ccw from lever");
			helper.succeed();
		});
	}
	
	@GameTestGenerator
	public static Collection<TestFunction> testNonRotatingDirections()
	{
		return GameTestBuilder.spin("testNonRotatingDirections", "non_rotating_directions_connector", helper -> {
			BlockPos oakPos = new BlockPos(1,2,1);
			BlockState oakState = helper.getBlockState(oakPos);
			helper.assertTrue(oakState.getBlock() == Blocks.OAK_PLANKS, "Can't find planks at " + oakPos);
			Predicate<BlockPos> hasCircuit = pos -> CircuitManager.get(helper.getLevel()).getCircuit(helper.absolutePos(pos)).isPresent();
			
			// test planks is configured to connect south in all cases
			for (Direction dir : Direction.values())
			{
				if (dir == Direction.SOUTH)
				{
					helper.assertTrue(hasCircuit.test(oakPos.relative(dir)), "No circuit south of planks");
				}
				else
				{
					helper.assertFalse(hasCircuit.test(oakPos.relative(dir)), "Erroneously found circuit to " + dir);
				}
			}
			helper.succeed();
		});
	}

	@PrefixGameTestTemplate(false)
	@GameTest(template="union_connector")
	public static void testUnionConnector(GameTestHelper helper)
	{
		BlockPos chainPos = new BlockPos(0,2,0);
		BlockState chainState = helper.getBlockState(chainPos);
		helper.assertTrue(chainState.getBlock() == Blocks.CHAIN, "Can't find chain at " + chainPos);
		Predicate<BlockPos> hasCircuit = pos -> CircuitManager.get(helper.getLevel()).getCircuit(helper.absolutePos(pos)).isPresent();
		
		// test chain is configured to connect above and below
		helper.assertTrue(hasCircuit.test(chainPos.above()), "No circuit above chain");
		helper.assertTrue(hasCircuit.test(chainPos.below()), "No circuit below chain");
		for (int i=0; i<4; i++)
		{
			Direction dir = Direction.from2DDataValue(i);
			helper.assertFalse(hasCircuit.test(chainPos.relative(dir)), "Erroneously found circuit to " +  dir);
		}
		helper.succeed();
	}
}
