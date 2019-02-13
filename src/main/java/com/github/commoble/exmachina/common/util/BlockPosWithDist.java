package com.github.commoble.exmachina.common.util;

import net.minecraft.util.math.BlockPos;

public class BlockPosWithDist implements Comparable
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
	public int compareTo(Object obj)
	{
		// TODO Auto-generated method stub
		if (this == obj)
		{
			return 0;
		}
		else if (obj instanceof BlockPosWithDist)
		{	// return a negative int, zeor, or a positive int if this<obj, this==obj, or this>obj, respectively
			return this.dist - ((BlockPosWithDist)obj).dist;
		}
		else if (obj == null)
		{
			throw new NullPointerException();
		}
		else
		{
			throw new ClassCastException();
		}
	}
}
