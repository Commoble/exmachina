package com.github.commoble.exmachina.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface PowerConsumer
{
	public void updateConsumption(IWorld world, BlockPos pos, BlockState state, double power);
}
