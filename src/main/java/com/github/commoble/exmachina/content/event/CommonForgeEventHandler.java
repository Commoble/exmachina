package com.github.commoble.exmachina.content.event;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.ResourceLocations;
import com.github.commoble.exmachina.content.capability.PlinthsInChunkCapability;

import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=ExMachinaMod.MODID, bus=Bus.FORGE)
public class CommonForgeEventHandler
{
	@SubscribeEvent
	public static void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		IWorld world = event.getWorld();
	}
	
	@SubscribeEvent
	public static void onAttachCapabilities(AttachCapabilitiesEvent<Chunk> event)
	{
		event.addCapability(ResourceLocations.PLINTHS_IN_CHUNK, new PlinthsInChunkCapability());
	}
}
