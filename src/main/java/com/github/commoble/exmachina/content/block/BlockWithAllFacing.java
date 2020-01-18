package com.github.commoble.exmachina.content.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class BlockWithAllFacing extends DirectionalBlock
{

	protected BlockWithAllFacing(Block.Properties props)
	{
		super(props);
	}
    
    public Direction getFacingOfBlockState(BlockState state)
    {
    	return state.get(FACING);
    }
    
    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link BlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
     * fine.
     */
    @Deprecated
	@Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
    	return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link BlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
     */
    @Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
    	return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    /**
     * Called by BlockItems just before a block is actually set in the world, to allow for adjustments to the
     * BlockState
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
    	return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }
}
