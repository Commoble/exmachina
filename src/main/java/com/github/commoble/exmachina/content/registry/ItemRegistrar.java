package com.github.commoble.exmachina.content.registry;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.RegistryNames;
import com.github.commoble.exmachina.content.ResourceLocations;
import com.github.commoble.exmachina.content.item.MondometerItem;
import com.github.commoble.exmachina.content.item.WireSpoolItem;
import com.github.commoble.exmachina.content.util.RegistryHelper;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering items and BlockItems, and keeping their references
 * also handle creative tabs since those are closely related to items
 */
@ObjectHolder(ExMachinaMod.MODID)
public class ItemRegistrar
{
	//BlockItems

	@ObjectHolder(RegistryNames.BATTERY)
	public static final Item battery = null;
	
	@ObjectHolder(RegistryNames.LIGHTBULB)
	public static final Item lightbulb = null;
	
	// real items
	@ObjectHolder(RegistryNames.MONDOMETER)
	public static final Item mondometer = null;
	
	@ObjectHolder(RegistryNames.WIRE_SPOOL)
	public static final Item WireSpoolItem = null;
	
	
	public static void registerItems(IForgeRegistry<Item> registry)
	{
		// BlockItems
		RegistryHelper.register(registry, ResourceLocations.BATTERY, new BlockItem(BlockRegistrar.battery, new Item.Properties().group(CreativeTabs.tab)));
		RegistryHelper.register(registry, ResourceLocations.LIGHTBULB, new BlockItem(BlockRegistrar.lightbulb, new Item.Properties().group(CreativeTabs.tab)));
		RegistryHelper.register(registry, ResourceLocations.WIRE_PLINTH, new BlockItem(BlockRegistrar.wire_plinth, new Item.Properties().group(CreativeTabs.tab)));
		
		// real items
		RegistryHelper.register(registry, ResourceLocations.MONDOMETER, new MondometerItem(new Item.Properties().group(CreativeTabs.tab).maxStackSize(1)));
		RegistryHelper.register(registry, ResourceLocations.WIRE_SPOOL, new WireSpoolItem(new Item.Properties().group(CreativeTabs.tab).maxStackSize(1)));
	}
}
