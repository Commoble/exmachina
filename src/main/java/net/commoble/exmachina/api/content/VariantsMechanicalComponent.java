package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.MechanicalBlockComponent;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.MechanicalNodeStates;
import net.commoble.exmachina.api.MechanicalStateComponent;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.util.CodecHelper;
import net.commoble.exmachina.internal.util.StateReader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * General-purpose MechanicalComponent which defines static mechanical nodes per-blockstate similar to how a blockstate file assigns model variants.
 * @param save If true, mechanical updates will be stored in a {@link MechanicalNodeStates} data attachment. Defaults false.
 * Automatic syncing is currently not supported, but blockentities which wish to manually sync this can invoke {@link Level#sendBlockUpdated} from {@link BlockEntity#setChanged()}.
 * @param variants Map of blockstate selector string (e.g. "side=down,waterlogged=false") to any number of node definitions.
 * NodeShapes within a variant must be unique, and each state must be matched by exactly one selector.
 * The "" selector may be used to match all states.
 */
public record VariantsMechanicalComponent(
	boolean save,
	Map<String,List<RawNode>> variants) implements MechanicalComponent
{
	/** exmachina:mechanical_component / exmachina:variants */
	public static final ResourceKey<MapCodec<? extends MechanicalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE, ExMachina.id("variants"));
	
	/**
	 * <pre>
	 * {
	 *   "type": "exmachina:variants",
	 *   "save": true, // automatically persists updates in data attached to blockentity, defaults to false
	 *   "variants": [
	 *     "side=down,waterlogged=false": [ // same format as blockstate json variant selectors
	 *       // list of RawNode objects, see that class for additional docs
	 *     ],
	 *     "side=up": {
	 *     	// singular RawNode object is fine too
	 *     }
	 *   ]
	 * }
	 * </pre>
	 */
	public static final MapCodec<VariantsMechanicalComponent> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.BOOL.optionalFieldOf("save", false).forGetter(VariantsMechanicalComponent::save),
			Codec.unboundedMap(Codec.STRING, CodecHelper.singleOrPluralCodec(RawNode.CODEC)).fieldOf("variants").forGetter(VariantsMechanicalComponent::variants)
		).apply(builder, VariantsMechanicalComponent::new));

	@Override
	public MapCodec<? extends MechanicalComponent> codec()
	{
		return CODEC;
	}
	
	/**
	 * Datagen helper which assigns the given nodes to all states
	 * @param save If true, mechanical updates will be stored in a {@link MechanicalNodeStates} data attachment.
	 * @param rawNodes RawNodes to assign to all blockstates 
	 * @return VariantsMechanicalComponent where all blockstates use the provided nodes
	 */
	public static VariantsMechanicalComponent always(boolean save, RawNode... rawNodes)
	{
		return new VariantsMechanicalComponent(save, Map.of("", List.of(rawNodes)));
	}
	
	/**
	 * Datagen helper returning a mutable builder-like VariantsMechanicalComponent
	 * @param save If true, mechanical updates will be stored in a {@link MechanicalNodeStates} data attachment.
	 * @return VariantsMechanicalComponent with a mutable internal map to add nodes to
	 */
	public static VariantsMechanicalComponent builder(boolean save)
	{
		return new VariantsMechanicalComponent(save, new HashMap<>());
	}
	
	/**
	 * Datagen helper to add a variant matching a single property's value
	 * @param <T> Type of the blockstate property's values, e.g. Direction
	 * @param property Property of this block's blockstates, e.g. BlockStateProperties.FACING
	 * @param value A value of that property, e.g. Direction.NORTH
	 * @param rawNodes RawNodes to assign to blockstates having that property value (can be none to assign no nodes)
	 * @return this
	 */
	public <T extends Comparable<T>> VariantsMechanicalComponent addVariant(Property<T> property, T value, RawNode... rawNodes)
	{
		return addVariant(List.of(new PropertyValue<>(property,value)), rawNodes);
	}
	
	/**
	 * Datagen helper to add a variant matching multiple properties' values
	 * @param builderConsumer Consumer of the form
	 * <pre>
	 * builder -> builder
	 * 	.add(BlockStateProperties.DIRECTION, Direction.NORTH)
	 * 	.add(BlockStateProperties.LIT, true)	 
	 * </pre>
	 * @param rawNodes zero or more RawNodes to assign to states having all property values specified by the variant
	 * @return this
	 */
	public VariantsMechanicalComponent addMultiPropertyVariant(Consumer<VariantBuilder> builderConsumer, RawNode... rawNodes)
	{
		VariantBuilder variantBuilder = new VariantBuilder();
		builderConsumer.accept(variantBuilder);
		this.addVariant(variantBuilder.propertyValues, rawNodes);
		return this;
	}
	
	/**
	 * Datagen helper to add a variant matching multiple properties' values
	 * @param propertyValues List of PropertyValues for this variant
	 * @param rawNodes RawNodes to assign to states matching all property values (can be none to assign no nodes)
	 * @return this
	 */
	private VariantsMechanicalComponent addVariant(List<PropertyValue<?>> propertyValues, RawNode... rawNodes)
	{
		List<String> variantStrings = new ArrayList<>();
		for (var propertyValue : propertyValues)
		{
			variantStrings.add(propertyValue.variantString());
		}
		this.variants.put(String.join(",", variantStrings), List.of(rawNodes));
		
		return this;
	}

	@Override
	public DataResult<MechanicalBlockComponent> bake(Block block, RegistryAccess registries)
	{
		List<NodeStateMatcher> matchers = new ArrayList<>();
		Map<NodeStateMatcher,String> matcherVariants = new IdentityHashMap<>(); // used for validator below
		for (var entry : this.variants.entrySet())
		{
			String variantString = entry.getKey(); // e.g. "direction=up;waterlogged=true"
			List<RawNode> rawNodes = entry.getValue();
			// validate nodeshapes are unique
			Set<NodeShape> shapes = new HashSet<>();
			for (RawNode rawNode : rawNodes)
			{
				if (!shapes.add(rawNode.shape()))
				{
					return DataResult.error(() -> String.format("Variant %s for block %s has duplicate NodeShape %s", variantString, block, rawNode.shape()));
				}
			}
			try
			{
				Predicate<BlockState> predicate = StateReader.parseVariantKey(block.getStateDefinition(), variantString);
				NodeStateMatcher matcher = new NodeStateMatcher(predicate, rawNodes);
				matchers.add(matcher);
				matcherVariants.put(matcher, variantString);
			}
			catch (Exception e)
			{
				return DataResult.error(() -> String.format("Failed to parse variant %s for block %s: %s", variantString, block, e.getMessage()));
			}
		}
		// if somebody makes selectors that can apply to more than one state, they might get an unexpected result
		for (BlockState state : block.getStateDefinition().getPossibleStates())
		{
			String existingVariant = null;
			for (var matcher : matchers)
			{
				if (matcher.predicate.test(state))
				{
					String thisVariant = matcherVariants.get(matcher);
					if (existingVariant == null)
					{
						existingVariant = thisVariant;
					}
					else
					{
						String err = String.format("BlockState %s matched by multiple variants: %s, %s", state, existingVariant, thisVariant);
						return DataResult.error(() -> err);
					}
				}
			}
			if (existingVariant == null)
			{
				return DataResult.error(() -> String.format("Blockstate %s matched by no variants", state));
			}
		}
		return DataResult.success(new NodeStateDistributor(matchers, this.save));
	}
	
	/**
	 * Pair of a blockstate Property and a value of that property; used for type safety during datagen
	 * @param property Property of a blockstate, e.g. BlockStateProperties.FACING
	 * @param value value of that property, e.g. Direction.NORTH
	 */
	private static record PropertyValue<T extends Comparable<T>>(Property<T> property, T value)
	{
		public String variantString()
		{
			return property.getName() + "=" + property.getName(value);
		}
	}
	
	private static record NodeStateMatcher(Predicate<BlockState> predicate, List<RawNode> rawNodes)
	{
	}
	
	private static record NodeStateDistributor(List<NodeStateMatcher> matchers, boolean save) implements MechanicalBlockComponent
	{
		@Override
		public MechanicalStateComponent bake(BlockState state, RegistryAccess registries)
		{
			for (NodeStateMatcher matcher : this.matchers)
			{
				if (matcher.predicate.test(state))
				{
					return new SimpleMechanicalStateComponent(save, matcher.rawNodes);
				}
			}
			return MechanicalStateComponent.EMPTY;
		}
	}

	/**
	 * Datagen helper for building a variant matching multiple properties' values
	 */
	public static class VariantBuilder
	{
		private List<PropertyValue<?>> propertyValues = new ArrayList<>();
		
		private VariantBuilder()
		{
			
		}
		
		/**
		 * Add a required property-value to this variant
		 * @param <T> Type of the blockstate property, e.g. Direction
		 * @param property Property of this block's blockstates, e.g. BlockStateProperties.FACING
		 * @param value A value of that property, e.g. Direction.NORTH
		 * @return this
		 */
		public <T extends Comparable<T>> VariantBuilder add(Property<T> property, T value)
		{
			this.propertyValues.add(new PropertyValue<>(property, value));
			return this;
		}
	}
}
