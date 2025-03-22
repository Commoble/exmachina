package net.commoble.exmachina.api;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registry ResourceKeys for Ex Machina's static registries
 */
public final class ExMachinaRegistries
{
	private ExMachinaRegistries() {}
	
	// static registries
	/**
	 * Registry key of the static Registry for Connector MapCodecs. Connectors are used for defining how blocks can connect to each other in power graphs.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends Connector>>> CONNECTOR_TYPE = ResourceKey.createRegistryKey(ExMachina.id("connector_type"));
	
	/**
	 * Registry key of the static Registry for StaticProperty MapCodecs. Static Properties are used for assigning voltage/resistance to blockstates in the power graph.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends StaticProperty>>> STATIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.id("static_property_type"));
	
	/**
	 * Registry key of the static Registry for DynamicProperty MapCodecs. Dynamic Properties are used for providing dynamic voltage/resistance to the power graph (e.g. from blockentity data)
	 */
	public static final ResourceKey<Registry<MapCodec<? extends DynamicProperty>>> DYNAMIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.id("dynamic_property_type"));

	/**
	 * Registry key of the static Registry for SignalComponent MapCodecs. SignalComponent are signal graph components assigned to blocks via datamap which form the unique internal nodes of the graph.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends SignalComponent>>> SIGNAL_COMPONENT_TYPE = ResourceKey.createRegistryKey(ExMachina.id("signal_component_type"));
	
	/**
	 * Registry key of the static registry for MechanicalComponent MapCodecs. MechanicalComponents are mechanical graph components assigned to blocks via datamap which form the unique internal nodes of the graph.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends MechanicalComponent>>> MECHANICAL_COMPONENT_TYPE = ResourceKey.createRegistryKey(ExMachina.id("mechanical_component_type"));

	// dynamic registries
	/**
	 * Registry key of the datapack Registry for CircuitComponents. CircuitComponents form nodes of the power graph and are assigned to blocks which share an id with them.
	 */
	public static final ResourceKey<Registry<CircuitComponent>> CIRCUIT_COMPONENT = ResourceKey.createRegistryKey(ExMachina.id("circuit_component"));
	
	/**
	 * Registry key of the datapack Registry for MechanicalComponents. MechanicalComponents form nodes of the mechanical graph and are assigned to blocks which share an id with them.
	 */
	public static final ResourceKey<Registry<MechanicalComponent>> MECHANICAL_COMPONENT = ResourceKey.createRegistryKey(ExMachina.id("mechanical_component"));
}
