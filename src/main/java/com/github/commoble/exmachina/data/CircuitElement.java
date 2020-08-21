package com.github.commoble.exmachina.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.exmachina.CircuitBehaviourRegistry;
import com.github.commoble.exmachina.api.DynamicCircuitElementProperty;
import com.github.commoble.exmachina.api.PowerConsumer;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class CircuitElement
{
	public final @Nonnull BiFunction<IWorld, BlockPos, Set<BlockPos>> connections;
	public final double wireResistance;
	public final @Nonnull Optional<ToDoubleFunction<BlockState>> staticProduction;
	public final @Nonnull Optional<DynamicCircuitElementProperty> dynamicProduction;
	public final @Nonnull Optional<ToDoubleFunction<BlockState>> staticLoad;
	public final @Nonnull Optional<DynamicCircuitElementProperty> dynamicLoad;
	public final @Nonnull Optional<PowerConsumer> consumer;
	
	public CircuitElement(@Nonnull RawCircuitElement raw, @Nonnull Block block, @Nonnull CircuitBehaviourRegistry registry)
	{
		this.connections = registry.connectionTypes.getOrDefault(new ResourceLocation(raw.connection), (world,pos) -> ImmutableSet.of());
		this.wireResistance = raw.wire_resistance;
		boolean hasProduction = raw.production != null && raw.production.type != null;
		this.staticProduction = hasProduction
			? getStaticProperty(new ResourceLocation(raw.production.type), raw.production.data, block, registry)
			: Optional.empty();
		this.dynamicProduction = hasProduction && !this.staticProduction.isPresent() // can have static or dynamic property, not both
			? getDynamicProperty(new ResourceLocation(raw.production.type), raw.production.data, block, registry)
			: Optional.empty();
		boolean hasLoad = raw.load != null && raw.load.type != null;
		this.staticLoad = hasLoad
			? getStaticProperty(new ResourceLocation(raw.load.type), raw.load.data, block, registry)
			: Optional.empty();
		this.dynamicLoad = hasLoad && !this.staticLoad.isPresent() // can have static or dynamic property, not both
			? getDynamicProperty(new ResourceLocation(raw.load.type), raw.load.data, block, registry)
			: Optional.empty();
		this.consumer = raw.consumption != null
			? Optional.ofNullable(registry.consumptionTypes.get(new ResourceLocation(raw.consumption)))
			: Optional.empty();
	}
	
	public static Optional<ToDoubleFunction<BlockState>> getStaticProperty(@Nonnull ResourceLocation identifier, @Nullable Map<String, Double> data, @Nonnull Block block, @Nonnull CircuitBehaviourRegistry registry)
	{
		BiFunction<Block, Map<String, Double>, ToDoubleFunction<BlockState>> function = registry.staticProperties.get(identifier);
		return function == null ? Optional.empty() : Optional.ofNullable(function.apply(block, data));
	}
	
	public static Optional<DynamicCircuitElementProperty> getDynamicProperty(@Nonnull ResourceLocation identifier, @Nullable Map<String, Double> data, @Nonnull Block block, @Nonnull CircuitBehaviourRegistry registry)
	{
		BiFunction<Block, Map<String, Double>, DynamicCircuitElementProperty> function = registry.dynamicProperties.get(identifier);
		return function == null ? Optional.empty() : Optional.ofNullable(function.apply(block, data));
	}
	

}
