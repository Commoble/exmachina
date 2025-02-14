package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.SignalGraphKey;
import net.commoble.exmachina.api.SignalComponent;
import net.commoble.exmachina.api.TransmissionNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SignalComponent for a Block that can connect to the signal graph on all sides, providing their vanilla redstone signal output to the graph.
<pre>
{
	"type": "exmachina:cube",
	"receives_power": false // optional boolean; if true, will recheck neighbor power after a graph update
}
</pre>
 * @param receivesPower boolean which indicates neighbor updates should be invoked on this block after a graph update
 */
public record CubeSignalComponent(boolean receivesPower) implements SignalComponent
{	
	/** exmachina:signal_transmitter_type / exmachina:cube */
	public static final ResourceKey<MapCodec<? extends SignalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, ExMachina.id("cube"));
	
	/**
	<pre>
	{
		"type": "exmachina:cube",
		"receives_power": false // optional boolean; if true, will recheck neighbor power after a graph update
	}
	</pre>
	*/
	public static final MapCodec<CubeSignalComponent> CODEC =
		RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.BOOL.optionalFieldOf("receives_power", false).forGetter(CubeSignalComponent::receivesPower)
		).apply(builder, CubeSignalComponent::new));

	@Override
	public MapCodec<? extends SignalComponent> codec()
	{
		return CODEC;
	}

	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		List<TransmissionNode> nodes = new ArrayList<>();
		if (channel != Channel.redstone())
			return nodes;
		
		for (Direction faceSide : Direction.values())
		{
			for (Direction directionToNeighbor : Direction.values())
			{
				if (directionToNeighbor == faceSide || directionToNeighbor == faceSide.getOpposite())
					continue;
				Direction directionFromNeighbor = directionToNeighbor.getOpposite();
				BlockPos neighborPos = pos.relative(directionToNeighbor);
				nodes.add(new TransmissionNode(
					NodeShape.ofSideSide(faceSide, directionToNeighbor),
					reader -> reader.getSignal(pos, directionFromNeighbor),
					Set.of(),
					Set.of(new SignalGraphKey(levelKey, neighborPos, NodeShape.ofSideSide(faceSide, directionFromNeighbor), Channel.redstone())),
					(levelAccess, power) -> Map.of()
				));
			}
		}
		
		return nodes;
	}

	@Override
	public boolean updateSelfFromNeighborsAfterGraphUpdate(LevelReader level, BlockState state, BlockPos pos)
	{
		return this.receivesPower;
	}
	
	
}
