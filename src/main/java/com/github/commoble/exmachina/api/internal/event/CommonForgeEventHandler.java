package com.github.commoble.exmachina.api.internal.event;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.api.circuit.CircuitHelper;
import com.github.commoble.exmachina.api.circuit.ComponentRegistry;
import com.github.commoble.exmachina.api.circuit.ExtantCircuits;
import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=ExMachinaMod.MODID, bus=Bus.FORGE)
public class CommonForgeEventHandler
{
	@SubscribeEvent
	// runs on the server when a block is updated in a manner that notifies its neighbors
	// this fires AFTER a block is set in the world
	public static void onNeighborNotify(NeighborNotifyEvent event)
	{		
		// If this position used to have a circuit block but no longer does, invalidate circuit
		BlockPos pos = event.getPos();
		if (ExtantCircuits.doesValidCircuitExistAt(pos)
			&& !ComponentRegistry.contains(event.getState().getBlock()))
		{
			CircuitHelper.onCircuitBlockRemoved(event.getWorld(), pos);
		}
	}
	
	@SubscribeEvent
	public static void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		BlockState state = event.getState();
		if (ComponentRegistry.contains(state.getBlock()))
		{
			CircuitHelper.onCircuitBlockAdded(event.getWorld(), new BlockContext(state, event.getPos()));
		}
	}
}
