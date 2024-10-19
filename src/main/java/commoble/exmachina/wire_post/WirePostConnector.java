package commoble.exmachina.wire_post;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.engine.api.Connector;
import net.minecraft.world.level.block.Block;

public final class WirePostConnector implements Connector
{
	public static final WirePostConnector INSTANCE = new WirePostConnector();
	public static final Codec<WirePostConnector> CODEC = Codec.unit(INSTANCE);

	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		return DataResult.success(state -> (level, pos) -> level.getBlockEntity(pos) instanceof WirePostBlockEntity post
			? post.getRemoteConnections()
			: Set.of());
	}

	@Override
	public Codec<? extends Connector> codec()
	{
		return CODEC;
	}
	
	
}
