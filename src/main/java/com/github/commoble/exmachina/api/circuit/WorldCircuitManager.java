package com.github.commoble.exmachina.api.circuit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;

public class WorldCircuitManager
{
	private static final Map<DimensionType, WorldCircuits> CIRCUITS_BY_WORLD = new HashMap<>();
	
	public static WorldCircuits getCircuitsForWorld(IWorld world)
	{
		DimensionType key = world.getDimension().getType();
		return CIRCUITS_BY_WORLD.computeIfAbsent(key, dim -> new WorldCircuits());
	}
	
	public static Circuit getCircuit(IWorld world, BlockPos pos)
	{
		return getCircuitsForWorld(world).getCircuit(pos);
	}
	
	public static void addCircuit(IWorld world, Circuit circuit)
	{
		getCircuitsForWorld(world).addCircuit(circuit);
	}
	
	public static void invalidateCircuitAt(IWorld world, BlockPos pos)
	{
		getCircuitsForWorld(world).invalidateCircuitAt(pos);
	}
	
	public static boolean doesValidCircuitExistAt(IWorld world, BlockPos pos)
	{
		return getCircuitsForWorld(world).doesValidCircuitExistAt(pos);
	}
	
	public static Optional<CircuitElement> getElement(IWorld world, BlockContext context)
	{
		return getCircuitsForWorld(world).getElement(world, context);
	}
}
