package com.github.commoble.exmachina.content.block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.commoble.exmachina.api.circuit.BlockContext;
import com.github.commoble.exmachina.api.circuit.WireProperties;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CubeWireProperties extends WireProperties
{
	public CubeWireProperties(double wireResistance)
	{
		super(wireResistance);
	}
	
	@Override
	public Set<BlockPos> getAllowedConnections(BlockContext context)
	{
		BlockPos pos = context.pos;
		return Arrays.stream(Direction.values())
			.map(dir -> pos.offset(dir))
			//.filter(newPos -> newPos.getY() >= 0 && newPos.getY() < world.getActualHeight())
			.collect(Collectors.toCollection(HashSet::new));
	}
}
