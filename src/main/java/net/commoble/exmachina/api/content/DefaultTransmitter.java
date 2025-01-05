package net.commoble.exmachina.api.content;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.SignalTransmitter;
import net.commoble.exmachina.api.TransmissionNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Default SignalTransmitter used for blocks that have none assigned to them. No-ops.
 * 
<pre>
{
	"type": "exmachina:default"
}
</pre>
 */
public enum DefaultTransmitter implements SignalTransmitter
{
	/** Singleton instance of the DefaultTransmitter */
	INSTANCE;

	/** exmachina/signal_transmitter_type / exmachina:default */ 
	public static final ResourceKey<MapCodec<? extends SignalTransmitter>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_TRANSMITTER_TYPE, ExMachina.id("default"));
	
	/** <pre>{"type": "exmachina:default"}</pre> */
	public static final MapCodec<DefaultTransmitter> CODEC = MapCodec.unit(INSTANCE);
	
	@Override
	public MapCodec<? extends SignalTransmitter> codec()
	{
		return CODEC;
	}
	
	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		return List.of();
	}
}
