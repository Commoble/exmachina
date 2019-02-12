package com.github.commoble.exmachina.client;

import com.github.commoble.exmachina.common.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
* CombinedClient is used to set up the mod and start it running when installed on a normal minecraft client.
* It should not contain any code necessary for proper operation on a DedicatedServer.
* Code required for both normal minecraft client and dedicated server should go into CommonProxy.
* 
* All client-side-specific things (rendering and textures, mostly) goes in here
*/
public class CombinedClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);	// run CommonProxy's preInit first to get things registered
		
		// entity renderers
		//RenderingRegistry.registerEntityRenderingHandler(EntityExplodingChicken.class, RenderExplodingChicken.FACTORY);
		
		// tile entity renderers
	}
	
	@Override
	public void load(FMLInitializationEvent event)
	{
		super.load(event);
		
		// register item renderers here -- the mesher hasn't been initialized yet in preinit
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
