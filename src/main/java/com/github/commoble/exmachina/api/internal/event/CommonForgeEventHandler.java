package com.github.commoble.exmachina.api.internal.event;

import java.util.EnumSet;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.api.circuit.CircuitHelper;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=ExMachinaMod.MODID, bus=Bus.FORGE)
public class CommonForgeEventHandler
{
	@SubscribeEvent
	// runs on the server when a block is updated in a manner that notifies its neighbors
	public static void onNeighborNotify(NeighborNotifyEvent event)
	{
		BlockPos pos = event.getPos();
		IWorld world = event.getWorld();
		
		EnumSet<Direction> sides = event.getNotifiedSides();
		for (Direction side : sides)
		{
			CircuitHelper.onCircuitNeighborChanged(world, pos.offset(side));
		}
		
		for (Direction side : sides)
		{
			CircuitHelper.revalidateCircuitAt(world, pos.offset(side));
		}
	}
}
