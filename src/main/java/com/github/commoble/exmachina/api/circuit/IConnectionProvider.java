package com.github.commoble.exmachina.api.circuit;

import java.util.HashSet;
import java.util.Set;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IConnectionProvider
{
	public boolean canThisConnectTo(IWorld world, BlockContext context);

	/**
	 * @return A Set of positions that a block would be able to connect to
	 * 	if the block at such a position would also be able to connect to this block
	 */
	public Set<BlockPos> getPotentialConnections(IWorld world);
	
	public static final IConnectionProvider NULL_CONNECTOR = new IConnectionProvider()
	{
		@Override
		public boolean canThisConnectTo(IWorld world, BlockContext context)
		{
			return false;
		}
		
		@Override
		public Set<BlockPos> getPotentialConnections(IWorld world)
		{
			return new HashSet<>();
		}
	};
}
