package com.github.commoble.exmachina.common.block;

import javax.annotation.Nullable;

import com.github.commoble.exmachina.common.tileentity.TileEntityElectricFurnace;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockElectricFurnace extends Block implements ITileEntityProvider
{
	public static final DirectionProperty FACING = BlockHorizontal.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = BlockRedstoneTorch.LIT;
	
	public static final int GUI_ID = 1;	// TODO replace with constant or enum as necessary

	protected BlockElectricFurnace(Properties props)
	{
		super(props);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, EnumFacing.NORTH).with(LIT, Boolean.valueOf(false)));
	}

	/**
	* Amount of light emitted
	* @deprecated prefer calling {@link IBlockState#getLightValue()}
	*/
	public int getLightValue(IBlockState state)
	{
			return state.get(LIT) ? super.getLightValue(state) : 0;
	}

	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (worldIn.isRemote)
		{
			return true;
		}
		else
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof IInteractionObject && player instanceof EntityPlayerMP)
			{
				//player.displayGUIChest((TileEntityElectricFurnace)tileentity);
				//player.addStat(StatList.INTERACT_WITH_FURNACE);
				NetworkHooks.openGui((EntityPlayerMP)player, (IInteractionObject)tileentity, pos);
			}

			return true;
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return createTileEntity(worldIn, getDefaultState());
	}

	public TileEntity createTileEntity(IBlockReader world, IBlockState state)
	{
		return new TileEntityElectricFurnace();
	}
	 
	 /**
	  * Called by ItemBlocks after a block is set in the world, to allow post-place logic
	  */
	 public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	 {
		 if (stack.hasDisplayName())
		 {
			 TileEntity tileentity = worldIn.getTileEntity(pos);
			 if (tileentity instanceof TileEntityElectricFurnace)
			 {
				 //TODO ((TileEntityElectricFurnace)tileentity).setCustomName(stack.getDisplayName());
			 }
		 }

	 }

	 public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving)
	 {
		 if (state.getBlock() != newState.getBlock())
		 {
			 TileEntity tileentity = worldIn.getTileEntity(pos);
			 if (tileentity instanceof TileEntityElectricFurnace)
			 {
				 // TODO InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityElectricFurnace)tileentity);
				 worldIn.updateComparatorOutputLevel(pos, this);
			 }

			 super.onReplaced(state, worldIn, pos, newState, isMoving);
		 }
	 }

	 /**
	  * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
	  * is fine.
	  */
	 public boolean hasComparatorInputOverride(IBlockState state) {
		 return true;
	 }

	 /**
	  * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
	  * Implementing/overriding is fine.
	  */
	 public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		 return Container.calcRedstone(worldIn.getTileEntity(pos));
	 }
	
	
	
	///***** BLOCKSTATE BOILERPLATE *****///

	/**
	* Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	* blockstate.
	* @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
	* fine.
	*/
	public IBlockState rotate(IBlockState state, Rotation rot)
	{
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	/**
	* Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	* blockstate.
	* @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
	*/
	public IBlockState mirror(IBlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}
	 
	 public EnumFacing getFacingOfBlockState(IBlockState state)
	 {
	 	return state.get(FACING);
	 }

	 /**
	  * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	  * IBlockstate
	  */
	 @Override
	 public IBlockState getStateForPlacement(BlockItemUseContext context)
	 {
		  return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	 }
	 
	 @Override
	 protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
	 {
		  builder.add(FACING);
		  builder.add(LIT);
	 }
}
