package com.github.commoble.exmachina.content.block;

import java.util.Arrays;
import java.util.Set;

import com.github.commoble.exmachina.api.circuit.WireProperties;
import com.github.commoble.exmachina.api.util.BlockContext;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CubeWireProperties extends WireProperties
{
	public CubeWireProperties(double wireResistance)
	{
		super(wireResistance);
	}
	
	@Override
	public Set<BlockPos> getAllowedConnections(IWorld world, BlockContext context)
	{
		BlockPos pos = context.pos;
		return Arrays.stream(Direction.values())
			.map(dir -> pos.offset(dir))
			//.filter(newPos -> newPos.getY() >= 0 && newPos.getY() < world.getActualHeight())
			.collect(ImmutableSet.toImmutableSet());
	}
}
