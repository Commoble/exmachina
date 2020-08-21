package com.github.commoble.exmachina;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

import org.apache.logging.log4j.Level;

import com.github.commoble.exmachina.api.DynamicCircuitElementProperty;
import com.github.commoble.exmachina.api.PluginRegistrator;
import com.github.commoble.exmachina.api.PowerConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CircuitBehaviourRegistry implements PluginRegistrator
{
	public final Map<ResourceLocation, BiFunction<IWorld, BlockPos, Set<BlockPos>>> connectionTypes = new HashMap<>();
	public final Map<ResourceLocation, BiFunction<Block, Map<String, Double>, ToDoubleFunction<BlockState>>> staticProperties = new HashMap<>();
	public final Map<ResourceLocation, BiFunction<Block, Map<String, Double>, DynamicCircuitElementProperty>> dynamicProperties = new HashMap<>();
	public final Map<ResourceLocation, PowerConsumer> consumptionTypes = new HashMap<>();
	
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
	public void registerStaticCircuitElementProperty(ResourceLocation identifier, BiFunction<Block, Map<String, Double>, ToDoubleFunction<BlockState>> propertyBuilder)
	{
		Object existing = this.staticProperties.put(identifier, propertyBuilder);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN, "A static circuit element property was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.", identifier.toString());
		}
	}

	@Override
	public void registerDynamicCircuitElementProperty(ResourceLocation identifier, BiFunction<Block, Map<String, Double>, DynamicCircuitElementProperty> propertyBuilder)
	{
		Object existing = this.dynamicProperties.put(identifier, propertyBuilder);
		if (existing != null)
		{
			ExMachina.LOGGER.log(Level.WARN,
				"A dynamic circuit element property was registered to the identifier {} more than once, overwriting an existing value. This is not supported behaviour and may cause unusual phenomena.",
				identifier.toString());
		}
	}
	
	
}
