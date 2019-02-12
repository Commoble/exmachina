package com.github.commoble.exmachina.common.block;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IElectricalBlock
{
	/**
	 * Returns a set of the faces representing potential adjacent block spaces the given
	 * block(state) is allowed to connect to
	 */
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos);
}
