package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.internal.ExMachina;
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
 * @param values List of sub-Connectors 
 */
public record UnionConnector(List<Connector> values) implements Connector
{
	/** exmachina:connector_type / exmachina:union */
	public static final ResourceKey<MapCodec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.id("union"));
	
	/**
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
	 */
	public static final MapCodec<UnionConnector> CODEC = Connector.CODEC.listOf()
		.fieldOf("values")
		.xmap(UnionConnector::new, UnionConnector::values);
	
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
	public MapCodec<? extends Connector> codec()
	{
		return CODEC;
	}

	@Override
	public boolean isPresent()
	{
		return this.values.size() > 0;
	}
}
