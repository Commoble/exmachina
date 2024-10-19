package net.commoble.exmachina.api.content;

import java.util.Set;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.Names;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connector for blocks that always connect in all six adjacent directions.
<pre>
{
	"type": "exmachina:all_directions"
}
</pre>
 */
public enum AllDirectionsConnector implements Connector
{
	INSTANCE;
	
	public static final ResourceKey<MapCodec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.id(Names.ALL_DIRECTIONS));
	public static final MapCodec<AllDirectionsConnector> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		return DataResult.success(AllDirectionsConnector::forState);
	}
	
	public static StateConnector forState(BlockState state)
	{
		return AllDirectionsConnector::connections;
	}
	
	public static Set<BlockPos> connections(LevelReader level, BlockPos pos)
	{
		return Set.of(
			pos.below(),
			pos.above(),
			pos.north(),
			pos.south(),
			pos.west(),
			pos.east()
			); 
	}

	@Override
	public MapCodec<? extends Connector> codec()
	{
		return CODEC;
	}
}
