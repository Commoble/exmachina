package com.github.commoble.exmachina.content.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;

public class BlockWithHorizontalFacing extends HorizontalBlock
{
	protected BlockWithHorizontalFacing(Block.Properties props)
	{
		super(props);
	}
    
    public Direction getFacingOfBlockState(BlockState state)
    {
    	return state.get(HORIZONTAL_FACING);
    }

    /**
     * Called by BlockItems just before a block is actually set in the world, to allow for adjustments to the
     * BlockState
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HORIZONTAL_FACING);
    }
}
