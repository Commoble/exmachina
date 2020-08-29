package commoble.exmachina.circuit;

import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.CircuitManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public class NoCircuitManager implements CircuitManager
{
	public static final LazyOptional<Circuit> HOLDER = LazyOptional.empty();

	@Override
	public LazyOptional<Circuit> getCircuit(BlockPos pos)
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
