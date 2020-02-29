package com.github.commoble.exmachina.content.util;

import net.minecraft.util.math.BlockPos;

public class BlockPosHelper
{
	public static BlockPos asLocalPosInChunk(BlockPos pos)
	{
		return new BlockPos(
			pos.getX() & 15,
			pos.getY() & 15,
			pos.getZ() & 15
		);
	}
}
