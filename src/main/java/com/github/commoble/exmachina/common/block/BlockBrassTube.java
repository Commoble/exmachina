package com.github.commoble.exmachina.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSixWay;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.items.CapabilityItemHandler;

public class BlockBrassTube extends Block implements IBucketPickupHandler, ILiquidContainer
{
	public static final EnumFacing[] FACING_VALUES = EnumFacing.values();

	public static final BooleanProperty DOWN = BlockSixWay.DOWN;
	public static final BooleanProperty UP = BlockSixWay.UP;
	public static final BooleanProperty NORTH = BlockSixWay.NORTH;
	public static final BooleanProperty SOUTH = BlockSixWay.SOUTH;
	public static final BooleanProperty WEST = BlockSixWay.WEST;
	public static final BooleanProperty EAST = BlockSixWay.EAST;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public static final double MIN_VOXEL = 0D;
	public static final double ONE_QUARTER = 4D;
	public static final double THREE_QUARTERS = 12D;
	public static final double SIX_SIXTEENTHS = 6D;
	public static final double TEN_SIXTEENTHS = 10D;
	public static final double MAX_VOXEL = 16D;

	protected final VoxelShape[] shapes;

	public BlockBrassTube(Properties properties)
	{
		super(properties);
		this.shapes = this.makeShapes();
	}

	/// basic block behaviour

