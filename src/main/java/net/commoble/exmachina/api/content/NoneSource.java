package net.commoble.exmachina.api.content;

import java.util.Map;
import java.util.function.ToIntFunction;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.Face;
import net.commoble.exmachina.api.SignalSource;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignalSource which provides no power and connects to no graph.
 * 
<pre>
{
	"type": "exmachina:none"
}
</pre>
 */
public enum NoneSource implements SignalSource
{
	/** Singleton instance of NoneSource */
	INSTANCE;
	
	/** exmachina:signal_source_type / exmachina:none */
	public static final ResourceKey<MapCodec<? extends SignalSource>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_SOURCE_TYPE, ExMachina.id("none"));
	/** <pre>{"type": "exmachina:none"}</pre> */
	public static final MapCodec<NoneSource> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public MapCodec<? extends SignalSource> codec()
	{
		return CODEC;
	}

	@Override
	public Map<Channel, ToIntFunction<LevelReader>> getSupplierEndpoints(BlockGetter level, BlockPos supplierPos, BlockState supplierState, Direction supplierSide,
		Face connectedFace)
	{
		return Map.of();
	}
}
