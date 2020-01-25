package com.github.commoble.exmachina.api.circuit;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlockContext
{
	public final BlockState state;
	public final BlockPos pos;
	public final IWorld world;
	
	public BlockContext(final BlockState state, final BlockPos pos, IWorld world)
	{
		this.state = state;
		this.pos = pos.toImmutable();
		this.world = world;
	}
	
	public static BlockContext getContext(final BlockPos pos, IWorld world)
	{
		return new BlockContext(world.getBlockState(pos), pos.toImmutable(), world);
	}
	
	public BlockContext getNewPosContext(BlockPos pos)
	{
		return getContext(pos.toImmutable(), this.world);
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