	/**
	 * @deprecated call via {@link IBlockState#isFullCube()} whenever possible.
	 *             Implementing/overriding is fine.
	 */
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean allowsMovement(IBlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(IBlockState state, IBlockReader reader, BlockPos pos)
	{
		return true;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks,
	 * CUTOUT or CUTOUT_MIPPED for on-off transparency (glass, reeds), TRANSLUCENT
	 * for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

//	/**
//	 * @deprecated call via
//	 *             {@link IBlockState#shouldSideBeRendered(IBlockAccess,BlockPos,EnumFacing)}
//	 *             whenever possible. Implementing/overriding is fine.
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static boolean shouldSideBeRendered(IBlockState adjacentState, IBlockReader blockState, BlockPos blockAccess,
//			EnumFacing pos)
//	{
//		return true;
//	}

	/// connections and states

	public IBlockState getStateForPlacement(BlockItemUseContext context)
	{
		IBlockReader world = context.getWorld();
		BlockPos pos = context.getPos();
		IFluidState fluidstate = context.getWorld().getFluidState(context.getPos());
		return super.getStateForPlacement(context).with(DOWN, canConnectTo(world, pos, EnumFacing.DOWN))
				.with(UP, canConnectTo(world, pos, EnumFacing.UP))
				.with(NORTH, canConnectTo(world, pos, EnumFacing.NORTH))
				.with(SOUTH, canConnectTo(world, pos, EnumFacing.SOUTH))
				.with(WEST, canConnectTo(world, pos, EnumFacing.WEST))
				.with(EAST, canConnectTo(world, pos, EnumFacing.EAST))
				.with(WATERLOGGED, Boolean.valueOf(fluidstate.getFluid() == Fluids.WATER));
	}

	protected boolean canConnectTo(IBlockReader world, BlockPos pos, EnumFacing face)
	{
		BlockPos newPos = pos.offset(face);
		if (world.getBlockState(newPos).getBlock() instanceof BlockBrassTube)
			return true;

		TileEntity te = world.getTileEntity(newPos);

		if (te == null)
			return false;

		if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face) != null)
		{
			return true;
		}
		return false;
	}

	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
	{
		builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST, WATERLOGGED);
	}

	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face)
	{
		return BlockFaceShape.UNDEFINED;
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor
	 * state, returning a new state. For example, fences make their connections to
	 * the passed in state if possible, and wet concrete powder immediately returns
	 * its solidified counterpart. Note that this method should ideally consider
	 * only the specific face passed in.
	 *
	 * @param facingState
	 *            The state that is currently at the position offset of the provided
	 *            face to the stateIn at currentPos
	 */
	@Override
	public IBlockState updatePostPlacement(IBlockState stateIn, EnumFacing facing, IBlockState facingState,
			IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (stateIn.get(WATERLOGGED))
		{
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}

		return stateIn.with(BlockSixWay.FACING_TO_PROPERTY_MAP.get(facing),
				Boolean.valueOf(this.canConnectTo(worldIn, currentPos, facing)));
	}

	/// model shapes

	protected VoxelShape[] makeShapes()
	{
		// 6 different state flags = 2^6 = 64 different state models (waterlogging
		// doesn't affect model)
		VoxelShape[] shapes = new VoxelShape[64];

		// define the shapes for the piping core and the dunswe pipe segments
		// reminder: north = negative
		VoxelShape core = Block.makeCuboidShape(ONE_QUARTER, ONE_QUARTER, ONE_QUARTER, THREE_QUARTERS, THREE_QUARTERS,
				THREE_QUARTERS);

		VoxelShape down = Block.makeCuboidShape(SIX_SIXTEENTHS, MIN_VOXEL, SIX_SIXTEENTHS, TEN_SIXTEENTHS,
				THREE_QUARTERS, TEN_SIXTEENTHS);
		VoxelShape up = Block.makeCuboidShape(SIX_SIXTEENTHS, THREE_QUARTERS, SIX_SIXTEENTHS, TEN_SIXTEENTHS, MAX_VOXEL,
				TEN_SIXTEENTHS);
		VoxelShape north = Block.makeCuboidShape(SIX_SIXTEENTHS, SIX_SIXTEENTHS, MIN_VOXEL, TEN_SIXTEENTHS,
				TEN_SIXTEENTHS, ONE_QUARTER);
		VoxelShape south = Block.makeCuboidShape(SIX_SIXTEENTHS, SIX_SIXTEENTHS, THREE_QUARTERS, TEN_SIXTEENTHS,
				TEN_SIXTEENTHS, MAX_VOXEL);
		VoxelShape west = Block.makeCuboidShape(MIN_VOXEL, SIX_SIXTEENTHS, SIX_SIXTEENTHS, THREE_QUARTERS,
				TEN_SIXTEENTHS, TEN_SIXTEENTHS);
		VoxelShape east = Block.makeCuboidShape(THREE_QUARTERS, SIX_SIXTEENTHS, SIX_SIXTEENTHS, MAX_VOXEL,
				TEN_SIXTEENTHS, TEN_SIXTEENTHS);

		VoxelShape[] dunswe = { down, up, north, south, west, east };

		for (int i = 0; i < 64; i++)
		{
			shapes[i] = core;
			// if the state flag exists in this state's 6-bit binary pattern, use the pipe
			// segment in this state model
			// down = LSB, east = MSB
			for (int j = 0; j < 6; j++)
			{
				if ((i & (1 << j)) != 0)
				{
					shapes[i] = VoxelShapes.or(shapes[i], dunswe[j]);
				}
			}
		}

		return shapes;
	}

	@Override
	public VoxelShape getRenderShape(IBlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return this.getShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getCollisionShape(IBlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return this.getShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return this.shapes[this.getShapeIndex(state)];
	}

	public int getShapeIndex(IBlockState state)
	{
		int index = 0;

		for (int j = 0; j < FACING_VALUES.length; ++j)
		{
			if (state.get(BlockSixWay.FACING_TO_PROPERTY_MAP.get(FACING_VALUES[j])))
			{
				index |= 1 << j;
			}
		}

		return index;
	}

	/// watterloggy stuff

	@Override
	public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, IBlockState state, Fluid fluidIn)
	{
		return !state.get(WATERLOGGED) && fluidIn == Fluids.WATER;
	}

	@Override
	public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn)
	{
		if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER)
		{
			if (!worldIn.isRemote())
			{
				worldIn.setBlockState(pos, state.with(WATERLOGGED, Boolean.valueOf(true)), 3);
				worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(),
						fluidStateIn.getFluid().getTickRate(worldIn));
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, IBlockState state)
	{
		if (state.get(WATERLOGGED))
		{
			worldIn.setBlockState(pos, state.with(WATERLOGGED, Boolean.valueOf(false)), 3);
			return Fluids.WATER;
		}
		else
		{
			return Fluids.EMPTY;
		}
	}

	@Override
	public IFluidState getFluidState(IBlockState state)
	{
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

}
