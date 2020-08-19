package com.github.commoble.exmachina.client;

import com.github.commoble.exmachina.content.TileEntityRegistrar;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	// called from mod constructor if on physical client
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
//		ClientConfig.initClientConfig();
		
		modBus.addListener(ClientEvents::onClientSetup);
	}
	
	public static void onClientSetup(FMLClientSetupEvent event)
	{
//		RenderTypeLookup.setRenderLayer(BlockRegistrar.REDWIRE_POST.get(), RenderType.getCutout());

		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.WIRE_POST.get(), WirePostRenderer::new);
	}
}
