package net.commoble.exmachina.api;

import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * DataMapTypes for Ex Machina's data maps
 */
public final class ExMachinaDataMaps
{
	private ExMachinaDataMaps() {}
	/**
	 * DataMapType for the exmachina:signal_transmitter block data map used by the signal graph
	 */
	public static final DataMapType<Block, SignalComponent> SIGNAL_TRANSMITTER = DataMapType.builder(ExMachina.id("signal_transmitter"), Registries.BLOCK, SignalComponent.CODEC).build();

}
