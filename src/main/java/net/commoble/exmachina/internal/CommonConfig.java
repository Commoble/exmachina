package net.commoble.exmachina.internal;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record CommonConfig(IntValue maxCircuitSize)
{	
	public static CommonConfig create(ModConfigSpec.Builder builder)
	{
		IntValue maxCircuitSize = builder
			.comment("Maximum size of circuits in blocks")
			.defineInRange("maxCircuitSize", 10000, 1, Integer.MAX_VALUE);
		
		return new CommonConfig(maxCircuitSize);
	}
}
