package commoble.exmachina.content;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.data.StateReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BuiltinFunctions
{
	
	public static final Type STATE_TABLE_TYPE = new TypeToken<Map<String, Double>>(){}.getType();
	public static final Gson GSON = new Gson();
	
	/**
	 * Return the set of the six block positions around a given position.
	 * @param world
	 * @param pos
	 * @return
	 */
	public static Set<BlockPos> getCubeConnections(IWorld world, BlockPos pos)
	{
		Set<BlockPos> set = new HashSet<>();
		
		for (int i=0; i<6; i++)
		{
			set.add(pos.offset(Direction.byIndex(i)));
		}
		return set;
	}
	
	public static StaticPropertyFactory getConstantPropertyReader(@Nonnull JsonObject object) throws JsonParseException
	{
		JsonElement valueElement = object.get("value");
		if (valueElement == null)
		{
			throw new JsonParseException("Constant property must specify a value field");
		}
		double value = valueElement.getAsDouble();
		return block -> state -> value;
	}
	
	public static StaticPropertyFactory getStateTablePropertyReader(@Nonnull JsonObject object) throws JsonParseException
	{
		JsonElement variantsElement = object.get("variants");
		if (variantsElement == null)
		{
			throw new JsonParseException("Blockstate table property must specify a variants object");
		}
		Map<String, Double> variants = GSON.fromJson(variantsElement, STATE_TABLE_TYPE);
		return block -> getStateTableProperty(block, variants);
	}
	
	public static StaticProperty getStateTableProperty(Block block, Map<String, Double> data)
	{
		Map<BlockState, Double> map = getStateMapper(block, data);
		
		return map::get;
	}
	
	private static Map<BlockState, Double> getStateMapper(@Nonnull Block block, @Nullable Map<String, Double> variants)
	{
		if (variants == null || variants.isEmpty())
		{
			return ImmutableMap.of();
		}
		else
		{
			Map<BlockState,Double> map = new HashMap<>();
			StateContainer<Block, BlockState> stateContainer = block.getStateContainer();
			List<BlockState> states = stateContainer.getValidStates();
			for (Entry<String, Double> entry : variants.entrySet())
			{
				String variantKey = entry.getKey();
				double value = entry.getValue();
				Predicate<BlockState> stateFilter = StateReader.parseVariantKey(stateContainer, variantKey);
				states.stream().filter(stateFilter).forEach(state -> map.put(state, value));
			}
			return map;
		}
		
	}
}
