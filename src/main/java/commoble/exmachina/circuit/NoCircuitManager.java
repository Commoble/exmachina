package commoble.exmachina.circuit;

import java.util.Optional;

import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.CircuitManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class NoCircuitManager implements CircuitManager
{
	public static final Optional<Circuit> HOLDER = Optional.empty();

	@Override
	public Optional<Circuit> getCircuit(BlockPos pos)
	{
		return HOLDER;
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
