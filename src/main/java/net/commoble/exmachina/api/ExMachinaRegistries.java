package net.commoble.exmachina.api;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.Names;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registry ResourceKeys for Ex Machina's static registries
 */
public class ExMachinaRegistries
{
	// static registries
	public static final ResourceKey<Registry<MapCodec<? extends Connector>>> CONNECTOR_TYPE = ResourceKey.createRegistryKey(ExMachina.id(Names.CONNECTOR_TYPE));
	public static final ResourceKey<Registry<MapCodec<? extends StaticProperty>>> STATIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.id(Names.STATIC_PROPERTY_TYPE));
	public static final ResourceKey<Registry<MapCodec<? extends DynamicProperty>>> DYNAMIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.id(Names.DYNAMIC_PROPERTY_TYPE));
	
	// dynamic registries
	public static final ResourceKey<Registry<CircuitComponent>> CIRCUIT_COMPONENT = ResourceKey.createRegistryKey(ExMachina.id(Names.CIRCUIT_COMPONENT));
}
