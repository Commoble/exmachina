package com.github.commoble.exmachina.api;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
	
	public void registerStaticCircuitElementProperty(ResourceLocation identifier, BiFunction<Block, Map<String, Double>, ToDoubleFunction<BlockState>> propertyBuilder);
	
	public void registerDynamicCircuitElementProperty(ResourceLocation identifier, BiFunction<Block, Map<String, Double>, DynamicCircuitElementProperty> propertyBuilder);
}
