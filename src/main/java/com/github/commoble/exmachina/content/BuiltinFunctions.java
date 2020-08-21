package com.github.commoble.exmachina.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.exmachina.data.StateReader;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BuiltinFunctions
{
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
	
	public static ToDoubleFunction<BlockState> getConstantProperty(Block block, Map<String, Double> data)
	{
		double value = data == null ? 0D : data.getOrDefault("value", 0D);
		
		return state -> value;
	}
	
	public static ToDoubleFunction<BlockState> getStateTableProperty(Block block, Map<String, Double> data)
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
