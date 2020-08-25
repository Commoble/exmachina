package com.github.commoble.exmachina.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import com.github.commoble.exmachina.ExMachina;
import com.github.commoble.exmachina.api.CircuitComponent;
import com.github.commoble.exmachina.api.DynamicPropertyFactory;
import com.github.commoble.exmachina.api.JsonObjectReader;
import com.github.commoble.exmachina.api.PluginRegistrator;
import com.github.commoble.exmachina.api.StaticPropertyFactory;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CircuitBehaviourRegistry implements PluginRegistrator
{
	public final Map<ResourceLocation, BiFunction<IWorld, BlockPos, Set<BlockPos>>> connectionTypes = new HashMap<>();
	public final Map<ResourceLocation, JsonObjectReader<StaticPropertyFactory>> staticProperties = new HashMap<>();
	public final Map<ResourceLocation, JsonObjectReader<DynamicPropertyFactory>> dynamicProperties = new HashMap<>();
	
	public CircuitBehaviourRegistry()
	{
	}

	@Override
	public void registerConnectionType(ResourceLocation identifier, BiFunction<IWorld, BlockPos, Set<BlockPos>> connectionType)
	{
		Object existing = this.connectionTypes.put(identifier, connectionType);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN, "A connection type was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.", identifier.toString());
		}
	}

	@Override
	public void registerStaticCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<StaticPropertyFactory> propertyReader)
	{
		Object existing = this.staticProperties.put(identifier, propertyReader);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN, "A static circuit element property was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.", identifier.toString());
		}
	}

	@Override
	public void registerDynamicCircuitElementProperty(ResourceLocation identifier, JsonObjectReader<DynamicPropertyFactory> propertyReader)
	{
		Object existing = this.dynamicProperties.put(identifier, propertyReader);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN,
				"A dynamic circuit element property was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.",
				identifier.toString());
		}
	}

	@Override
	public Supplier<Map<Block, ? extends CircuitComponent>> getComponentDataGetter()
	{
		return ExMachina.INSTANCE.circuitElementDataManager;
	}
	
	
}
