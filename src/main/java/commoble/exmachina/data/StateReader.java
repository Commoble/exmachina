package commoble.exmachina.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

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
	 */
	public static Predicate<BlockState> parseVariantKey(StateContainer<Block, BlockState> stateContainer, String keyList)
	{
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
					Comparable<?> propertyValue = parseValue(property, valueName);
					if (propertyValue == null)
					{
						throw new RuntimeException("Unknown value: '" + valueName + "' for blockstate property: '" + keyName + "' " + property.getAllowedValues());
					}

					map.put(property, propertyValue);
				}
				else if (!keyName.isEmpty())
				{
					throw new RuntimeException("Unknown blockstate property: '" + keyName + "'");
				}
			}
		}

		Block block = stateContainer.getOwner();
		return (state) -> {
			if (state != null && block == state.getBlock())
			{
				for (Entry<Property<?>, Comparable<?>> entry : map.entrySet())
				{
					if (!Objects.equals(state.get(entry.getKey()), entry.getValue()))
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

	@Nullable
	private static <T extends Comparable<T>> T parseValue(Property<T> property, String name)
	{
		return property.parseValue(name).orElse((T) null);
	}
}
