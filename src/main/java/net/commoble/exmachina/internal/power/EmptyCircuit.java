package net.commoble.exmachina.internal.power;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.commoble.exmachina.api.Circuit;
import net.commoble.exmachina.api.StateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a power graph circuit without nodes or power
 */
public enum EmptyCircuit implements Circuit
{
	/** Singleton instance of the EmptyCircuit */
	INSTANCE;

	@Override
	public double getPowerSuppliedTo(BlockPos pos)
	{
		return 0;
	}

	@Override
	public double getCurrent()
	{
		return 0;
	}

	@Override
	public void markNeedingDynamicUpdate()
	{
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public Map<BlockPos, Pair<BlockState, StateComponent>> components()
	{
		return Map.of();
	}
	
}