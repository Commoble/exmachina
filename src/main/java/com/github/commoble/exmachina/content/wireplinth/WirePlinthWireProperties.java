package com.github.commoble.exmachina.content.wireplinth;

import java.util.Collections;
import java.util.Set;

import com.github.commoble.exmachina.api.circuit.WireProperties;
import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class WirePlinthWireProperties extends WireProperties
{

	public WirePlinthWireProperties(double wireResistance)
	{
		super(wireResistance);
	}

	@Override
	public Set<BlockPos> getAllowedConnections(IWorld world, BlockContext context)
	{
		return WirePlinthTileEntity.getPlinth(world, context.pos)
			.map(WirePlinthTileEntity::getConnections)
			.orElse(Collections.emptySet());
	}

}
