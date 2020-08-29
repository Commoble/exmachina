package commoble.exmachina.api;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface PluginRegistrator
{
	/**
	 * Registers a connection type that can be referred to in data packs.
	 * This determines what block-positions a given block's circuit element can connect to in a circuit.
	 * Two blocks can be connected in a circuit if they belong to each other's respective position-sets determined by this.
	 * @param identifier A registry identifier for the connection type
	 * @param connectionType A function that determines which positions a circuit element is allowed to connect to.
	 */
	public void registerConnectionType(ResourceLocation identifier, BiFunction<IWorld, BlockPos, Set<BlockPos>> connectionType);
	
	/**
	 * Registers a static property that can be used for electrical component blocks' source and load properties in data jsons.
	 * Static properties are evaluated less often than dynamic properties, but can only vary by blockstate.
	 * @param identifier An ID for this property. Data jsons can refer to this property when defining components.
	 * @param propertyReader A function that will be used when deserializing data jsons into components.
	 */
	public void registerStaticCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<StaticPropertyFactory> propertyReader);
	
	/**
	 * Registers a dynamic proprety that can be used for electrical component blocks' source and load properties in data jsons.
	 * Dynamic properties can use world and positional context, but are evaluated more often than static properties.
	 * However, they can be reevaluated without rebuilding the entire circuit object.
	 * They are ideal for block entities whose properties update too frequently to be blockstate-based.
	 * Static properties are preferable for properties that vary infrequently, if ever.
	 * @param identifier
	 * @param propertyReader
	 */
	public void registerDynamicCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<DynamicPropertyFactory> propertyReader);
	
	/**
	 * Returns a supplier for the circuit component data loaded from jsons.
	 * The supplier is safe to cache; the map itself is not, as the map instance changes when datapacks are reloaded. 
	 * Keep in mind that querying map.get(block) on the given map returns null for blocks that do not have associated component data
	 * @return
	 */
	public Supplier<Map<Block, ? extends CircuitComponent>> getComponentDataGetter();
}
