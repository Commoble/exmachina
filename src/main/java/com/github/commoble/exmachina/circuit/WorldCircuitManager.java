package com.github.commoble.exmachina.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.exmachina.ExMachina;
import com.github.commoble.exmachina.api.Circuit;
import com.github.commoble.exmachina.api.CircuitComponent;
import com.github.commoble.exmachina.api.CircuitManager;
import com.github.commoble.exmachina.api.CircuitManagerCapability;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class WorldCircuitManager implements CircuitManager, ICapabilityProvider
{
	public static final LazyOptional<Circuit> EMPTY_CIRCUIT = LazyOptional.empty();
	
	public final LazyOptional<CircuitManager> holder = LazyOptional.of(() -> this);
	
	private final Map<BlockPos, LazyOptional<Circuit>> circuitMap = new HashMap<>();
	private final World world;
	
	public WorldCircuitManager(World world)
	{
		this.world = world;
	}
	
	@Override
	public LazyOptional<Circuit> getCircuit(BlockPos pos)
	{
		LazyOptional<Circuit> existingCircuitHolder = this.circuitMap.getOrDefault(pos, EMPTY_CIRCUIT);
		if (existingCircuitHolder.isPresent())
		{
			return existingCircuitHolder;
		}
		else // try to build new circuit here if possible
		{
			LazyOptional<Circuit> builtCircuitHolder = CircuitBuilder.attemptToBuildCircuitFrom(this.world, pos);
			// if we built a valid circuit, keep track of where it is
			builtCircuitHolder.ifPresent(circuit -> circuit.getComponentCache().keySet()
				.forEach(posInCircuit -> this.circuitMap.put(posInCircuit, builtCircuitHolder)));
			return builtCircuitHolder;
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CircuitManagerCapability.INSTANCE.orEmpty(cap, this.holder);
	}

	@Override
	public void onBlockUpdate(BlockState newState, BlockPos updatedPos)
	{
		List<BlockPos> positionsToRemove = new ArrayList<>(); // no two circuit instances *should* share any blockpos
		Consumer<BlockPos> addPosToRemovalList = positionsToRemove::add;
		
		// first, if the block was in an extant circuit, we invalidate it if the blockstate changed
		LazyOptional<Circuit> circuitHolder = this.getCircuit(updatedPos);
		circuitHolder.ifPresent(circuit -> {
			Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components = circuit.getComponentCache();
			Pair<BlockState, ? extends CircuitComponent> cachedComponent = components.get(updatedPos);
			if (cachedComponent != null && cachedComponent.getLeft() != newState)
			{
				components.keySet().forEach(addPosToRemovalList);
				circuitHolder.invalidate();
			}
		});
		
		// then, if the new block is connectable, we check those connections,
		// and, for any circuit extant at those positions,
		// if the circuit
			// A) does not contain the updated position, and
			// B) has a mutual connection to the updated position from the connected position
		// then we invalidate that circuit
			// if the circuit didn't connect to the old blockstate, and doesn't connect to the new one, we don't worry about it
		// if the circuit did connect to the old blockstate, we invalidate the circuit if the new state
			// has mutual connections to any position not in that circuit
		CircuitComponent component = ExMachina.INSTANCE.circuitElementDataManager.data.get(newState.getBlock());
		if (component != null)
		{
			Set<BlockPos> connections = component.getConnector().apply(this.world, updatedPos);
			for (BlockPos connectedPos : connections)
			{
				LazyOptional<Circuit> connectedCircuitHolder = this.getCircuit(connectedPos);
				connectedCircuitHolder.ifPresent(connectedCircuit -> {
					Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components = connectedCircuit.getComponentCache();
					// if updated block connects to an extant circuit that doesn't contain the updated block 
					if (!components.containsKey(updatedPos))
					{
						Pair<BlockState, ? extends CircuitComponent> pair = components.get(connectedPos);
						// and if the extant circuit can mutually connect to the new block
						if (pair != null && pair.getRight().getConnector().apply(this.world, connectedPos).contains(updatedPos))
						{
							// mark the circuit for invalidation and removal from the manager
							// circuit won't be in the set of circuits to remove yet because it would have been invalidated already
							components.keySet().forEach(addPosToRemovalList);
							connectedCircuitHolder.invalidate();
						}
					}
					else // if updated block connects to an extant circuit that contains the updated block,
					{
						// if the updated block now has any mutual connections to blocks that the extant circuit does not contain,
						// invalidate that circuit
						for (BlockPos otherConnectedPos : connections)
						{
							if (!components.containsKey(otherConnectedPos))
							{
								BlockState otherConnectedState = this.world.getBlockState(otherConnectedPos);
								CircuitComponent otherConnectedComponent = ExMachina.INSTANCE.circuitElementDataManager.data.get(otherConnectedState.getBlock());
								if (otherConnectedComponent != null && !otherConnectedComponent.getConnector().apply(this.world, otherConnectedPos).contains(updatedPos))
								{
									components.keySet().forEach(addPosToRemovalList);
									connectedCircuitHolder.invalidate();
									break;
								}
							}
						}
					}
				});
			}
		}
		
		// then we remove any invalidated circuits from the map
		// (or the map will just get increasingly large until the world unloads)
		positionsToRemove.forEach(this.circuitMap::remove);
	}
	
	public void onCapabilityInvalidated()
	{
		for (LazyOptional<Circuit> circuitHolder : this.circuitMap.values())
		{
			circuitHolder.invalidate();
		}
		this.holder.invalidate();
	}
}
