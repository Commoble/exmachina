package com.github.commoble.exmachina.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.EnumFacing;

public class BlockWithHorizontalFacing extends BlockHorizontal
{
	protected BlockWithHorizontalFacing(Block.Properties props)
	{
		super(props);
	}
    
    public EnumFacing getFacingOfBlockState(IBlockState state)
    {
    	return state.get(HORIZONTAL_FACING);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
    {
        builder.add(HORIZONTAL_FACING);
    }
}
