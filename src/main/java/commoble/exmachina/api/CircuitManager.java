package commoble.exmachina.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public interface CircuitManager
{

	/**
	 * Get an invalidatable reference to a circuit at a given position.
	 * @param pos A position in global world coordinates
	 * @return An optional reference to a circuit. Returns empty if no valid circuit exists at the given position.
	 * This can be cached until the circuit is invalidated.
	 */
	public LazyOptional<Circuit> getCircuit(BlockPos pos);
	
	/**
	 * Notifies the circuit manager that a block at this position has updated.
	 * Normal blockstate updates are handled by internal processes and mods generally
	 * shouldn't need to call this for those.
	 * @param newState
	 * @param pos
	 */
	public void onBlockUpdate(BlockState newState, BlockPos pos);
	
	/**
	 * Forcibly invalidate a circuit. Use this when standard block updates are insufficient
	 * to cause a circuit revalidation.
	 * Call this when you have a circuit component block whose connectable positions change
	 * but the blockstate does not change (due to the positions being stored in a TileEntity or elsewhere).
	 * Failing to do so may cause circuits to not properly update.
	 * @param pos
	 */
	public void invalidateCircuit(BlockPos pos);
}
