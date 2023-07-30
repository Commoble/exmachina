package commoble.exmachina.api;

import com.mojang.serialization.Codec;

import commoble.exmachina.ExMachina;
import commoble.exmachina.Names;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registry ResourceKeys for Ex Machina's static registries
 */
public class ExMachinaRegistries
{
	// static registries
	public static final ResourceKey<Registry<Codec<? extends Connector>>> CONNECTOR_TYPE = ResourceKey.createRegistryKey(ExMachina.getModRL(Names.CONNECTOR_TYPE));
	public static final ResourceKey<Registry<Codec<? extends StaticProperty>>> STATIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.getModRL(Names.STATIC_PROPERTY_TYPE));
	public static final ResourceKey<Registry<Codec<? extends DynamicProperty>>> DYNAMIC_PROPERTY_TYPE = ResourceKey.createRegistryKey(ExMachina.getModRL(Names.DYNAMIC_PROPERTY_TYPE));
	
	// dynamic registries
	public static final ResourceKey<Registry<CircuitComponent>> CIRCUIT_COMPONENT = ResourceKey.createRegistryKey(ExMachina.getModRL(Names.CIRCUIT_COMPONENT));
}
