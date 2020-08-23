package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.api.CircuitManagerCapability;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class MondometerItem extends Item
{

	public MondometerItem(Properties props)
	{
		super(props);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		BlockPos pos = context.getPos();
		Chunk chunk = context.getWorld().getChunkAt(pos);
		chunk.getCapability(CircuitManagerCapability.INSTANCE).
		
		return super.onItemUse(context);
	}

	
}
