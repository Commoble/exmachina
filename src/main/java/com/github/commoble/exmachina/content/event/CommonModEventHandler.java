package com.github.commoble.exmachina.content.event;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.block.BlockRegistrar;
import com.github.commoble.exmachina.content.item.ItemRegistrar;
import com.github.commoble.exmachina.content.tileentity.TileEntityRegistrar;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Event handler for registering Blocks, Enchantments, Items, Potions, SoundEvents, and Biomes
 * @author Joseph
 *
 */
@Mod.EventBusSubscriber(modid = ExMachinaMod.MODID, bus=Bus.MOD)
public class CommonModEventHandler
{
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		BlockRegistrar.registerBlocks(event);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		ItemRegistrar.registerItems(event);
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
	{
		TileEntityRegistrar.registerTileEntities(event);
	}
	
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event)
	{

	}
	
	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event)
	{
		BlockRegistrar.defineBlockCategories();
	}
}
