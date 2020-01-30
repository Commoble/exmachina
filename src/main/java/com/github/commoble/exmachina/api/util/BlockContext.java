package com.github.commoble.exmachina.api.util;

import javax.annotation.concurrent.Immutable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@Immutable
public class BlockContext
{
	public final BlockState state;
	public final BlockPos pos;
	
	public BlockContext(final BlockState state, final BlockPos pos)
	{
		this.state = state;
		this.pos = pos.toImmutable();
	}
	
	public static BlockContext getContext(final BlockPos pos, IWorld world)
	{
		return new BlockContext(world.getBlockState(pos), pos);
	}
	
	public BlockContext getNewPosContext(BlockPos pos, IWorld world)
	{
		return getContext(pos, world);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		else if (other instanceof BlockContext)
		{
			return this.areValuesEqual((BlockContext)other);
		}
		else
		{
			return false;
		}
	}
	
	public boolean areValuesEqual(BlockContext other)
	{
		return this.state.equals(other.state)
			&& this.pos.equals(other.pos);
	}
}
