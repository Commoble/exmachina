package net.commoble.exmachina.api;

import java.util.Objects;

import net.commoble.exmachina.api.content.DefaultReceiver;
import net.commoble.exmachina.api.content.DefaultSource;
import net.commoble.exmachina.api.content.DefaultTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Signal Source+Transmitter+Receiver bound together from a given position
 * @param state BlockState associated with the signal components
 * @param source SignalSource of the blockstate
 * @param transmitter SignalTransmitter of the blockstate
 * @param receiver SignalReceiver of the blockstate
 */
public record StateWirer(BlockState state, SignalSource source, SignalTransmitter transmitter, SignalReceiver receiver)
{
	/**
	 * {@return StateWirer using default wiring components if the given block has none assigned}
	 * @param blockGetter BlockGetter where a signal graph is
	 * @param pos BlockPos to get the wiring components from
	 */
	@SuppressWarnings("deprecation")
	public static StateWirer getOrDefault(BlockGetter blockGetter, BlockPos pos)
	{
		BlockState state = blockGetter.getBlockState(pos);
		SignalSource source = Objects.requireNonNullElse(
			BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_SOURCE, state.getBlock().builtInRegistryHolder().getKey()),
			DefaultSource.INSTANCE);
		SignalTransmitter transmitter = Objects.requireNonNullElse(
			BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_TRANSMITTER, state.getBlock().builtInRegistryHolder().getKey()),
			DefaultTransmitter.INSTANCE);
		SignalReceiver receiver = Objects.requireNonNullElse(
			BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_RECEIVER, state.getBlock().builtInRegistryHolder().getKey()),
			DefaultReceiver.INSTANCE);
		return new StateWirer(state, source, transmitter, receiver);
	}
	
	/**
	 * {@return true if a signal graph should ignore vanilla redstone signal emitted by this block}
	 * @param levelReader LevelReader to read signal from
	 */
	public boolean ignoreVanillaSignal(LevelReader levelReader)
	{
		return this.state.is(ExMachinaTags.Blocks.IGNORE_VANILLA_SIGNAL);
	}
}
