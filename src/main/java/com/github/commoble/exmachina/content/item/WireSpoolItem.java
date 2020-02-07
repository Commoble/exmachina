package com.github.commoble.exmachina.content.item;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WireSpoolItem extends Item
{
	public static final String LAST_PLINTH_POS = "last_plinth_pos";
	
	public WireSpoolItem(Properties properties)
	{
		super(properties);
	}

	/**
	 * Called when this item is used when targetting a Block
	 */
	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		return WirePlinthTileEntity.getPlinth(world, pos)
			.map(plinth -> this.onUseOnPlinth(world, pos, plinth, context.getItem()))
			.orElseGet(() -> super.onItemUse(context));
	}
	
	private ActionResultType onUseOnPlinth(World world, BlockPos pos, @Nonnull WirePlinthTileEntity plinth, ItemStack stack)
	{
		if (!world.isRemote)
		{
			CompoundNBT nbt = stack.getChildTag(LAST_PLINTH_POS);
			
			if (nbt == null)
			{
				stack.setTagInfo(LAST_PLINTH_POS, NBTUtil.writeBlockPos(pos));
			}
			else // existing position stored in stack
			{
				BlockPos lastPos = NBTUtil.readBlockPos(nbt);
				// if player clicked the same plinth twice, clear the last-used-position
				if (lastPos.equals(pos))
				{
					stack.removeChildTag(LAST_PLINTH_POS);
				}
				// if plinth was already connected to the other position, remove connections
				else if (plinth.hasRemoteConnection(lastPos))
				{
					WirePlinthTileEntity.removeConnection(world, pos, lastPos);
					stack.removeChildTag(LAST_PLINTH_POS);
				}
				// if plinth wasn't connected, connect them if they're close enough
				else if (pos.withinDistance(lastPos, ExMachinaMod.config.max_plinth_connection_range.get()))
				{
					stack.removeChildTag(LAST_PLINTH_POS);
					WirePlinthTileEntity.getPlinth(world, lastPos)
						.ifPresent(lastPlinth -> WirePlinthTileEntity.addConnection(world, plinth, lastPlinth));
						
				}
				else	// too far away, initiate a new connection from here
				{
					stack.setTagInfo(LAST_PLINTH_POS, NBTUtil.writeBlockPos(pos));
					// TODO give feedback to player
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote)
		{
			double maxDistance = ExMachinaMod.config.max_plinth_connection_range.get();
			Optional.ofNullable(stack.getChildTag(LAST_PLINTH_POS))
				.map(nbt -> NBTUtil.readBlockPos(nbt))
				.filter(pos -> entityIn.getPositionVec().squareDistanceTo((new Vec3d(pos).add(0.5,0.5,0.5))) > maxDistance*maxDistance)
				.ifPresent(pos -> stack.removeChildTag(LAST_PLINTH_POS));;
		}
	}
}
