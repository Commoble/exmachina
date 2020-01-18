package com.github.commoble.exmachina.content.item;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.RegistryNames;
import com.github.commoble.exmachina.content.block.BlockRegistrar;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
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
	
	@ObjectHolder(RegistryNames.WIRE)
	public static final Item wire = null;
	
	// real items
	@ObjectHolder("exmachina:mondometer")
	public static final Item mondometer = null;
	
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		
		// BlockItems
		registerItem(registry, new BlockItem(BlockRegistrar.battery, new Item.Properties().group(CreativeTabs.tab)), RegistryNames.BATTERY);
		registerItem(registry, new BlockItem(BlockRegistrar.wire, new Item.Properties().group(CreativeTabs.tab)), RegistryNames.WIRE);
		registerItem(registry, new BlockItem(BlockRegistrar.lightbulb, new Item.Properties().group(CreativeTabs.tab)), RegistryNames.LIGHTBULB);
		
		// real items
		registerItem(registry, new ItemMondometer(new Item.Properties().group(CreativeTabs.tab).maxStackSize(1)), ItemNames.MONDOMETER_NAME);
	}
	
	private static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T newItem, String name)
	{
		String prefixedName = ExMachinaMod.MODID + ":" + name;
		newItem.setRegistryName(prefixedName);
		registry.register(newItem);
		return newItem;
	}
}
