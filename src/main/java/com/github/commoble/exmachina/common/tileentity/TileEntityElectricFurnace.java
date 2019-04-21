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
