package com.github.commoble.exmachina.util;

import net.minecraft.util.math.BlockPos;

public class BlockPosWithDist implements Comparable<BlockPosWithDist>
{
	public final int dist;
	public final BlockPos pos;
	
	public BlockPosWithDist(int dist, BlockPos pos)
	{
		this.dist=dist; this.pos=pos;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj instanceof BlockPosWithDist)
		{
			return ((BlockPosWithDist)obj).dist == this.dist;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int compareTo(BlockPosWithDist obj)
	{
		// TODO Auto-generated method stub
		if (this == obj)
		{
			return 0;
		}
		else
		{	// return a negative int, zeor, or a positive int if this<obj, this==obj, or this>obj, respectively
			// throws NPE if obj is null
			return this.dist - obj.dist;
		}
	}
}
