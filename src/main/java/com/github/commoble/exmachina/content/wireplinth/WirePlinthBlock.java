package com.github.commoble.exmachina.content.wireplinth;

import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.github.commoble.exmachina.content.registry.TileEntityRegistrar;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

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
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader)
	{
		return TileEntityRegistrar.wire_plinth.create();
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
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace)
	{
		IChunk chunk = world.getChunk(pos);
		if (chunk instanceof Chunk)
		{
			((Chunk)chunk).getCapability(PlinthsInChunkCapability.INSTANCE)
				.ifPresent(plinths -> System.out.println(plinths.getPositions().size()));
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	@Deprecated
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		this.doPlinthSetOperation(world, pos, Set<BlockPos>::add);
		super.onBlockAdded(state, world, pos, oldState, isMoving);
	}

	@Override
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		this.doPlinthSetOperation(world, pos, Set<BlockPos>::remove);
		super.onReplaced(state, world, pos, newState, isMoving);
	}
	
	public void doPlinthSetOperation(World world, BlockPos pos, BiConsumer<Set<BlockPos>, BlockPos> consumer)
	{
		IChunk chunk = world.getChunk(pos);
		if (chunk instanceof Chunk)
		{
			((Chunk)chunk).getCapability(PlinthsInChunkCapability.INSTANCE)
				.ifPresent(plinths -> consumer.accept(plinths.getPositions(), pos));
		}
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
	}

	/**
	 * Called after a block is placed next to this block
	 * 
	 * Update the provided state given the provided neighbor facing and neighbor
	 * state, returning a new state. For example, fences make their connections to
	 * the passed in state if possible, and wet concrete powder immediately returns
	 * its solidified counterpart. Note that this method should ideally consider
	 * only the specific face passed in.
	 */
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState()
			: super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
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
