package net.commoble.exmachina.api.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.Names;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

/**
 * Connector for a block that cannot connect to or be part of a circuit.
<pre>
{
	"type": "exmachina:none"
}
</pre>
 */
public enum NoneConnector implements Connector
{
	INSTANCE;
	
	public static final ResourceKey<MapCodec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.id(Names.NONE));
	public static final MapCodec<NoneConnector> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		return DataResult.success(BlockConnector.EMPTY);
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public MapCodec<? extends Connector> codec()
	{
		return CODEC;
	}
}
