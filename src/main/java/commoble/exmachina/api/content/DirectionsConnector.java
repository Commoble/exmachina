package commoble.exmachina.api.content;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.ExMachina;
import commoble.exmachina.Names;
import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ExMachinaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

/**
 * Connector type for a block that connects to a subset of its six neighbors.
<pre>
{
	"type": "exmachina:directions",
	"directions": ["north", "south", "east", "west"] // list of Directions
}
</pre>
 */
public record DirectionsConnector(EnumSet<Direction> directions) implements Connector
{
	public static final ResourceKey<Codec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.getModRL(Names.DIRECTIONS));
	public static final Codec<DirectionsConnector> CODEC = Direction.CODEC.listOf()
		.fieldOf("directions")
		.xmap(list -> new DirectionsConnector(EnumSet.copyOf(list)), conn -> List.copyOf(conn.directions()))
		.codec();

	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		return DataResult.success(state -> this::connect);
	}
	
	public Set<BlockPos> connect(LevelReader level, BlockPos pos)
	{
		Set<BlockPos> set = new HashSet<>();
		for (Direction dir : this.directions())
		{
			set.add(pos.relative(dir));
		}
		return set;
	}

	@Override
	public Codec<? extends Connector> codec()
	{
		return CODEC;
	}

}
