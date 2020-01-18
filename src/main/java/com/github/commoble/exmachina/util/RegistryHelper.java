package com.github.commoble.exmachina.util;

import com.github.commoble.exmachina.ExMachinaMod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryHelper
{
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, String registryKey, T entry)
	{
		return register(registry, ExMachinaMod.MODID, registryKey, entry);
	}

	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, String modID, String registryKey, T entry)
	{
		return register(registry, new ResourceLocation(modID, registryKey), entry);
	}

	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, ResourceLocation registryKey, T entry)
	{
		entry.setRegistryName(registryKey);
		registry.register(entry);
		return entry;
	}
}
