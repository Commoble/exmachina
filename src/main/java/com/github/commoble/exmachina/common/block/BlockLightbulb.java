package com.github.commoble.exmachina.common.block;

import java.util.EnumSet;
import java.util.Set;

import com.github.commoble.exmachina.common.item.ItemRegistrar;
import com.github.commoble.exmachina.common.tileentity.TileEntityBattery;
import com.github.commoble.exmachina.common.tileentity.TileEntityLightbulb;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLightbulb extends Block implements IElectricalBlock
{
	public static final EnumSet<EnumFacing> CONNECTABLE_FACES = EnumSet.allOf(EnumFacing.class);

	public BlockLightbulb()
	{
		super(Material.GLASS);
		this.setCreativeTab(ItemRegistrar.tab);
		this.setSoundType(SoundType.GLASS);
		this.setHardness(0.3F);	// same as Glass block
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state)
	{
		// TODO Auto-generated method stub
		return new TileEntityLightbulb();
	}

	@Override
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos)
	{
		// TODO Auto-generated method stub
		return this.CONNECTABLE_FACES;
	}

}
