package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.ExMachinaMod;

import net.minecraft.util.ResourceLocation;

public class ResourceLocations
{
	public static final ResourceLocation BATTERY = getModRL(RegistryNames.BATTERY);
	public static final ResourceLocation LIGHTBULB = getModRL(RegistryNames.LIGHTBULB);
	public static final ResourceLocation MONDOMETER = getModRL(RegistryNames.MONDOMETER);
	public static final ResourceLocation WIRE_SPOOL = getModRL(RegistryNames.WIRE_SPOOL);
	public static final ResourceLocation WIRE_PLINTH = getModRL(RegistryNames.WIRE_PLINTH);
		
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(ExMachinaMod.MODID, name);
	}
}
