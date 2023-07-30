package commoble.exmachina.circuit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.exmachina.api.BlockComponent;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.Connector;
import commoble.exmachina.api.Connector.BlockConnector;
import commoble.exmachina.api.Connector.StateConnector;
import commoble.exmachina.api.ExMachinaRegistries;
import commoble.exmachina.api.StateComponent;
import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticProperty.BakedStaticProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ComponentBaker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final ComponentBaker INSTANCE = new ComponentBaker();
	public static ComponentBaker get() { return INSTANCE; }
	
	private int generation = 0;
	private Object2ObjectMap<Block, BlockComponent> bakedBlockComponents = null;
	private Object2ObjectMap<BlockState, StateComponent> bakedStateComponents = new Object2ObjectOpenHashMap<>();
	
	public void clear()
	{
		this.generation++;
		this.bakedBlockComponents = null;
		this.bakedStateComponents = new Object2ObjectOpenHashMap<>();
	}
	
	public int generation()
	{
		return this.generation;
	}
	
	public void preBake(RegistryAccess registries)
	{
		this.clear();
		this.getBlockComponents(registries);
	}

	private Object2ObjectMap<Block, BlockComponent> getBlockComponents(RegistryAccess registries)
	{
		if (this.bakedBlockComponents == null)
		{
			this.bakedBlockComponents = this.bakeBlockComponents(registries);
		}
		return this.bakedBlockComponents;
	}
	
	public StateComponent getComponent(BlockState state, RegistryAccess registries)
	{
		return this.bakedStateComponents.computeIfAbsent(state, (BlockState s) -> this.bakeStateComponent(s, registries));
	}

	private Object2ObjectMap<Block, BlockComponent> bakeBlockComponents(RegistryAccess registries)
	{
		Object2ObjectMap<Block, BlockComponent> map = new Object2ObjectOpenHashMap<>();
		
		var components = registries.registryOrThrow(ExMachinaRegistries.CIRCUIT_COMPONENT);
		for (var entry : components.entrySet())
		{
			ResourceLocation blockId = entry.getKey().location();
			CircuitComponent circuitComponent = entry.getValue();
			
			// blocks with invalid connectors are ignored by the circuit builder
			if (!circuitComponent.connector().isPresent())
				continue;
			
			@SuppressWarnings("deprecation")
			Block block = BuiltInRegistries.BLOCK.get(blockId);
			// make sure block exists (and isn't air, air can never have components)
			if (block == Blocks.AIR)
			{
				LOGGER.error("Missing or invalid block: " + blockId);
				continue;
			}
			
			BlockComponent blockComponent = this.bakeBlockComponent(block, circuitComponent);
			if (blockComponent.isPresent())
			{
				map.put(block, blockComponent);
			}
		}
		return map;
	}
	
	private BlockComponent bakeBlockComponent(Block block, CircuitComponent circuitComponent)
	{
		BlockConnector blockConnector = this.bakeBlockConnector(block, circuitComponent.connector());
		if (!blockConnector.isPresent())
		{
			return BlockComponent.EMPTY;
		}
		BakedStaticProperty staticLoad = this.bakeStaticProperty(block, circuitComponent.staticLoad());
		BakedStaticProperty staticSource = this.bakeStaticProperty(block, circuitComponent.staticSource());
		
		return new BlockComponent(
			blockConnector,
			staticLoad,
			staticSource,
			circuitComponent.dynamicLoad(),
			circuitComponent.dynamicSource());
	}
	
	private StateComponent bakeStateComponent(BlockState state, RegistryAccess registries)
	{
		var components = this.getBlockComponents(registries);
		BlockComponent blockComponent = components.getOrDefault(state.getBlock(), BlockComponent.EMPTY);
		if (!blockComponent.isPresent())
			return StateComponent.EMPTY;
		
		StateConnector stateConnector = blockComponent.connector().getStateConnector(state);
		if (!stateConnector.isPresent())
			return StateComponent.EMPTY;

		return new StateComponent(
			stateConnector,
			blockComponent.staticLoad().getValue(state),
			blockComponent.staticSource().getValue(state),
			blockComponent.dynamicLoad(),
			blockComponent.dynamicSource());
	}
	
	private BlockConnector bakeBlockConnector(Block block, Connector connector)
	{
		if (!connector.isPresent())
			return BlockConnector.EMPTY;
		
		return connector.bake(block)
			.resultOrPartial(LOGGER::error)
			.orElse(BlockConnector.EMPTY);
	}
	
	private BakedStaticProperty bakeStaticProperty(Block block, StaticProperty prop)
	{
		if (!prop.isPresent())
			return BakedStaticProperty.EMPTY;
		
		return prop.bake(block)
			.resultOrPartial(LOGGER::error)
			.orElse(BakedStaticProperty.EMPTY);
	}
}
