package net.commoble.exmachina.api.content;
import java.util.Map;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
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
 * SignalSource which can connect to graph nodes if the connecting graph node is adjacent to the source block and on the floor (face direction == DOWN)
 * 
<pre>
{
	"type": "exmachina:floor",
	"offset": -1 // optional int, values is added to vanilla redstone signal level
}
</pre>
 * @param offset optional int added to vanilla redstone signal level
 */
public record FloorSource(int offset) implements SignalSource
{
	/** exmachina:signal_source_type / exmachina:floor */
	public static final ResourceKey<MapCodec<? extends SignalSource>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_SOURCE_TYPE, ExMachina.id("floor"));
	
	/**
<pre>
{
	"type": "exmachina:floor",
	"offset": -1 // optional int, values is added to vanilla redstone signal level
}
</pre>
	*/
	public static final MapCodec<FloorSource> CODEC = Codec.INT.optionalFieldOf("offset", 0).xmap(FloorSource::new, FloorSource::offset);

	@Override
	public MapCodec<? extends SignalSource> codec()
	{
		return CODEC;
	}

	@Override
	public Map<Channel, ToIntFunction<LevelReader>> getSupplierEndpoints(BlockGetter level, BlockPos supplierPos, BlockState supplierState, Direction supplierSide, Face connectedFace)
	{
		if (connectedFace.attachmentSide() != Direction.DOWN)
			return Map.of();

		BlockPos offsetFromNeighbor = supplierPos.subtract(connectedFace.pos());
		@Nullable Direction directionFromNeighbor = Direction.getNearest(offsetFromNeighbor, null); 
		return directionFromNeighbor == null
			? Map.of()
			: Map.of(Channel.redstone(), reader -> reader.getSignal(supplierPos, directionFromNeighbor) + this.offset);
	}
}
