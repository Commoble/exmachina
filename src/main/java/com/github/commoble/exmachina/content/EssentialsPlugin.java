package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.api.AutoPlugin;
import com.github.commoble.exmachina.api.Plugin;
import com.github.commoble.exmachina.api.PluginRegistrator;
import com.github.commoble.exmachina.content.wire_post.WirePostBlock;

import net.minecraft.util.ResourceLocation;

@AutoPlugin
public class EssentialsPlugin implements Plugin
{
	@Override
	public void register(PluginRegistrator registry)
	{
		registry.registerConnectionType(new ResourceLocation("exmachina:wire_post"), WirePostBlock::getPotentialConnections);
		
	}

}
