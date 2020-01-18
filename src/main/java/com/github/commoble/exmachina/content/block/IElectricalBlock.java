package com.github.commoble.exmachina.content.block;

import java.util.Set;

import com.github.commoble.exmachina.api.electrical.ElectricalValues;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public interface IElectricalBlock
{
	/**
	 * Returns a set of the faces representing potential adjacent block spaces the given
	 * block(state) is allowed to connect to
	 */
	public Set<Direction> getConnectingFaces(IWorld world, BlockState blockState, BlockPos pos);
	public ElectricalValues getElectricalValues(World world, BlockState blockState, BlockPos pos);
	
	default boolean doesThisBlockConnectTo(IWorld world, BlockState thisState, BlockPos thisPos, BlockPos toPos)
	{
		for(Direction face : this.getConnectingFaces(world, thisState, thisPos))
		{
			if (thisPos.offset(face).equals(toPos))
			{
				return true;
			}
		}
		return false;
	}
}
