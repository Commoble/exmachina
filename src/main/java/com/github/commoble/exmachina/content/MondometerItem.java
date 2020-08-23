package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.api.CircuitManager;
import com.github.commoble.exmachina.api.CircuitManagerCapability;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MondometerItem extends Item
{

	public MondometerItem(Properties props)
	{
		super(props);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		if (!world.isRemote)
		{
			context.getWorld().getCapability(CircuitManagerCapability.INSTANCE)
				.ifPresent(manager -> getAndSendInfo(manager, context));
		}
		
		return super.onItemUse(context);
	}

	public static void getAndSendInfo(CircuitManager manager, ItemUseContext context)
	{
		BlockPos pos = context.getPos();
		int size = manager.getCircuit(pos).map(circuit -> circuit.getComponentCache().size()).orElse(0);
		System.out.println(size);
	}
}
