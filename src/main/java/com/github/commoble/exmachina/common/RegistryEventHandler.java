package com.github.commoble.exmachina.common;

import com.github.commoble.exmachina.common.block.BlockRegistrar;
import com.github.commoble.exmachina.common.item.ItemRegistrar;
import com.github.commoble.exmachina.common.sound.SoundLedger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Event handler for registering Blocks, Enchantments, Items, Potions, SoundEvents, and Biomes
 * @author Joseph
 *
 */
@Mod.EventBusSubscriber(modid = ExMachinaMod.MODID)
public class RegistryEventHandler
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
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		SoundLedger.registerSounds();
	}
}
