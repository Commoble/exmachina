package commoble.exmachina.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class StateReader
{
	private static final Splitter SPLITTER_COMMA = Splitter.on(',');
	private static final Splitter EQUALS_SPLITTER = Splitter.on('=').limit(2);

	/**
	 * This is based on how blockstate jsons are parsed in ModelBakery; we need it to be
	 * available for server data. Parses a blockstate property filter string into a blockstate predicate.
	 * @param stateContainer A statecontainer from a Block
	 * @param keyList A blockstate property filter (same format as blockstate .jsons)
	 * @return A Predicate that returns true for the states specified by the string and false otherwise
	 * @throws IllegalArgumentException if keyList cannot be parsed
	 */
	public static Predicate<BlockState> parseVariantKey(StateDefinition<Block, BlockState> stateContainer, String keyList) throws IllegalArgumentException
	{
		// "" indicates all states are valid
		if (keyList.isEmpty())
			return state -> true;
			
		Map<Property<?>, Comparable<?>> map = Maps.newHashMap();

		for (String keyAndValue : SPLITTER_COMMA.split(keyList))
		{
			Iterator<String> iteratorOverKeyAndValue = EQUALS_SPLITTER.split(keyAndValue).iterator();
			if (iteratorOverKeyAndValue.hasNext())
			{
				String keyName = iteratorOverKeyAndValue.next();
				Property<?> property = stateContainer.getProperty(keyName);
				if (property != null && iteratorOverKeyAndValue.hasNext())
				{
					String valueName = iteratorOverKeyAndValue.next();
					Comparable<?> propertyValue = property.getValue(valueName).orElse(null);
					if (propertyValue == null)
					{
						throw new IllegalArgumentException("Unknown value: '" + valueName + "' for blockstate property: '" + keyName + "' " + property.getAllValues());
					}

					map.put(property, propertyValue);
				}
				else if (!keyName.isEmpty())
				{
					throw new IllegalArgumentException( "Unknown blockstate property: '" + keyName + "'");
				}
			}
		}

		Block block = stateContainer.getOwner();
		return (state) -> {
			if (state != null && block == state.getBlock())
			{
				for (Entry<Property<?>, Comparable<?>> entry : map.entrySet())
				{
					if (!Objects.equals(state.getValue(entry.getKey()), entry.getValue()))
					{
						return false;
					}
				}

				return true;
			}
			else
			{
				return false;
			}
		};
	}
}
