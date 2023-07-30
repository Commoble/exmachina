package commoble.exmachina.api.content;

import java.util.ArrayList;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

/**
 * Connector providing the union of the position sets of a list of subconnectors.
<pre>
{
	"type": "exmachina:union",
	"values":
	[
		{
			// another connector object
		},
		{
			// another connector object
		},
	// etc
	]
}
</pre>
 * "values" must have at least one non-empty connector or baking will fail. Use exmachina:none to represent an empty connector instead. 
 */
public record UnionConnector(List<Connector> values) implements Connector
{
	public static final ResourceKey<Codec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.getModRL(Names.UNION));
	public static final Codec<UnionConnector> CODEC = Connector.CODEC.listOf()
		.fieldOf("values")
		.xmap(UnionConnector::new, UnionConnector::values)
		.codec();
	
	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		List<BlockConnector> blockConnectors = new ArrayList<>();
		for (Connector c : this.values)
		{
			DataResult<BlockConnector> result = c.bake(block);
			var optional = result.result();
			if (!optional.isPresent())
				return result;
			BlockConnector b = optional.get();
			if (b.isPresent())
			{
				blockConnectors.add(b);
			}
		}
		if (blockConnectors.isEmpty())
		{
			return DataResult.error(() -> "No non-empty subconnectors in list: " + this.values);
		}
		return DataResult.success(state -> {
			List<StateConnector> list = new ArrayList<>();
			for (BlockConnector b : blockConnectors)
			{
				list.add(b.getStateConnector(state));
			}
			
			return (level, pos) -> {
				Set<BlockPos> set = new HashSet<>();
				for (StateConnector s : list)
				{
					set.addAll(s.connectedPositions(level, pos));
				}
				return set;
			};
		});
	}

	@Override
	public Codec<? extends Connector> codec()
	{
		return CODEC;
	}

	@Override
	public boolean isPresent()
	{
		return this.values.size() > 0;
	}
}
