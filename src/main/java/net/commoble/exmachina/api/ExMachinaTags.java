package net.commoble.exmachina.api;

import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Tags under the exmachina namespace
 */
public final class ExMachinaTags
{
	private ExMachinaTags() {}
	
	/** Block Tags under the exmachina namespace */
	public static final class Blocks
	{
		private Blocks() {}
		
		private static TagKey<Block> tag(String name) { return TagKey.create(Registries.BLOCK, ExMachina.id(name)); }
		
		/**
		 * If a block is tagged with the exmachina:ignore_vanilla_signal block tag,
		 * then when the signal grapher is determining whether a block outputs signal to the graph,
		 * it will NOT read signal from that block.
		 */
		public static final TagKey<Block> IGNORE_VANILLA_SIGNAL = tag("ignore_vanilla_signal");		
	}
}
