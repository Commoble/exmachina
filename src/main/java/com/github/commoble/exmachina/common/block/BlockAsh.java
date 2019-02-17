package com.github.commoble.exmachina.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;

// lotta stuff copied from BlockSnow
public class BlockAsh extends Block	// need to copy BlockFalling
{
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS_1_8;
	protected static final VoxelShape[] SHAPES = new VoxelShape[]{VoxelShapes.empty(), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

	public final IBlockState MINIMUM_LAYER_STATE;
    public final IBlockState FULL_CUBE_STATE;

	public BlockAsh(Block.Properties props)
    {
		super(props);
        this.MINIMUM_LAYER_STATE = this.stateContainer.getBaseState().with(LAYERS, Integer.valueOf(1));
        this.FULL_CUBE_STATE = this.stateContainer.getBaseState().with(LAYERS, Integer.valueOf(8));
        this.setDefaultState(this.MINIMUM_LAYER_STATE);
    }

    /*@OnlyIn(Dist.CLIENT)
    @Override
    public int getDustColor(IBlockState state)
    {
   		return -8356741;
    }*/

	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return SHAPES[state.get(LAYERS)];
	}

	public VoxelShape getCollisionShape(IBlockState state, IBlockReader worldIn, BlockPos pos)
   	{
	   return SHAPES[state.get(LAYERS) - 1];
   	}

	public boolean allowsMovement(IBlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		switch(type)
		{
			case LAND:
				return state.get(LAYERS) < 5;
			case WATER:
				return false;
			case AIR:
				return false;
			default:
				return false;
		}
	}

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     */
    public boolean isTopSolid(IBlockState state)
    {
        return state.get(LAYERS) == 8;
    }

    /**
     * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
     * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
     * <p>
     * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that
     * does not fit the other descriptions and will generally cause other things not to connect to the face.
     * 
     * @return an approximation of the form of the given face
     */
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
    	return state.get(LAYERS) == 8;
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean isValidPosition(IBlockState state, IWorldReaderBase worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos.down());
        Block block = iblockstate.getBlock();


        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP);
        return blockfaceshape == BlockFaceShape.SOLID || iblockstate.isIn(BlockTags.LEAVES) || block == this && iblockstate.get(LAYERS) == 8;
    }
    
    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     *  
     * @param facingState The state that is currently at the position offset of the provided face to the stateIn at
     * currentPos
     */
    public IBlockState updatePostPlacement(IBlockState stateIn, EnumFacing facing, IBlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
    	return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Nullable
    public IBlockState getStateForPlacement(BlockItemUseContext context)
    {
    	IBlockState iblockstate = context.getWorld().getBlockState(context.getPos());
    	if (iblockstate.getBlock() == this)
    	{
    		int i = iblockstate.get(LAYERS);
    		return iblockstate.with(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
    	}
    	else
    	{
    		return super.getStateForPlacement(context);
    	}
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.checkAndDropBlock(worldIn, pos, state);
    }

    private boolean checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!this.isValidPosition(state, worldIn, pos))
        {
            worldIn.removeBlock(pos);
            
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {	//TODO check snow, more complicated
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos);
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.SNOWBALL;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(IBlockState state, Random random)
    {
    	return random.nextInt(state.get(LAYERS) + 1);
    }

    @Override
    public void tick(IBlockState state, World worldIn, BlockPos pos, Random random)
    {
    	// ash slowly blows away outdoors but not if:
    	// layers is 1 and no ash below, or
    	// layers is 8 and ash above
    	if (worldIn.canBlockSeeSky(pos))
    	{
        	int layers = state.get(LAYERS);
        	if (
        			(layers == 1 && worldIn.getBlockState(pos.down()).getBlock() != BlockRegistrar.ash)
        			||
        			(layers == 8 && worldIn.getBlockState(pos.up()).getBlock() == BlockRegistrar.ash)
        		)
        	{
        		return;
        	}
        	else
        	{
        		worldIn.setBlockState(pos, state.with(LAYERS, layers-1));
        	}
    	}
    }
    
    /*@OnlyIn(Dist.CLIENT)
    @Override
    public static boolean shouldSideBeRendered(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side)
    {
        if (side == EnumFacing.UP)
        {
            return true;
        }
        else
        {
            IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
            return iblockstate.getBlock() == this && iblockstate.get(LAYERS) >= blockState.get(LAYERS) ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }*/

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    @Override
    public boolean isReplaceable(IBlockState state, BlockItemUseContext useContext)
    {
		int i = state.get(LAYERS);
		if (useContext.getItem().getItem() == this.asItem() && i < 8)
		{
			if (useContext.replacingClickedOnBlock())
			{
				return useContext.getFace() == EnumFacing.UP;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return i == 1;
		}
    }
    


    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
    {
    	builder.add(LAYERS);
    }
	
	/**
	 * Attempt to generate ash on the best spot below the burned block, using these rules:
	 * 0) Search downward until one of these three things is found
	 * 1) If a solid block is found, do not place ash
	 * 2) If ash is found w/ layers < 8, increment layers
	 * 3) If an air block that ash can be placed at is found, place it there
	 * @param world
	 * @param pos
	 */
	public static void tryGenerateAsh(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		BlockAsh ash = BlockRegistrar.ash;
		
		if (state.isFullCube() && !state.isIn(BlockTags.LEAVES))
		{
			return;
		}
		else if (state.getBlock() == ash)
		{
			int layers = state.get(LAYERS);
			if (layers < 8)
			{
				world.setBlockState(pos, state.with(LAYERS, layers+1));
			}
			else
			{
				return;
			}
		}
		else if (world.isAirBlock(pos) && ash.isValidPosition(state, world, pos))
		{
			world.setBlockState(pos, ash.MINIMUM_LAYER_STATE);
			return;
		}
		else
		{
			tryGenerateAsh(world, pos.down());
		}
	}
}
