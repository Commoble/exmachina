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
 * SignalComponent which can connect to graph nodes if the connecting graph node is adjacent to the source block and on the floor (face direction == DOWN)
 * 
<pre>
{
	"type": "exmachina:floor",
	"offset": -1, // optional int, values is added to vanilla redstone signal level
	"receives_power": false // optional boolean; if true, will recheck neighbor power after a graph update
}
</pre>
 * @param offset optional int added to vanilla redstone signal level
 * @param receivesPower boolean which indicates neighbor updates should be invoked on this block after a graph update
 */
public record FloorSignalComponent(int offset, boolean receivesPower) implements SignalComponent
{
	/** exmachina:signal_source_type / exmachina:floor */
	public static final ResourceKey<MapCodec<? extends SignalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, ExMachina.id("floor"));
	
	/**
<pre>
{
	"type": "exmachina:floor",
	"offset": -1, // optional int, values is added to vanilla redstone signal level
	"receives_power": false // optional boolean; if true, will recheck neighbor power after a graph update
}
</pre>
	*/
	public static final MapCodec<FloorSignalComponent> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.INT.optionalFieldOf("offset", 0).forGetter(FloorSignalComponent::offset),
			Codec.BOOL.optionalFieldOf("receives_power", false).forGetter(FloorSignalComponent::receivesPower)
		).apply(builder, FloorSignalComponent::new));

	@Override
	public MapCodec<? extends SignalComponent> codec()
	{
		return CODEC;
	}
	
	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		if (channel != Channel.redstone())
		{
			return List.of();
		}
		
		List<TransmissionNode> nodes = new ArrayList<>();
		for (Direction directionToNeighbor : Direction.values())
		{
			if (directionToNeighbor.getAxis().isHorizontal()) {
				Direction directionFromNeighbor = directionToNeighbor.getOpposite();
				nodes.add(new TransmissionNode(
					NodeShape.ofSideSide(Direction.DOWN, directionToNeighbor),
					levelReader -> levelReader.getSignal(pos, directionFromNeighbor) + this.offset,
					Set.of(),
					Set.of(new SignalGraphKey(
						levelKey,
						pos.relative(directionToNeighbor),
						NodeShape.ofSideSide(Direction.DOWN, directionToNeighbor.getOpposite()),
						Channel.redstone()
					)),
					(levelAccess, newPower) -> Map.of()
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
