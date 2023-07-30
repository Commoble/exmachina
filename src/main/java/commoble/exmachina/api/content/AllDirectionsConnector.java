package commoble.exmachina.api.content;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.ExMachina;
import commoble.exmachina.Names;
import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ExMachinaRegistries;
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
	
	public static final ResourceKey<Codec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.getModRL(Names.ALL_DIRECTIONS));
	public static final Codec<AllDirectionsConnector> CODEC = Codec.unit(INSTANCE);

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
	public Codec<? extends Connector> codec()
	{
		return CODEC;
	}
}
