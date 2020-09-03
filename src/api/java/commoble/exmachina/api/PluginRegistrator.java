package commoble.exmachina.api;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public interface PluginRegistrator
{
	
	/**
	 * Registers a connection type that can be referred to in data packs.
	 * Registering a connection type in this manner allows extra parameters to be specified in jsons.
	 * 
	 * A circuit element json can specify one of these connection types with an object field, e.g.
	 * "connector":
	 * {
	 * 	"type": "exmachina:directions_except",
	 * 	"values": ["up"]
	 * }
	 * 
	 * A user can optionally refer to a connection type as a simple string field in their json:
	 * 
	 * "connector": "exmachina:all_directions"
	 * 
	 * In this case, a JsonObject object with just the type field will be generated and used as the argument to
	 * the registered ConnectorFactory, for consistency.
	 * 
	 * 
	 * @param identifier An ID for this connection type. Data jsons can refer to this property when defining circuit elements.
	 * @param connectionType The connector factory deserializer being registered
	 */
	public void registerConnectionType(ResourceLocation identifier, JsonObjectReader<ConnectorFactory> connectionType);
	
	/**
	 * Registers a static property that can be used for electrical component blocks' source and load properties in data jsons.
	 * Static properties are evaluated less often than dynamic properties, but can only vary by blockstate.
	 * @param identifier An ID for this property. Data jsons can refer to this property when defining circuit elements.
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
