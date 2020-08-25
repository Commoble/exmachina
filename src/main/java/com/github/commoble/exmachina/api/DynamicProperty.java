package com.github.commoble.exmachina.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@FunctionalInterface
public interface DynamicCircuitElementProperty
{
	public double getValue(IWorld world, BlockPos pos, BlockState state);
}
