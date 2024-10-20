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
	 * DataMapType for the exmachina:signal_source block data map used by the signal graph
	 */
	public static final DataMapType<Block, SignalSource> SIGNAL_SOURCE = DataMapType.builder(ExMachina.id("signal_source"), Registries.BLOCK, SignalSource.CODEC).build();
	
	/**
	 * DataMapType for the exmachina:signal_transmitter block data map used by the signal graph
	 */
	public static final DataMapType<Block, SignalTransmitter> SIGNAL_TRANSMITTER = DataMapType.builder(ExMachina.id("signal_transmitter"), Registries.BLOCK, SignalTransmitter.CODEC).build();
	
	/**
	 * DataMapType for the exmachina:signal_receiver block data map used by the signal graph
	 */
	public static final DataMapType<Block, SignalReceiver> SIGNAL_RECEIVER = DataMapType.builder(ExMachina.id("signal_receiver"), Registries.BLOCK, SignalReceiver.CODEC).build();

}
