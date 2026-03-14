package net.commoble.exmachina.api;

import org.jspecify.annotations.Nullable;

import net.commoble.exmachina.api.content.DefaultSignalComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignalComponent bound with its providing BlockState at some position
 * @param state BlockState associated with the signal components
 * @param component SignalComponent of the blockstate
 */
public record StateWirer(BlockState state, SignalComponent component)
{
	/**
	 * {@return StateWirer using default wiring components if the given block has none assigned}
	 * @param blockGetter BlockGetter where a signal graph is
	 * @param pos BlockPos to get the wiring components from
	 */
	public static StateWirer getOrDefault(BlockGetter blockGetter, BlockPos pos)
	{
		BlockState state = blockGetter.getBlockState(pos);
		@Nullable ResourceKey<Block> key = state.typeHolder().getKey();
		if (key == null)
			return new StateWirer(state, DefaultSignalComponent.INSTANCE);
		@Nullable SignalComponent transmitter = BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_COMPONENT, key);
		return new StateWirer(state, transmitter == null ? DefaultSignalComponent.INSTANCE : transmitter);
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
