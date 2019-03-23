package com.github.commoble.exmachina.common.tileentity;

import javax.annotation.Nullable;

import com.github.commoble.exmachina.common.ExMachinaMod;
import com.github.commoble.exmachina.common.block.BlockNames;
import com.github.commoble.exmachina.common.container.ContainerElectricFurnace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;

public class TileEntityElectricFurnace extends TileEntityFurnace implements IInteractionObject
{
//	public static final int ITEM_SLOT_COUNT = 3;
//	public static final String INVENTORY_NBT_NAME = "items";
	public static String GUI_ID = ExMachinaMod.MODID + ":" + BlockNames.ELECTRIC_FURNACE_NAME;
	private ITextComponent furnaceCustomName;
//	
//	private ItemStackHandler itemStackHandler = new ItemStackHandler(ITEM_SLOT_COUNT)
//	{
//		@Override
//		protected void onContentsChanged(int slot)
//		{
//			TileEntityElectricFurnace.this.markDirty();
//		}
//	};
	
//	private LazyOptional<? extends IItemHandler>[] handlers = S
	
//	public TileEntityElectricFurnace(TileEntityType<?> tileEntityTypeIn)
//	{
//		super(tileEntityTypeIn);
//		// TODO Auto-generated constructor stub
//	}
//	
//	public TileEntityElectricFurnace()
//	{
//		super(TileEntityRegistrar.teElectricFurnaceType);
//	}
//
//	@Override
//	protected void readDataFromNBTIntoTE(NBTTagCompound compound)
//	{
//		if (compound.hasKey(INVENTORY_NBT_NAME))
//		{
//			itemStackHandler.deserializeNBT((NBTTagCompound)compound.getTag(INVENTORY_NBT_NAME));
//		}
//	}
//
//	@Override
//	protected NBTTagCompound writeDataFromTEIntoNBT(NBTTagCompound compound)
//	{
//		compound.setTag(INVENTORY_NBT_NAME, itemStackHandler.serializeNBT());
//		return compound;
//	}

	////**** Capability stuff copied from furnace ****////
//	net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] handlers =
//	           net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH);

//   @Override
//   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable EnumFacing facing) {
//      if (!this.removed && facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//         if (facing == EnumFacing.UP)
//            return handlers[0].cast();
//         else if (facing == EnumFacing.DOWN)
//            return handlers[1].cast();
//         else
//            return handlers[2].cast();
//      }
//      return super.getCapability(capability, facing);
//   }

   /**
    * invalidates a tile entity
    */
//   @Override
//   public void remove() {
//      super.remove();
//      for (int x = 0; x < handlers.length; x++)
//        handlers[x].invalidate();
//   }
	
//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> capability, EnumFacing facing)
//	{
//		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
//		{
//			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
//		}
//		return super.getCapability(capability, facing);
//	}
//	@Override
//	@Nonnull
//	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing face)
//	{
//		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
//		{
//			return (LazyOptional<T>) LazyOptional.of(() -> new CapabilityItemHandler());
//		}
//		return super.getCapability(capability, face);
//	}
	
	////**** IInteractionObject boilerplate ****////

	@Override
	public ITextComponent getName()
	{
		// TODO Auto-generated method stub
		return (ITextComponent)(this.furnaceCustomName != null ? this.furnaceCustomName : new TextComponentTranslation("container.exmachina.electric_furnace"));
	}

	@Override
	public boolean hasCustomName()
	{
		// TODO Auto-generated method stub
		return this.furnaceCustomName != null;
	}

	@Override
	public ITextComponent getCustomName()
	{
		// TODO Auto-generated method stub
		return this.furnaceCustomName;
	}
	
	public void setCustomName(@Nullable ITextComponent name)
	{
		this.furnaceCustomName = name;
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player)
	{
		// TODO Auto-generated method stub
		return new ContainerElectricFurnace(player.inventory, this);
	}

	@Override
	public String getGuiID()
	{
		// TODO Auto-generated method stub
		return TileEntityElectricFurnace.GUI_ID;
	}
}
