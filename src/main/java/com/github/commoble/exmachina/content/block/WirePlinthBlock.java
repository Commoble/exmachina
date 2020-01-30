package com.github.commoble.exmachina.content.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class WirePlinthBlock extends Block
{
	public static final DirectionProperty DIRECTION_OF_ATTACHMENT = BlockStateProperties.FACING;
	
	protected static final VoxelShape[] SHAPES_DUNSWE = {
		Block.makeCuboidShape(6D, 0D, 6D, 10D, 10D, 10D),	// down
		Block.makeCuboidShape(6D, 16D, 6D, 10D, 6D, 10D),	// up
		Block.makeCuboidShape(6D, 6D, 0D, 10D, 10D, 10D),	// north
		Block.makeCuboidShape(6D, 6D, 6D, 10D, 10D, 16D),	// south
		Block.makeCuboidShape(0D, 6D, 6D, 10D, 10D, 10D),	// west
		Block.makeCuboidShape(6D, 6D, 6D, 16D, 10D, 10D)	// east
	};

	public WirePlinthBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(DIRECTION_OF_ATTACHMENT, Direction.DOWN));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(DIRECTION_OF_ATTACHMENT);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES_DUNSWE[state.has(DIRECTION_OF_ATTACHMENT) ? state.get(DIRECTION_OF_ATTACHMENT).ordinal() : 0];
	}


	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		Direction attachmentDirection = state.has(DIRECTION_OF_ATTACHMENT) ? state.get(DIRECTION_OF_ATTACHMENT) : Direction.DOWN;
		return hasEnoughSolidSide(worldIn, pos.offset(attachmentDirection), attachmentDirection.getOpposite());
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		BlockState defaultState = this.getDefaultState();
		IWorldReader world = context.getWorld();
		BlockPos pos = context.getPos();

		// Direction clickedFace = context.getFace(); // face of the block that was
		// clicked
		// Direction againstClickedFace = clickedFace.getOpposite();
		// BlockState stateAgainstClickedFace = defaultState.with(FACING,
		// againstClickedFace);
		// if (stateAgainstClickedFace.isValidPosition(world, pos))
		// {
		// return stateAgainstClickedFace;
		// }

		BlockState bestState = null;
		for (Direction direction : context.getNearestLookingDirections())
		{
			BlockState checkState = defaultState.with(DIRECTION_OF_ATTACHMENT, direction);
			if (checkState != null && checkState.isValidPosition(world, pos))
			{
				bestState = checkState;
				break;
			}
		}

		return bestState != null && world.func_226663_a_(bestState, pos, ISelectionContext.dummy()) ? bestState : null;

		// for (Direction direction : context.getNearestLookingDirections())
		// {
		// if (direction != Direction.UP)
		// {
		// BlockState blockstate2 = direction == Direction.DOWN ?
		// this.getBlock().getStateForPlacement(context) : blockstate;
		// if (blockstate2 != null && blockstate2.isValidPosition(iworldreader,
		// blockpos))
		// {
		// blockstate1 = blockstate2;
		// break;
		// }
		// }
		// }
		//
		// return blockstate1 != null && iworldreader.func_226663_a_(blockstate1,
		// blockpos, ISelectionContext.dummy()) ? blockstate1 : null;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.with(DIRECTION_OF_ATTACHMENT, rot.rotate(state.get(DIRECTION_OF_ATTACHMENT)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.toRotation(state.get(DIRECTION_OF_ATTACHMENT)));
	}

}
