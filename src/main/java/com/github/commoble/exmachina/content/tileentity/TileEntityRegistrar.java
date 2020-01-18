package com.github.commoble.exmachina.content.tileentity;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.RegistryNames;
import com.github.commoble.exmachina.ResourceLocations;
import com.github.commoble.exmachina.content.block.BlockRegistrar;
import com.github.commoble.exmachina.util.RegistryHelper;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExMachinaMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(RegistryNames.BATTERY)
	public static TileEntityType<TileEntityBattery> teBatteryType;
	
	@ObjectHolder(RegistryNames.LIGHTBULB)
	public static TileEntityType<TileEntityLightbulb> teLightbulbType;
	
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		IForgeRegistry<TileEntityType<?>> reg = event.getRegistry();
		RegistryHelper.register(reg, ResourceLocations.BATTERY, TileEntityType.Builder.create(TileEntityBattery::new, BlockRegistrar.battery).build(null));
		RegistryHelper.register(reg, ResourceLocations.LIGHTBULB, TileEntityType.Builder.create(TileEntityLightbulb::new, BlockRegistrar.lightbulb).build(null));
	}
}
