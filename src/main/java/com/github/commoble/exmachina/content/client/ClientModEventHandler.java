package com.github.commoble.exmachina.content.client;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.registry.TileEntityRegistrar;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid=ExMachinaMod.MODID, value= {Dist.CLIENT}, bus = Bus.MOD)
public class ClientModEventHandler
{
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.wire_plinth, WirePlinthRenderer::new);
	}
}
