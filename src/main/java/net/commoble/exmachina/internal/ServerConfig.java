package net.commoble.exmachina.internal;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record ServerConfig(
	IntValue machineCycleTicks
	)
{
	public static ServerConfig create(ModConfigSpec.Builder builder)
	{
		return new ServerConfig(
			builder
				.comment("Some machine animations modulate the game ticks by this number (i.e. effectiveTicks = gameTime % n) to avoid floating-point rounding errors at high game times",
					"This value should be a value such that (value+1) / (40pi) is almost, but not quite, a whole number",
					"The default value of 6030 causes a render cycle every five minutes with <0.1% discontinuity after the final tick",
					"Higher values increase the time between discontinuities but also increase potential for rounding errors")
				.translation("exmachina.machineCycleTicks")
				.defineInRange("machineCycleTicks", 6030, 1, Integer.MAX_VALUE)
		);
	}
}
