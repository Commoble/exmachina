package net.commoble.exmachina.api.content;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.Names;
import net.commoble.exmachina.internal.util.StateReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Connector for a block whose connections depend on blockstate properties.
 * Blockstates are designated similarly to variant blockstate jsons.
 * e.g. for a furnace block that only connects on one side:
<pre>
{
	"type": "exmachina:blockstate",
	"variants": {
		"facing=north": ["north", "up"],
		"facing=south": ["south", "up"],
		"facing=west": ["west", "up"],
		"facing=east": ["east", "up"]
	}
}
</pre>
 * Not all states are required to be specified (unspecified states default to empty connectors), but a state cannot be specified by more than one variant.
 */
public record BlockStateConnector(Map<String, EnumSet<Direction>> variants) implements Connector
{
	public static final ResourceKey<MapCodec<? extends Connector>> KEY = ResourceKey.create(ExMachinaRegistries.CONNECTOR_TYPE, ExMachina.id(Names.BLOCKSTATE));
	public static final MapCodec<BlockStateConnector> CODEC = Codec.unboundedMap(
			Codec.STRING,
			Direction.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf))
		.fieldOf("variants")
		.xmap(BlockStateConnector::new, BlockStateConnector::variants);
	
	@Override
	public DataResult<BlockConnector> bake(Block block)
	{
		if (this.variants.isEmpty())
		{
			return DataResult.error(() -> "Variants cannot be empty");
		}
		else
		{
			Object2ObjectMap<BlockState, EnumSet<Direction>> map = new Object2ObjectOpenHashMap<>();
			StateDefinition<Block, BlockState> stateContainer = block.getStateDefinition();
			List<BlockState> states = stateContainer.getPossibleStates();
			for (var entry : variants.entrySet())
			{
				String variantKey = entry.getKey();
				EnumSet<Direction> directions = entry.getValue();
				// we're using a try/catch here on parseVariantKey because I haven't find a
				// good way to promote error results with the wrong type from inside a loop
				try
				{
					Predicate<BlockState> stateFilter = StateReader.parseVariantKey(stateContainer, variantKey);
					for (BlockState state : states)
					{
						if (stateFilter.test(state))
						{
							if (map.containsKey(state))
							{
								return DataResult.error(() -> String.format("BlockState %s cannot be specified in more than one variant", state));
							}
							map.put(state, directions);
						}
					}
				}
				catch(IllegalArgumentException e)
				{
					return DataResult.error(() -> e.getMessage());
				}
			}
			return DataResult.success(state -> {
				EnumSet<Direction> dirs = map.get(state);
				return dirs == null
					? StateConnector.EMPTY
					: (level, pos) -> {
						Set<BlockPos> set = new HashSet<>();
						for (Direction dir : dirs)
						{
							set.add(pos.relative(dir));
						}
						return set;
					};
			});
		}
	}

	@Override
	public MapCodec<? extends Connector> codec()
	{
		return CODEC;
	}

}
