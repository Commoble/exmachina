package com.github.commoble.exmachina.common.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITwoTerminalVoltageSource
{
	public EnumFacing getPositiveFace(World world, BlockPos pos);
	public EnumFacing getNegativeFace(World world, BlockPos pos);
}
