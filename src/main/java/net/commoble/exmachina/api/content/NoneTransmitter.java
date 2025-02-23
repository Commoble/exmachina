package net.commoble.exmachina.api.content;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.SignalComponent;
import net.commoble.exmachina.api.TransmissionNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignalComponent which provides no power and connects to no graph.
 * 
<pre>
{
	"type": "exmachina:none"
}
</pre>
 */
public enum NoneTransmitter implements SignalComponent
{
	/** Singleton instance of NoneSource */
	INSTANCE;
	
	/** exmachina:signal_component_type / exmachina:none */
	public static final ResourceKey<MapCodec<? extends SignalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, ExMachina.id("none"));
	/** <pre>{"type": "exmachina:none"}</pre> */
	public static final MapCodec<NoneTransmitter> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public MapCodec<? extends SignalComponent> codec()
	{
		return CODEC;
	}

	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		return List.of();
	}	
}
