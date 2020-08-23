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
	 * This is normally done by internal systems and generally shouldn't be needed
	 * to be called by mods unless they're updating blockstates in a way that doesn't
	 * cause ordinary block updates
	 * @param newState
	 * @param pos
	 */
	public void onBlockUpdate(BlockState newState, BlockPos pos);
}
