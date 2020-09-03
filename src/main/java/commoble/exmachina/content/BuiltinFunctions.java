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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.data.DefinedCircuitComponent;
import commoble.exmachina.data.StateReader;
import commoble.exmachina.util.DirectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
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
	 * @param world A world
	 * @param pos A position in the world
	 * @param state The blockstate at the given position in the world
	 * @return The set of the six positions adjacent to the given position
	 */
	public static Set<BlockPos> getAllDirectionsConnectionSet(IWorld world, BlockPos pos, BlockState state)
	{
		return getAdjacentPositions(pos, Direction.values());
	}
	
	public static Set<BlockPos> getAdjacentPositions(BlockPos pos, Direction[] directions)
	{
		Set<BlockPos> positions = new HashSet<>();
		int size = directions.length;
		for (int i=0; i<size; i++)
		{
			positions.add(pos.offset(directions[i]));
		}
		return positions;
	}
	
	public static ConnectorFactory readRotatableDirections(@Nonnull JsonObject object)
	{
		// names of the base directions
		JsonElement valuesElement = object.get("values");
		if (valuesElement == null)
		{
			return DefinedCircuitComponent.NO_CONNECTOR_FACTORY;
		}
		JsonArray values = valuesElement.getAsJsonArray();
		
		// this is the name of the blockstate DirectionProperty that affects the rotation of our connection directions
		JsonElement facingPropertyElement = object.get("direction_property");
		// if a facing property isn't specified, don't rotate the specified directions for this block
		if (facingPropertyElement == null)
		{
			// filter the directions through a Set to remove duplicates
			Set<Direction> directions = new HashSet<>();
			values.forEach(element -> directions.add(Direction.byName(element.getAsString())));
			Direction[] directionArray = directions.toArray(new Direction[directions.size()]);
			return block -> (world,pos,state) -> getAdjacentPositions(pos, directionArray);
		}
		
		String facingPropertyName = facingPropertyElement.getAsString();
		
		// optional field, will use the default state's direction if not present
		JsonElement unrotatedDirectionElement = object.get("unrotated_direction");
		@Nullable Direction unrotatedDirection = unrotatedDirectionElement == null ? null : Direction.byName(unrotatedDirectionElement.toString());

		// filter the directions through a Set to remove duplicates
		Set<Direction> directions = new HashSet<>();
		values.forEach(element -> directions.add(Direction.byName(element.getAsString())));
		Direction[] directionArray = directions.toArray(new Direction[directions.size()]);
		
		return block -> getRotatedDirections(block, facingPropertyName, unrotatedDirection, directionArray);
	}
	
	public static Connector getRotatedDirections(Block block, String facingPropertyName, @Nullable Direction unrotatedDirection, Direction[] directions)
	{
		Property<?> property = block.getStateContainer().getProperty(facingPropertyName);
		if (property == null || !(property instanceof DirectionProperty))
		{
			return (world,pos,state) -> getAdjacentPositions(pos, directions);
		}
		DirectionProperty directionProperty = (DirectionProperty)property;
		Direction defaultStateFacing = unrotatedDirection == null ? block.getDefaultState().get(directionProperty) : unrotatedDirection;
		return (world,pos,state) -> getRotatedPositions(pos, state, directionProperty, defaultStateFacing, directions);
	}
	
	public static Set<BlockPos> getRotatedPositions(BlockPos pos, BlockState state, DirectionProperty directionProperty, Direction defaultStateFacing, Direction[] directions)
	{
		Set<BlockPos> positions = new HashSet<>();
		Direction currentFacing = state.get(directionProperty);
		
		int size = directions.length;
		for (int i=0; i<size; i++)
		{
			positions.add(pos.offset(DirectionHelper.getRotatedDirection(defaultStateFacing, currentFacing, directions[i])));
		}
		
		return positions;
	}
	
	/**
	 * Return a constant value
	 * @param object A json object for the static property
	 * @return a static property factory
	 * @throws JsonParseException If "value" field in json is null
	 */
	public static StaticPropertyFactory getConstantPropertyReader(@Nonnull JsonObject object)
	{
		JsonElement valueElement = object.get("value");
		if (valueElement == null)
		{
			throw new JsonParseException("Constant property must specify a value field");
		}
		double value = valueElement.getAsDouble();
		return block -> state -> value;
	}
	
	public static StaticPropertyFactory getStateTablePropertyReader(@Nonnull JsonObject object)
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
