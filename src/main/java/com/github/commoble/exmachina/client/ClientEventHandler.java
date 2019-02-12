package com.github.commoble.exmachina.client;

import com.github.commoble.exmachina.common.ExMachinaMod;
import com.github.commoble.exmachina.common.item.ItemRegistrar;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ExMachinaMod.MODID)
public class ClientEventHandler
{
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent e)
	{
		//registerItemRenderer(ItemRegistrar.itemBlockTransporter);
		registerItemRenderer(ItemRegistrar.ash);
		
		// itemblocks
		registerItemRenderer(ItemRegistrar.battery);
		registerItemRenderer(ItemRegistrar.lightbulb);
		registerItemRenderer(ItemRegistrar.wire);
		
		// real items
		registerItemRenderer(ItemRegistrar.mondometer);
	}
	
	private static void registerItemRenderer(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
