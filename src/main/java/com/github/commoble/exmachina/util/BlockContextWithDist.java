package com.github.commoble.exmachina.util;

import com.github.commoble.exmachina.api.util.BlockContext;

public class BlockContextWithDist implements Comparable<BlockContextWithDist>
{
	public final int dist;
	public final BlockContext context;
	
	public BlockContextWithDist(int dist, BlockContext pos)
	{
		this.dist=dist; this.context=pos;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj instanceof BlockContextWithDist)
		{
			return ((BlockContextWithDist)obj).dist == this.dist;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int compareTo(BlockContextWithDist obj)
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
