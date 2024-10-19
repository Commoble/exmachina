package commoble.exmachina.wire_post;

import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.mojang.math.OctahedralGroup;

import commoble.exmachina.ExMachina;
import commoble.exmachina.util.EightGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WirePostBlock extends Block implements EntityBlock
{
	public static final DirectionProperty DIRECTION_OF_ATTACHMENT = BlockStateProperties.FACING;
	public static final EnumProperty<OctahedralGroup> TRANSFORM = EightGroup.TRANSFORM;
	
	protected static final VoxelShape[] SHAPES_DUNSWE = {
		Block.box(6D, 0D, 6D, 10D, 10D, 10D),
		Block.box(6D, 6D, 6D, 10D, 16D, 10D),
		Block.box(6D, 6D, 0D, 10D, 10D, 10D),
		Block.box(6D, 6D, 6D, 10D, 10D, 16D),
		Block.box(0D, 6D, 6D, 10D, 10D, 10D),
		Block.box(6D, 6D, 6D, 16D, 10D, 10D)
	};

	public WirePostBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(DIRECTION_OF_ATTACHMENT, Direction.DOWN)
			.setValue(TRANSFORM, OctahedralGroup.IDENTITY));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return ExMachina.WIRE_POST_BLOCKENTITY.get().create(pos, state);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(DIRECTION_OF_ATTACHMENT, TRANSFORM);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		// if we're raytracing a wire, ignore the post (the plate can still block the raytrace)
		if (context instanceof WireRayTraceSelectionContext && ((WireRayTraceSelectionContext)context).shouldIgnoreBlock(pos))
		{
			return Shapes.empty();
		}
		else
		{
			return SHAPES_DUNSWE[state.getValue(DIRECTION_OF_ATTACHMENT).ordinal()];
		}
	}

	protected void notifyNeighbors(Level level, BlockPos pos, BlockState state)
	{
		// markdirty is sufficient for notifying neighbors of internal BE updates
		// standard block updates are sufficient for notifying neighbors of blockstate addition/removal
		// but we do want to notify connected BEs
		if (level.getBlockEntity(pos) instanceof WirePostBlockEntity be)
		{
			be.notifyConnections();
		}
	}

	@Override
	@Deprecated
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		// we override this to ensure the correct context is used instead of the dummy context
		return this.hasCollision ? state.getShape(worldIn, pos, context) : Shapes.empty();
	}

	@Override
	@Deprecated
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		this.updatePostSet(world, pos, Set<BlockPos>::add);
		super.onPlace(state, world, pos, oldState, isMoving);
		this.notifyNeighbors(world, pos, state);
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		
		if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity()))
		{
			if (level.getBlockEntity(pos) instanceof WirePostBlockEntity be)
			{
				be.clearRemoteConnections();
			}
			this.updatePostSet(level, pos, Set<BlockPos>::remove);
			level.removeBlockEntity(pos);
			this.notifyNeighbors(level, pos, state);
		}
	}
	
	public void updatePostSet(Level world, BlockPos pos, BiConsumer<Set<BlockPos>, BlockPos> consumer)
	{
		LevelChunk chunk = world.getChunkAt(pos);
		if (chunk != null)
		{
			chunk.getCapability(PostsInChunk.CAPABILITY)
				.ifPresent(posts -> {
					Set<BlockPos> set = posts.getPositions();
					consumer.accept(set, pos);
					posts.setPositions(set);
				});
		}
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState defaultState = this.defaultBlockState();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		
		for (Direction direction : context.getNearestLookingDirections())
		{
			BlockState checkState = defaultState.setValue(DIRECTION_OF_ATTACHMENT, direction);
			if (checkState != null && checkState.canSurvive(world, pos))
			{
				return world.isUnobstructed(checkState, pos, CollisionContext.empty())
					? checkState
					: null;
			}
		}

		return null;
	}

	@Deprecated
	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		BlockState newState = state.setValue(DIRECTION_OF_ATTACHMENT, rot.rotate(state.getValue(DIRECTION_OF_ATTACHMENT)));
		newState = EightGroup.rotate(newState, rot);
		return newState;
	}

	@Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		Direction oldFacing = state.getValue(DIRECTION_OF_ATTACHMENT);
		Direction newFacing = mirrorIn.getRotation(oldFacing).rotate(oldFacing);
		BlockState newState = state.setValue(DIRECTION_OF_ATTACHMENT, newFacing);
		newState = EightGroup.mirror(newState, mirrorIn);
		return newState;
	}
}
