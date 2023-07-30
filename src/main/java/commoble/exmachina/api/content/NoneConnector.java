package commoble.exmachina.api.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.exmachina.ExMachina;
import commoble.exmachina.Names;
import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ExMachinaRegistries;
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
	
	public static final ResourceKey<Codec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.getModRL(Names.NONE));
	public static final Codec<NoneConnector> CODEC = Codec.unit(INSTANCE);

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
	public Codec<? extends Connector> codec()
	{
		return CODEC;
	}
}
