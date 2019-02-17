package com.github.commoble.exmachina.common.block;

import java.util.EnumSet;
import java.util.Set;

import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.tileentity.TileEntityLightbulb;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLightbulb extends Block implements IElectricalBlock
{
	public static final EnumSet<EnumFacing> CONNECTABLE_FACES = EnumSet.allOf(EnumFacing.class);

	public BlockLightbulb(Block.Properties props)
	{
		super(props);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos)
	{
		// TODO Auto-generated method stub
		return BlockLightbulb.CONNECTABLE_FACES;
	}

	@Override
	public ElectricalValues getElectricalValues(World world, IBlockState blockState, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityLightbulb)
		{
			return ((TileEntityLightbulb)te).getElectricalValues();
		}
		return null;
	}

}
