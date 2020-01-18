package com.github.commoble.exmachina.content.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITwoTerminalComponent
{
	public Direction getPositiveFace(World world, BlockPos pos);
	public Direction getNegativeFace(World world, BlockPos pos);
}
