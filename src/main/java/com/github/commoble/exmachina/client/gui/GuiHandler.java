package com.github.commoble.exmachina.client.gui;

import com.github.commoble.exmachina.common.tileentity.TileEntityElectricFurnace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;

public class GuiHandler
{
	
	public static GuiScreen getClientGuiElement(FMLPlayMessages.OpenContainer msg)
	{
		if (msg.getId().toString().equals(TileEntityElectricFurnace.GUI_ID))
		{
			EntityPlayerSP player = Minecraft.getInstance().player;
			World world = player.world;
			TileEntity te = world.getTileEntity(msg.getAdditionalData().readBlockPos());
			if (te instanceof TileEntityElectricFurnace)
			{
				return new GuiElectricFurnace(player.inventory, (TileEntityElectricFurnace)te);
			}
		}
		return null;
	}

}
