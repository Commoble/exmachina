package commoble.exmachina.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import commoble.exmachina.ExMachina;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.DynamicPropertyFactory;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.plugins.CircuitBehaviourRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class CircuitElementDataManager extends JsonReloadListener implements Supplier<Map<Block, ? extends CircuitComponent>>
{	
	public static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(StaticPropertyFactory.class, new ComponentPropertyTypeAdapter<StaticPropertyFactory>("static",
			id -> ExMachina.INSTANCE.circuitBehaviourRegistry.staticProperties.get(id),
			x -> block -> state -> x))
		.registerTypeAdapter(DynamicPropertyFactory.class, new ComponentPropertyTypeAdapter<DynamicPropertyFactory>("dynamic",
			id -> ExMachina.INSTANCE.circuitBehaviourRegistry.dynamicProperties.get(id),
			x -> block -> (world, pos, state) -> x))
		.registerTypeAdapter(ConnectorFactory.class, ConnectorFactoryTypeAdapter.INSTANCE)
		.create();
	
	// use a subfolder so we're less likely to conflict with other mods
	// i.e. this loads jsons at resources/data/modid/exmachina/circuit_elements/name.json
	public static final String FOLDER = "exmachina/circuit_elements";

	public Map<Block, DefinedCircuitComponent> data = new HashMap<>();
	
	public CircuitElementDataManager()
	{
		super(GSON, FOLDER);
	}

	@Override
	// the superclass parses all jsons for all modids in the above data folder
	// and compiles them into the given JsonElement map below
	// no merging is done here, so jsons with the same modid:name identifier are overwritten by datapack priority
	protected void apply(Map<ResourceLocation, JsonElement> jsons, IResourceManager resourceManager, IProfiler profiler)
	{
		Map<Block, DefinedCircuitComponent> newMap = new HashMap<>();
		CircuitBehaviourRegistry circuitBehaviourRegistry = ExMachina.INSTANCE.circuitBehaviourRegistry;
		for (java.util.Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet())
		{
			// gson the json element into an intermediary class
			RawCircuitElement raw = GSON.fromJson(entry.getValue(), RawCircuitElement.class);
			ResourceLocation key = entry.getKey();
			if (ForgeRegistries.BLOCKS.containsKey(key))
			{
				// and use it to create the finalized element
				// block registry returns nonnull values
				Block block = ForgeRegistries.BLOCKS.getValue(entry.getKey());
				if (block == Blocks.AIR)
				{
					ExMachina.LOGGER.warn("While attempting to retrieve a block for the circuit element defined for {}, found minecraft:air. This is disallowed as it generally indicates a broken registry and registering circuit elements to air causes performance issues.", key);
				}
				else
				{
					newMap.put(block, new DefinedCircuitComponent(raw, block, circuitBehaviourRegistry));
				}
			}
			else
			{
				ExMachina.LOGGER.warn("Circuit element json present for unused block id: {} -- this circuit element will not be registered.", key.toString());
			}
		}
		
		this.data = newMap;
	}

	@Override
	public Map<Block, ? extends CircuitComponent> get()
	{
		return this.data;
	}
	

}
