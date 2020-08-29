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
import commoble.exmachina.data.DefinedCircuitComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;

public class CircuitBuilder
{
	public static LazyOptional<Circuit> attemptToBuildCircuitFrom(IWorld world, BlockPos pos)
	{
		Map<Block, DefinedCircuitComponent> data = ExMachina.INSTANCE.circuitElementDataManager.data;
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		DefinedCircuitComponent element = data.get(block);
		if (element == null || element.connector == null)
		{
			return WorldCircuitManager.EMPTY_CIRCUIT; // block must have element data with a connector to be part of a circuit
		}
		else
		{
			Map<BlockPos, Pair<BlockState, DefinedCircuitComponent>> partialCircuit = new HashMap<>();
			Deque<ElementContext> uncheckedConnectedElements = new LinkedList<>();
			uncheckedConnectedElements.add(new ElementContext(pos, state, element));
			partialCircuit.put(pos, Pair.of(state, element));

			// traverse all blocks connectable to this starting block and assemble the partial circuit
			// avoid recursive graph solving so we don't cause stack overflows with large networks
			
			while (!uncheckedConnectedElements.isEmpty())
			{
				ElementContext nextContext = uncheckedConnectedElements.remove();
				BlockPos nextPos = nextContext.pos;

				Set<BlockPos> possibleConnections = nextContext.element.connector.apply(world, nextPos);
				
				for (BlockPos possibleConnectedPos : possibleConnections)
				{
					// don't look for connections to positions we've already looked at
					if (!partialCircuit.containsKey(possibleConnectedPos))
					{
						BlockState connectedState = world.getBlockState(possibleConnectedPos);
						Block connectedBlock = connectedState.getBlock();
						DefinedCircuitComponent connectedElement = data.get(connectedBlock);
						if (connectedElement != null && connectedElement.connector != null)
						{
							if (connectedElement.connector.apply(world, possibleConnectedPos).contains(nextPos))
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
	
	public static LazyOptional<Circuit> buildCircuit(IWorld world, Map<BlockPos, Pair<BlockState, DefinedCircuitComponent>> components)
	{
		double totalStaticLoad = 0D;
		double totalStaticSource = 0D;
		
		List<DoubleSupplier> dynamicLoads = new ArrayList<>();
		List<DoubleSupplier> dynamicSources = new ArrayList<>();
		Multiset<Pair<BlockState, DefinedCircuitComponent>> stateCounter = HashMultiset.create();
		
		// iterate over the individual positions, cache dynamic getters, build the state/element counter
		for (java.util.Map.Entry<BlockPos, Pair<BlockState, DefinedCircuitComponent>> entry : components.entrySet())
		{
			Pair<BlockState, DefinedCircuitComponent> pair = entry.getValue();
			BlockPos pos = entry.getKey();
			BlockState state = pair.getLeft();
			DefinedCircuitComponent element = pair.getRight();
			stateCounter.add(pair);
			
			element.dynamicLoad.ifPresent(dynamicProperty -> dynamicLoads.add(() -> dynamicProperty.getValue(world, pos, state)));
			element.dynamicSource.ifPresent(dynamicProperty -> dynamicSources.add(() -> dynamicProperty.getValue(world, pos, state)));
		}
		// calculate static values once per state and multiplying them by the number of states
		// should make the calculations for e.g. a 100x100x100 cube of wires nicer
		// also need to find at least one source and at least one non-wire load to build a valid circuit
		boolean hasLoad = false;
		boolean hasSource = false;
		for (Multiset.Entry<Pair<BlockState, DefinedCircuitComponent>> entry : stateCounter.entrySet())
		{
			Pair<BlockState, DefinedCircuitComponent> pair = entry.getElement();
			BlockState state = pair.getLeft();
			DefinedCircuitComponent element = pair.getRight();
			int count = entry.getCount();
			double staticLoad = element.staticLoad.applyAsDouble(state);
			double staticSource = element.staticSource.applyAsDouble(state);
			if (staticLoad > 0 || element.dynamicLoad.isPresent())
			{
				hasLoad = true;
			}
			if (staticSource > 0 || element.dynamicSource.isPresent())
			{
				hasSource = true;
			}
			totalStaticSource += count * staticSource;
			totalStaticLoad += count * staticLoad;
		}
		
		if (hasLoad && hasSource)
		{
			Circuit circuit = new CircuitImpl(world, totalStaticLoad, totalStaticSource, components, dynamicLoads, dynamicSources);
			return LazyOptional.of(() -> circuit);
		}
		else
		{
			return WorldCircuitManager.EMPTY_CIRCUIT;
		}
	}
	
	public static class ElementContext
	{
		public final @Nonnull BlockPos pos;
		public final @Nonnull BlockState state;
		public final @Nonnull DefinedCircuitComponent element;
		
		public ElementContext(@Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull DefinedCircuitComponent element)
		{
			this.pos = pos;
			this.state = state;
			this.element = element;
		}
	}
}
