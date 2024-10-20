package net.commoble.exmachina.internal;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * /config/exmachina-common.toml
 * @param maxPowerGraphSize maximum size of power graphs in blocks (default 10000)
 * @param maxSignalGraphSize maximum size of signal graphs in blocks (default 1024)
 */
public record CommonConfig(IntValue maxPowerGraphSize, IntValue maxSignalGraphSize)
{	
	/**
	 * {@return common config}
	 * @param builder config builder
	 */
	public static CommonConfig create(ModConfigSpec.Builder builder)
	{
		IntValue maxPowerGraphSize = builder
			.comment("Maximum size of power graph in blocks")
			.defineInRange("max_power_graph_size", 10000, 1, Integer.MAX_VALUE);
		IntValue maxSignalGraphSize = builder
			.comment("Maximum size (in nodes) of signal graphs (used by redstone wires, bundled cables, etc),",
				"where a node is a color channel on a given face (e.g. [0,0,0] + north + orange).",
				"Single-channel blocks will consume one node per face.",
				"Multi-channel blocks such as bundled cables may use one node for each channel needed.")
			.translation("morered.config.max_signal_graph_size")
			.defineInRange("max_signal_graph_size", 1024, 1, Integer.MAX_VALUE);
		
		return new CommonConfig(maxPowerGraphSize, maxSignalGraphSize);
	}
}
