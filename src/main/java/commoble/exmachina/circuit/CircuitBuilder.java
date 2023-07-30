package commoble.exmachina.circuit;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import commoble.exmachina.ExMachina;
import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.DynamicProperty;
import commoble.exmachina.api.StateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CircuitBuilder
{
	public static Circuit attemptToBuildCircuitFrom(LevelAccessor world, BlockPos pos)
	{
		ComponentBaker circuitBaker = ComponentBaker.get();
		RegistryAccess registries = world.registryAccess();
		BlockState state = world.getBlockState(pos);
		StateComponent element = circuitBaker.getComponent(state, registries);
		if (!element.isPresent())
		{
			return Circuit.empty(); // block must have element data with a connector to be part of a circuit
		}
		else
		{
			Map<BlockPos, Pair<BlockState, StateComponent>> partialCircuit = new HashMap<>();
			Deque<ElementContext> uncheckedConnectedElements = new LinkedList<>();
			uncheckedConnectedElements.add(new ElementContext(pos, state, element));
			// put the starting position in the partial circuit
			partialCircuit.put(pos, Pair.of(state, element));

			// traverse all blocks connectable to this starting block and assemble the partial circuit
			// avoid recursive graph solving so we don't cause stack overflows with large networks
			int maxSize = ExMachina.get().commonConfig.maxCircuitSize().get();
			
			while (!uncheckedConnectedElements.isEmpty() && partialCircuit.size() < maxSize)
			{
				// nextPos is already in the partial circuit
				ElementContext nextContext = uncheckedConnectedElements.remove();
				BlockPos nextPos = nextContext.pos;

				// get all the positions that nextPos points to
				Set<BlockPos> possibleConnections = nextContext.element.connector().connectedPositions(world, nextPos);
				
				for (BlockPos possibleConnectedPos : possibleConnections)
				{
					// don't look for connections to positions we've already looked at
					if (!partialCircuit.containsKey(possibleConnectedPos))
					{
						BlockState connectedState = world.getBlockState(possibleConnectedPos);
						StateComponent connectedElement = circuitBaker.getComponent(connectedState, registries);
						if (connectedElement.isPresent())
						{
							if (connectedElement.connector().connectedPositions(world, possibleConnectedPos).contains(nextPos))
							{
								// the two positions connect to each other
								// nextPos has been established as being part of the circuit
								// connected pos hasn't, so establish it and put it in the queue
								uncheckedConnectedElements.add(new ElementContext(possibleConnectedPos, connectedState, connectedElement));
								partialCircuit.put(possibleConnectedPos, Pair.of(connectedState, connectedElement));
								
							}
						}
					}
				}
				
			}
			
			// we've fully traversed the mutual connections, use the network map to build a circuit object
			return CircuitBuilder.buildCircuit(world, partialCircuit);
		}
	}
	
	public static Circuit buildCircuit(LevelAccessor world, Map<BlockPos, Pair<BlockState, StateComponent>> components)
	{
		double totalStaticLoad = 0D;
		double totalStaticSource = 0D;
		
		List<DoubleSupplier> dynamicLoads = new ArrayList<>();
		List<DoubleSupplier> dynamicSources = new ArrayList<>();
		Multiset<Pair<BlockState, StateComponent>> stateCounter = HashMultiset.create();
		
		// iterate over the individual positions, cache dynamic getters, build the state/element counter
		for (var entry : components.entrySet())
		{
			Pair<BlockState, StateComponent> pair = entry.getValue();
			BlockPos pos = entry.getKey();
			BlockState state = pair.getLeft();
			StateComponent element = pair.getRight();
			stateCounter.add(pair);
			
			DynamicProperty dynamicLoad = element.dynamicLoad();
			DynamicProperty dynamicSource = element.dynamicSource();
			if (element.dynamicLoad().isPresent())
			{
				dynamicLoads.add(() -> dynamicLoad.getValue(world, pos, state));
			}
			if (dynamicSource.isPresent())
			{
				dynamicSources.add(() -> dynamicSource.getValue(world, pos, state));
			}
		}
		// calculate static values once per state and multiplying them by the number of states
		// should make the calculations for e.g. a 100x100x100 cube of wires nicer
		// also need to find at least one source and at least one non-wire load to build a valid circuit
		boolean hasLoad = false;
		boolean hasSource = false;
		for (var entry : stateCounter.entrySet())
		{
			Pair<BlockState, StateComponent> pair = entry.getElement();
			StateComponent element = pair.getRight();
			int count = entry.getCount();
			double staticLoad = element.staticLoad();
			double staticSource = element.staticSource();
			// The circuit must have a static load, to prevent divide-by-0 strangeness
			if (staticLoad > 0)
			{
				hasLoad = true;
			}
			// The circuit must have some kind of source at some point or we shouldn't bother, though it doesn't have to be a static source
			if (staticSource > 0 || element.dynamicSource().isPresent())
			{
				hasSource = true;
			}
			totalStaticSource += count * staticSource;
			totalStaticLoad += count * staticLoad;
		}
		
		if (hasLoad && hasSource)
		{
			Circuit circuit = new CircuitImpl(world, totalStaticLoad, totalStaticSource, components, dynamicLoads, dynamicSources);
			return circuit;
		}
		else
		{
			return Circuit.empty();
		}
	}
	
	private static record ElementContext(@Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull StateComponent element)
	{
	}
}
