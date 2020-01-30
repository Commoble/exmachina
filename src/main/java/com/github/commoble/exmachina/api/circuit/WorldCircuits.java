package com.github.commoble.exmachina.api.circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class WorldCircuits
{
	private final Map<BlockPos, Circuit> CIRCUITS = new HashMap<>();
	
	public Circuit getCircuit(BlockPos pos)
	{
		return this.CIRCUITS.get(pos);
	}
	
	/**
	 * Add a newly constructed circuit to the known extant circuits
	 * @param circuit
	 */
	public void addCircuit(Circuit circuit)
	{
		Set<BlockPos> newPositions = circuit.getAllPositions();
		
		// make sure that any old circuits are cleaned up
		// get colliding positions -- present in existing map, but don't point to the new circuit
		HashSet<BlockPos> collidingPositions = newPositions.stream()
			.filter(newPos -> this.CIRCUITS.containsKey(newPos) && !circuit.equals(this.CIRCUITS.get(newPos)))
			.collect(Collectors.toCollection(HashSet::new));
		
		// remove circuits from map
		HashSet<Circuit> badCircuits = collidingPositions.stream()
			.map(pos -> this.CIRCUITS.remove(pos))
			.filter(oldCircuit -> oldCircuit != null)
			.collect(Collectors.toCollection(HashSet::new));
		
		// invalidate old circuits
		badCircuits.forEach(Circuit::invalidate);
		
		// add new circuit to map
		newPositions.forEach(pos -> this.CIRCUITS.put(pos, circuit));
	}
	
	public void invalidateCircuitAt(BlockPos pos)
	{
		Circuit oldCircuit = this.CIRCUITS.remove(pos);
		if (oldCircuit != null)
		{
			oldCircuit.invalidate();
		}
	}
	
	public boolean doesValidCircuitExistAt(BlockPos pos)
	{
		Circuit circuit = this.CIRCUITS.get(pos);
		return (circuit != null && circuit.isValid());
	}
	
	public Optional<CircuitElement> getElement(IWorld world, BlockContext context)
	{
		if (context == null
			|| !ComponentRegistry.ELEMENTS.containsKey(context.state.getBlock()))
		{
			return Optional.empty();
		}
		
		CircuitHelper.validateCircuitAt(world, context);
		
		return Optional.ofNullable(this.CIRCUITS.get(context.pos))
			.map(circuit -> circuit.components.get(context.pos));
	}
}
