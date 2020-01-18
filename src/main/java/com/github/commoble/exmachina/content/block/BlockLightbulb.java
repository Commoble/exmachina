package com.github.commoble.exmachina.content.block;

import java.util.EnumSet;
import java.util.Set;

import com.github.commoble.exmachina.api.electrical.ElectricalValues;
import com.github.commoble.exmachina.content.tileentity.TileEntityLightbulb;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockLightbulb extends BlockWithAllFacing implements IElectricalBlock, ITileEntityProvider, ITwoTerminalComponent
{
	public BlockLightbulb(Block.Properties props)
	{
		super(props);

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.WEST));
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return createTileEntity(worldIn, getDefaultState());
	}

	public TileEntity createTileEntity(IBlockReader world, BlockState state)
	{
		return new TileEntityLightbulb();
	}

	@Override
	public Set<Direction> getConnectingFaces(IWorld world, BlockState blockState, BlockPos pos)
	{
		Direction face1 = this.getFacingOfBlockState(blockState);
		Direction face2 = face1.getOpposite();
		return EnumSet.of(face1, face2);
	}

	@Override
	public Direction getPositiveFace(World world, BlockPos pos)
	{
		return this.getFacingOfBlockState(world.getBlockState(pos));
	}

	@Override
	public Direction getNegativeFace(World world, BlockPos pos)
	{
		return this.getFacingOfBlockState(world.getBlockState(pos)).getOpposite();
	}

	@Override
	public ElectricalValues getElectricalValues(World world, BlockState blockState, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityLightbulb)
		{
			return ((TileEntityLightbulb)te).getElectricalValues();
		}
		return null;
	}

	/**
	 * This is called after another block is placed next to a position containing this block
	 * 
	* Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
	* For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
	* returns its solidified counterpart.
	* Note that this method should ideally consider only the specific face passed in.
	*  
	* @param facingState The state that is currently at the position offset of the provided face to the stateIn at
	* currentPos is the position of this block
	* facingPos is the position of the adjacent block that triggered this method
	*/
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (!worldIn.isRemote())
		{
			TileEntity te = worldIn.getTileEntity(currentPos);
			if (te instanceof TileEntityLightbulb)
			{
				((TileEntityLightbulb)te).invalidateCircuit();
			}
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
}
