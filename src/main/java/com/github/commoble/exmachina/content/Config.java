package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.content.util.ConfigHelper;
import com.github.commoble.exmachina.content.util.ConfigHelper.ConfigValueListener;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config
{
	public ConfigValueListener<Double> max_plinth_connection_range;
	
	public Config(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("General Settings");
		this.max_plinth_connection_range = subscriber.subscribe(builder
			.comment("Maximum Plinth Connection Range")
			.translation("exmachina.config.max_plinth_connection_range")
			.defineInRange("max_plinth_connection_range", 32D, 0D, Double.MAX_VALUE));
	}
}
