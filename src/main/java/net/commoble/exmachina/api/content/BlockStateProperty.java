package net.commoble.exmachina.api.content;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.StaticProperty;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.util.StateReader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * StaticProperty for a block which has different property values for different blockstates.
 * Blockstates are designated similarly to variant blockstate jsons.
 * e.g. for a furnace block:
<pre>
{
	"type": "exmachina:blockstate",
	"variants": {
		"lit=false": 0,
		"lit=true,facing=north": 1,
		"lit=true,facing=west": 2
	}
}
</pre>
 * Not all states are required to be specified (unspecified states default to 0), but a state cannot be specified by more than one variant.
 * @param variants Map of blockstate predicate string to double value for the blockstate(s)
 */
public record BlockStateProperty(Map<String, Double> variants) implements StaticProperty
{
	/** exmachina:static_property_type / exmachina:blockstate */
	public static final ResourceKey<MapCodec<? extends StaticProperty>> KEY = ResourceKey.create(ExMachinaRegistries.STATIC_PROPERTY_TYPE, ExMachina.id("blockstate"));
	
	/**
	 * e.g. for a furnace block:
<pre>
{
	"type": "exmachina:blockstate",
	"variants": {
		"lit=false": 0,
		"lit=true,facing=north": 1,
		"lit=true,facing=west": 2
	}
}
</pre>
	 */
	public static final MapCodec<BlockStateProperty> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
		.fieldOf("variants")
		.xmap(BlockStateProperty::new, BlockStateProperty::variants);
	
	@Override
	public DataResult<BakedStaticProperty> bake(Block block)
	{
		if (this.variants.isEmpty())
		{
			return DataResult.error(() -> "Variants cannot be empty");
		}
		else
		{
			Object2DoubleMap<BlockState> map = new Object2DoubleOpenHashMap<>(); // generally faster to read than arraymaps when n>=3
			StateDefinition<Block, BlockState> stateContainer = block.getStateDefinition();
			List<BlockState> states = stateContainer.getPossibleStates();
			for (Entry<String, Double> entry : variants.entrySet())
			{
				String variantKey = entry.getKey();
				double value = entry.getValue();
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
							map.put(state, value);
						}
					}
				}
				catch(IllegalArgumentException e)
				{
					return DataResult.error(() -> e.getMessage());
				}
			}
			return DataResult.success(map::getDouble);
		}
	}

	@Override
	public MapCodec<? extends StaticProperty> codec()
	{
		return CODEC;
	}
}
