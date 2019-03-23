package com.github.commoble.exmachina.common.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;

// copying a lot from ContainerFurnace here
public class ContainerElectricFurnace extends ContainerFurnace
{

	public ContainerElectricFurnace(InventoryPlayer playerInventory, IInventory furnaceInventory)
	{
		super(playerInventory, furnaceInventory);
	}
	
}
