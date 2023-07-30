package commoble.exmachina.circuit;

import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.CircuitManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NoCircuitManager implements CircuitManager
{
	@Override
	public Circuit getCircuit(BlockPos pos)
	{
		return Circuit.empty();
	}

	@Override
	public void onBlockUpdate(BlockState newState, BlockPos pos)
	{
	}

	@Override
	public void invalidateCircuit(BlockPos pos)
	{
	}

}
