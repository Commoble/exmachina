package com.github.commoble.exmachina.api;

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
	 * Blocks with connector sets that vary with non-blockstate data may call this
	 * to inform the circuit manager that connections should be re-checked.
	 * Note that calling setBlockState or markAndNotifyBlock on the world will automatically
	 * trigger circuit revalidation, but notifyBlockUpdate does not (so this here must be called as well
	 * to trigger circuit revalidation).
	 * @param newState
	 * @param pos
	 */
	public void onBlockUpdate(BlockState newState, BlockPos pos);
}
