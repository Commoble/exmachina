package com.github.commoble.exmachina.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.exmachina.CircuitBehaviourRegistry;
import com.github.commoble.exmachina.api.CircuitComponent;
import com.github.commoble.exmachina.api.DynamicCircuitElementProperty;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class DefinedCircuitComponent implements CircuitComponent
{
	public static final ToDoubleFunction<BlockState> STATIC_NOOP = state -> 0D;
	
	public final double constantLoad;
	public final double constantSource;
	public final @Nonnull BiFunction<IWorld, BlockPos, Set<BlockPos>> connector;
	public final @Nonnull ToDoubleFunction<BlockState> staticSource;
	public final @Nonnull Optional<DynamicCircuitElementProperty> dynamicSource;
	public final @Nonnull ToDoubleFunction<BlockState> staticLoad;
	public final @Nonnull Optional<DynamicCircuitElementProperty> dynamicLoad;
	
	public DefinedCircuitComponent(@Nonnull RawCircuitElement raw, @Nonnull Block block, @Nonnull CircuitBehaviourRegistry registry)
	{
		this.connector = registry.connectionTypes.getOrDefault(new ResourceLocation(raw.connector), (world,pos) -> ImmutableSet.of());
		this.constantLoad = raw.constant_load;
		this.constantSource = raw.constant_source;
		this.staticSource = getProperty(raw.static_source, block, registry.staticProperties).orElse(STATIC_NOOP);
		this.dynamicSource = getProperty(raw.dynamic_source, block, registry.dynamicProperties);
		this.staticLoad = getProperty(raw.static_load, block, registry.staticProperties).orElse(STATIC_NOOP);
		this.dynamicLoad = getProperty(raw.dynamic_load, block, registry.dynamicProperties);
	}
	
	private static <T> Optional<T> getProperty(@Nullable RawCircuitProperty property, @Nonnull Block block, @Nonnull Map<ResourceLocation, BiFunction<Block, Map<String, Double>, T>> propertyMap)
	{
		if (property == null || property.type == null)
		{
			return Optional.empty();
		}
		else
		{
			BiFunction<Block, Map<String, Double>, T> function = propertyMap.get(new ResourceLocation(property.type));
			return function == null ? Optional.empty() : Optional.ofNullable(function.apply(block, property.data));
		}
	}
	
	@Override
	public double getLoad(IWorld world, BlockState state, BlockPos pos)
	{
		return this.constantLoad + this.staticLoad.applyAsDouble(state) + this.dynamicLoad.map(f -> f.getValue(world, pos, state)).orElse(0D);
	}
	
	@Override
	public double getSource(IWorld world, BlockState state, BlockPos pos)
	{
		return this.constantSource + this.staticSource.applyAsDouble(state) + this.dynamicSource.map(f -> f.getValue(world, pos, state)).orElse(0D);
	}
	
	@Override
	@Nonnull
	public BiFunction<IWorld, BlockPos, Set<BlockPos>> getConnector()
	{
		return this.connector;
	}
}
