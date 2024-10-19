package net.commoble.exmachina.api.content;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.DynamicProperty;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.Names;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * DynamicProperty representing an absent DynamicProperty.
 * This is the default dynamic property for CircuitComponents that do not specify them.
 * Blocks that use this property will not be dynamically tracked.
 * 
 * We use this instead of empty optionals or nulls, because optionals are clunkier
 * and codecs don't like nulls.
 */
public enum NoneDynamicProperty implements DynamicProperty
{
	INSTANCE;
	
	public static final ResourceKey<MapCodec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.id(Names.NONE));
	public static final MapCodec<NoneDynamicProperty> CODEC = MapCodec.unit(INSTANCE); 

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public double getValue(LevelReader level, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public MapCodec<? extends DynamicProperty> codec()
	{
		return CODEC;
	}
}
