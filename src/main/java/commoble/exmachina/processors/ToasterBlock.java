package commoble.exmachina.processors;

import commoble.exmachina.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ToasterBlock extends Block implements EntityBlock
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	
	public ToasterBlock(Properties props)
	{
		super(props);
		this.registerDefaultState(this.defaultBlockState()
			.setValue(FACING, Direction.NORTH)
			.setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
		builder.add(LIT);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return ExMachina.TOASTER_BLOCKENTITY.get().create(pos, state);
	}

	@Override
	@Deprecated
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		return super.use(state, level, pos, player, hand, result);
	}

	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving)
	{
		if (!oldState.is(newState.getBlock()))
		{
			BlockEntity blockentity = level.getBlockEntity(pos);
			if (blockentity instanceof ToasterBlockEntity be)
			{
				if (level instanceof ServerLevel serverLevel)
				{
					be.dropContents(serverLevel);
					be.getRecipesToAwardAndPopExperience(serverLevel, Vec3.atCenterOf(pos));
				}

				level.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(oldState, level, pos, newState, moving);
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return !level.isClientSide
			? BaseEntityBlock.createTickerHelper(type, ExMachina.TOASTER_BLOCKENTITY.get(), ToasterBlockEntity::serverTick)
			: null;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public boolean hasAnalogOutputSignal(BlockState p_48700_)
	{
		return true;
	}

	public int getAnalogOutputSignal(BlockState p_48702_, Level p_48703_, BlockPos p_48704_)
	{
		return 0;
	}

	@Override
	@Deprecated
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return super.rotate(state, rotation)
			.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	@Deprecated
	public BlockState mirror(BlockState state, Mirror mirror)
	{
	      return super.mirror(state, mirror)
	    	  .rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
