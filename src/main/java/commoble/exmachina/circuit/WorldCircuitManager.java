package commoble.exmachina.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import commoble.exmachina.ExMachina;
import commoble.exmachina.api.Circuit;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.CircuitManager;
import commoble.exmachina.api.CircuitManagerCapability;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class WorldCircuitManager implements CircuitManager, ICapabilityProvider
{
	public static final Optional<Circuit> EMPTY_CIRCUIT = Optional.empty();
	
	public final LazyOptional<CircuitManager> holder = LazyOptional.of(() -> this);
	
	private Map<BlockPos, Optional<Circuit>> circuitMap = new HashMap<>();
	private final World world;
	private int lastKnownGeneration = 0;
	
	public WorldCircuitManager(World world)
	{
		this.world = world;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CircuitManagerCapability.INSTANCE.orEmpty(cap, this.holder);
	}
	
	@Override
	public Optional<Circuit> getCircuit(BlockPos pos)
	{
		// if data has been reloaded, dump the circuit map
		int actualGeneration = ExMachina.INSTANCE.circuitElementDataManager.getCurrentGeneration();
		if (this.lastKnownGeneration != actualGeneration)
		{
			this.lastKnownGeneration = actualGeneration;
			this.circuitMap = new HashMap<>();
		}
		
		Optional<Circuit> existingCircuitHolder = this.circuitMap.getOrDefault(pos, EMPTY_CIRCUIT);
		if (existingCircuitHolder.isPresent())
		{
			return existingCircuitHolder;
		}
		else // try to build new circuit here if possible
		{
			Optional<Circuit> builtCircuitHolder = CircuitBuilder.attemptToBuildCircuitFrom(this.world, pos);
			// if we built a valid circuit, keep track of where it is
			builtCircuitHolder.ifPresent(circuit -> circuit.getComponentCache().keySet()
				.forEach(posInCircuit -> this.circuitMap.put(posInCircuit, builtCircuitHolder)));
			return builtCircuitHolder;
		}
	}

	@Override
	public void onBlockUpdate(BlockState newState, BlockPos updatedPos)
	{
		List<BlockPos> positionsToRemove = new ArrayList<>(); // no two circuit instances *should* share any blockpos
		Consumer<BlockPos> addPosToRemovalList = positionsToRemove::add;
		
		// first, if the block was in an extant circuit, mark the position for removal
		Optional<Circuit> circuitHolder = this.getCircuit(updatedPos);
		circuitHolder.ifPresent(circuit -> {
			Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components = circuit.getComponentCache();
			Pair<BlockState, ? extends CircuitComponent> cachedComponent = components.get(updatedPos);
			if (cachedComponent != null && cachedComponent.getLeft() != newState)
			{
				components.keySet().forEach(addPosToRemovalList);
			}
		});
		
		// then, if the new block is connectable, we check those connections,
		// and, for any circuit extant at those positions,
		// if the circuit
			// A) does not contain the updated position, and
			// B) has a mutual connection to the updated position from the connected position
		// then we mark the circuit for removal
			// if the circuit didn't connect to the old blockstate, and doesn't connect to the new one, we don't worry about it
			// if the circuit did connect to the old blockstate,
				// if the old blockstate changed, we handled it above
				// if the old blockstate didn't change, we ignore it and rely on the block's developer to
				// call a circuit invalidation if necessary
		CircuitComponent component = ExMachina.INSTANCE.circuitElementDataManager.data.get(newState.getBlock());
		if (component != null)
		{
			Set<BlockPos> connections = component.getConnector().apply(this.world, updatedPos, newState);
			for (BlockPos connectedPos : connections)
			{
				Optional<Circuit> connectedCircuitHolder = this.getCircuit(connectedPos);
				connectedCircuitHolder.ifPresent(connectedCircuit -> {
					Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> components = connectedCircuit.getComponentCache();
					// if updated block connects to an extant circuit that doesn't contain the updated block 
					if (!components.containsKey(updatedPos))
					{
						Pair<BlockState, ? extends CircuitComponent> pair = components.get(connectedPos);
						// and if the extant circuit can mutually connect to the new block
						if (pair != null && pair.getRight().getConnector().apply(this.world, connectedPos, pair.getLeft()).contains(updatedPos))
						{
							// mark the circuit for invalidation and removal from the manager
							// circuit won't be in the set of circuits to remove yet because it would have been invalidated already
							components.keySet().forEach(addPosToRemovalList);
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
		this.holder.invalidate();
	}

	@Override
	public void invalidateCircuit(BlockPos pos)
	{
		this.circuitMap.getOrDefault(pos, EMPTY_CIRCUIT).ifPresent(this::invalidateCircuitInstance);
	}
	
	private void invalidateCircuitInstance(@Nonnull Circuit circuit)
	{
		circuit.getComponentCache().keySet().forEach(this.circuitMap::remove);
	}
}
