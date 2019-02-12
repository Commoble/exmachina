package com.github.commoble.exmachina.common.tileentity;

import com.github.commoble.exmachina.common.ExMachinaMod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileEntityRegistrar
{
	public static void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityBattery.class, new ResourceLocation(ExMachinaMod.MODID, "te_battery"));
		GameRegistry.registerTileEntity(TileEntityLightbulb.class, new ResourceLocation(ExMachinaMod.MODID, "te_lightbulb"));
		
	}
}
