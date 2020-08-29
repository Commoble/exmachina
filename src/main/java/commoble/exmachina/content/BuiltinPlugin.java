package com.github.commoble.exmachina.content;


import com.github.commoble.exmachina.api.AutoPlugin;
import com.github.commoble.exmachina.api.Plugin;
import com.github.commoble.exmachina.api.PluginRegistrator;

import net.minecraft.util.ResourceLocation;

@AutoPlugin
public class BuiltinPlugin implements Plugin
{
	@Override
	public void register(PluginRegistrator registry)
	{
		registry.registerConnectionType(new ResourceLocation("exmachina:cube_all"), BuiltinFunctions::getCubeConnections);
		
		registry.registerStaticCircuitElementProperty(new ResourceLocation("exmachina:constant"), BuiltinFunctions::getConstantPropertyReader);
		registry.registerStaticCircuitElementProperty(new ResourceLocation("exmachina:blockstate"), BuiltinFunctions::getStateTablePropertyReader);
	}

}
