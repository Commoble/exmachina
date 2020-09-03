package commoble.exmachina.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import commoble.exmachina.ExMachina;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.DynamicPropertyFactory;
import commoble.exmachina.api.JsonObjectReader;
import commoble.exmachina.api.PluginRegistrator;
import commoble.exmachina.api.StaticPropertyFactory;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class CircuitBehaviourRegistry implements PluginRegistrator
{
	public final Map<ResourceLocation, JsonObjectReader<ConnectorFactory>> connectionTypes = new HashMap<>();
	public final Map<ResourceLocation, JsonObjectReader<StaticPropertyFactory>> staticProperties = new HashMap<>();
	public final Map<ResourceLocation, JsonObjectReader<DynamicPropertyFactory>> dynamicProperties = new HashMap<>();
	
	public CircuitBehaviourRegistry()
	{
	}
	
	@Override
	public void registerConnectionType(ResourceLocation identifier, JsonObjectReader<ConnectorFactory> connectionType)
	{
		this.registerToNewIdentifierOrLogWarning(identifier, connectionType, this.connectionTypes, "connection type");
	}

	@Override
	public void registerStaticCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<StaticPropertyFactory> propertyReader)
	{
		this.registerToNewIdentifierOrLogWarning(identifier, propertyReader, this.staticProperties, "static circuit element property");
	}

	@Override
	public void registerDynamicCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<DynamicPropertyFactory> propertyReader)
	{
		this.registerToNewIdentifierOrLogWarning(identifier, propertyReader, this.dynamicProperties, "dynamic circuit element property");
	}

	@Override
	public Supplier<Map<Block, ? extends CircuitComponent>> getComponentDataGetter()
	{
		return ExMachina.INSTANCE.circuitElementDataManager;
	}
	
	private <VALUE> void registerToNewIdentifierOrLogWarning(ResourceLocation identifier, VALUE value, Map<ResourceLocation, VALUE> map, String valueTypeName)
	{
		Object existing = map.put(identifier, value);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN,
				"A {} was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.",
				valueTypeName,
				identifier.toString());
		}
	}
}
