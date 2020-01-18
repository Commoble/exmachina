package com.github.commoble.exmachina;

import net.minecraft.util.ResourceLocation;

public class ResourceLocations
{
	public static final ResourceLocation BATTERY = getModRL(RegistryNames.BATTERY);
	public static final ResourceLocation WIRE = getModRL(RegistryNames.WIRE);
	public static final ResourceLocation LIGHTBULB = getModRL(RegistryNames.LIGHTBULB);
		
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(ExMachinaMod.MODID, name);
	}
}
