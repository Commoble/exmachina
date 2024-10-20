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
	 * Registry key of the static Registry for SignalSource MapCodecs. SignalSources are signal graph components assigned to blocks via datamap which can provide power to the signal graph when adjacent to it.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends SignalSource>>> SIGNAL_SOURCE_TYPE = ResourceKey.createRegistryKey(ExMachina.id("signal_source_type"));
	
	/**
	 * Registry key of the static Registry for SignalTransmitter MapCodecs. SignalTransmitters are signal graph components assigned to blocks via datamap which form the unique internal nodes of the graph.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends SignalTransmitter>>> SIGNAL_TRANSMITTER_TYPE = ResourceKey.createRegistryKey(ExMachina.id("signal_transmitter_type"));
	
	/**
	 * Registry key of the static Registry for SignalReceiver MapCodecs. SignalReceivers are signal graph components assigned to blocks via datamap which can listen to graph updates in adjacent graphs.
	 */
	public static final ResourceKey<Registry<MapCodec<? extends SignalReceiver>>> SIGNAL_RECEIVER_TYPE = ResourceKey.createRegistryKey(ExMachina.id("signal_receiver_type"));
	
	// dynamic registries
	/**
	 * Registry key of the datapack Registry for CircuitComponents. CircuitComponents form nodes of the power graph and are assigned to blocks which share an id with them.
	 */
	public static final ResourceKey<Registry<CircuitComponent>> CIRCUIT_COMPONENT = ResourceKey.createRegistryKey(ExMachina.id("circuit_component"));
}
