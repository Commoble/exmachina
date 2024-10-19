package net.commoble.exmachina.api;

import net.commoble.exmachina.internal.circuit.LevelCircuitManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface CircuitManager
{
	/**
	 * Gets the CircuitManager for a given ServerLevel
	 * @param level ServerLevel to get the CircuitManager for
	 * @return CircuitManager for that ServerLevel
	 */
	public static CircuitManager get(ServerLevel level)
	{
		return LevelCircuitManager.getOrCreate(level);
	}

	/**
	 * Get a circuit at a position.
	 * @param pos A position in global world coordinates
	 * @return Circuit at the position.
	 * Circuit#isPresent will return false if the block at that position is not part of a circuit.
	 * Circuits are not cacheable and should be requeried when needed.
	 */
	public Circuit getCircuit(BlockPos pos);
	
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
	 * 
	 * This will crash if you call it while iterating over the circuit's component map.
	 * 
	 * @param pos A position contained by the circuit that should be invalidated.
	 */
	public void invalidateCircuit(BlockPos pos);
}
