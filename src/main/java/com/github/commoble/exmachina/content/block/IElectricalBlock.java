package com.github.commoble.exmachina.common.block;

import java.util.Set;

import com.github.commoble.exmachina.common.electrical.ElectricalValues;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public interface IElectricalBlock
{
	/**
	 * Returns a set of the faces representing potential adjacent block spaces the given
	 * block(state) is allowed to connect to
	 */
	public Set<EnumFacing> getConnectingFaces(IWorld world, IBlockState blockState, BlockPos pos);
	public ElectricalValues getElectricalValues(World world, IBlockState blockState, BlockPos pos);
	
	default boolean doesThisBlockConnectTo(IWorld world, IBlockState thisState, BlockPos thisPos, BlockPos toPos)
	{
		for(EnumFacing face : this.getConnectingFaces(world, thisState, thisPos))
		{
			if (thisPos.offset(face).equals(toPos))
			{
				return true;
			}
		}
		return false;
	}
}
