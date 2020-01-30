package com.github.commoble.exmachina.api.circuit;

import java.util.HashSet;
import java.util.Set;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;

public interface IConnectionProvider
{
	public boolean canThisConnectTo(BlockContext context);

	/**
	 * @return A Set of positions that a block would be able to connect to
	 * 	if the block at such a position would also be able to connect to this block
	 */
	public Set<BlockPos> getPotentialConnections();
	
	public static final IConnectionProvider NULL_CONNECTOR = new IConnectionProvider()
	{
		@Override
		public boolean canThisConnectTo(BlockContext context)
		{
			return false;
		}
		
		@Override
		public Set<BlockPos> getPotentialConnections()
		{
			return new HashSet<>();
		}
	};
}
