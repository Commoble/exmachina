package com.github.commoble.exmachina.common.block;

import java.util.EnumSet;
import java.util.Set;

import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.item.ItemRegistrar;
import com.github.commoble.exmachina.common.tileentity.TileEntityBattery;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBattery extends BlockWithFacing implements IElectricalBlock, ITwoTerminalVoltageSource
{
	// facing of block = facing of positive side
	
	public BlockBattery()
	{
		super(Material.IRON);
		this.setCreativeTab(ItemRegistrar.tab);
		this.setSoundType(SoundType.METAL);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.WEST));
        
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
		return new TileEntityBattery();
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBattery)
		{
			TileEntityBattery teb = (TileEntityBattery)te;
			teb.setFacing(this.getFacingOfBlockState(state));
			if (!world.isRemote)
			{
				teb.circuit_update_check_pending = true;
			}
		}
		
	}

	@Override
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos)
	{
		EnumFacing face1 = this.getFacingOfBlockState(blockState);
		EnumFacing face2 = face1.getOpposite();
		return EnumSet.of(face1, face2);
	}

	@Override
	public EnumFacing getPositiveFace(World world, BlockPos pos)
	{
		return this.getFacingOfBlockState(world.getBlockState(pos));
	}

	@Override
	public EnumFacing getNegativeFace(World world, BlockPos pos)
	{
		return this.getFacingOfBlockState(world.getBlockState(pos)).getOpposite();
	}

	@Override
	public ElectricalValues getElectricalValues(World world, IBlockState blockState, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBattery)
		{
			return ((TileEntityBattery)te).getElectricalValues();
		}
		return null;
	}
}
