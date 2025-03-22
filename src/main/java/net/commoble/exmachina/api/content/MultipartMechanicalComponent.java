package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.MechanicalBlockComponent;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.MechanicalNodeStates;
import net.commoble.exmachina.internal.ExMachina;
import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * General-purpose MechanicalComponent which has a format similar to multipart blockstate defintions,
 * assigning nodes to blockstates by comparing states to property-value predicate Cases.
 * Multiple sets of nodes can be applied to individual blockstates.
 * 
 * @param save If true, mechanical updates will be stored in a {@link MechanicalNodeStates} data attachment. Defaults false.
 * Automatic syncing is currently not supported, but blockentities which wish to manually sync this can invoke {@link Level#sendBlockUpdated} from {@link BlockEntity#setChanged()}.
 * @param cases List of ApplyWhen cases
 */
public record MultipartMechanicalComponent(boolean save, List<ApplyWhen> multipart) implements MechanicalComponent
{
	/** exmachina:mechanical_component / exmachina:multipart */
	public static final ResourceKey<MapCodec<? extends MechanicalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE, ExMachina.id("multipart"));
	
	/**
	 * <pre>
	 * {
	 *   "type": "exmachina:multipart",
	 *   "save": true, // automatically persists updates in data attached to blockentity, defaults to false
	 *   "multipart": [
	 *   	{
	 *   		// apply one or more nodes in all cases
	 *   		"apply": {
	 *   			// see RawNode class for object definition
	 *   		}
	 *   	},
	 *   	{
	 *   		// apply one or more nodes in specific case
	 *   		"apply": [
	 *   			{
	 *   				// see RawNode class for object definition
	 *   			}
	 *   		],
	 *   		"when": { // list of blockstate predicates, same format as multipart blockstate model jsons
	 *   			"up": "true",
	 *   			"face": "nortH|south"
	 *   		}
	 *     }
	 *   ]
	 * }
	 * </pre>
	 **/
	public static final MapCodec<MultipartMechanicalComponent> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.BOOL.fieldOf("save").forGetter(MultipartMechanicalComponent::save),
			ApplyWhen.CODEC.listOf().fieldOf("multipart").forGetter(MultipartMechanicalComponent::multipart)
		).apply(builder, MultipartMechanicalComponent::new));


	@Override
	public MapCodec<? extends MechanicalComponent> codec()
	{
		return CODEC;
	}

	@Override
	public DataResult<MechanicalBlockComponent> bake(Block block, RegistryAccess registries)
	{
		Map<BlockState, List<RawNode>> bakedNodes = new HashMap<>();
		for (ApplyWhen applyWhen : this.multipart)
		{
			List<RawNode> apply = applyWhen.apply;
			if (apply.isEmpty())
			{
				continue;
			}
			DataResult<Predicate<BlockState>> result = applyWhen.when.map(either -> either.map(
					orCase -> orCase.apply(block),
					c -> c.apply(block)))
				.orElse(DataResult.success(Predicates.alwaysTrue()));
			if (result.isError())
				return DataResult.error(() -> String.format("Failed to parse exmachina:multipart component for block %s: %s", block, result.error().get().message()));
			Predicate<BlockState> predicate = result.getOrThrow();
			for (BlockState state : block.getStateDefinition().getPossibleStates())
			{
				if (predicate.test(state))
				{
					bakedNodes.computeIfAbsent(state, $ -> new ArrayList<>()).addAll(apply);
				}
			}
		}
		
		return DataResult.success((state,$) -> {
			List<RawNode> stateNodes = bakedNodes.get(state);
			return new SimpleMechanicalStateComponent(this.save, stateNodes == null ? List.of() : stateNodes);
		});
	}
	/**
	 * Datagen helper; Creates and returns a MultipartMechanicalComponent for whom cases can be defined.
	 * @return a mutable MultipartMechanicalComponent
	 */
	public static MultipartMechanicalComponent builder(boolean save)
	{
		return new MultipartMechanicalComponent(save, new ArrayList<>());
	}
	
	/**
	 * Adds an apply-when case to this multipart definition
	 * @param applyWhen ApplyWhen to add
	 * @return this
	 */
	public MultipartMechanicalComponent addApplyWhen(ApplyWhen applyWhen)
	{
		this.multipart.add(applyWhen);
		return this;
	}
	/**
	 * Component of MultipartMechanicalComponent definitions, analogous to the "when" and "apply" blocks
	 * in a blockstate json.
	 * 
	 * @param apply List of nodes to apply to a blockstate at if the 'when' case
	 * applies to that blockstate. Must contain at least one node.
	 * @param when Optional field specifying one or more case predicates to apply to each blockstate.
	 * If a Case is provided, the nodes will be used for blockstates that match that case.
	 * If an OrCase is provided, the nodes will be used for blockstates that match any of the sub-cases.
	 * If no case is provided, the nodes will be used for all blockstates. 
	 */
	public static record ApplyWhen(List<RawNode> apply, Optional<Either<OrCase,Case>> when)
	{

		/** codec **/
		public static final Codec<ApplyWhen> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				CodecHelper.singleOrPluralCodec(RawNode.CODEC).fieldOf("apply").forGetter(ApplyWhen::apply),
				Codec.either(OrCase.CODEC, Case.CODEC).optionalFieldOf("when").forGetter(ApplyWhen::when)
			).apply(builder, ApplyWhen::new));
		
		/**
		 * Builder-like factory for datageneration, using a single-case when.
		 * @param when CaseDefinition, all of whose cases must be true to apply the nodes.
		 * @param apply RawNode to apply when the 'when' is true for a given blockstate.
		 * @param additionalNodes optional additional RawNode(s) to include.
		 * @return ApplyWhen builder
		 */
		public static ApplyWhen when(Case when, RawNode apply, RawNode... additionalNodes)
		{
			return new ApplyWhen(Lists.asList(apply, additionalNodes), Optional.of(Either.right(when)));
		}
		
		/**
		 * Builder-like factory for datageneration, using an OR-case when.
		 * @param when OrCase, any of whose cases must be true to apply the RawNode
		 * @param apply RawNode to apply when the 'when' is true for a given blockstate.
		 * @param additionalNodes optional additional RawNode(s) to include.
		 * @return ApplyWhen builder
		 */
		public static ApplyWhen or(OrCase when, RawNode apply, RawNode... additionalNodes)
		{
			return new ApplyWhen(Lists.asList(apply, additionalNodes), Optional.of(Either.left(when)));
		}
		
		/**
		 * Builder-like factory for datageneration, applying one or more RawNode(s) to all blockstates.
		 * @param apply RawNode to apply to all blockstates.
		 * @param additionalNodes optional additional RawNode(s) to include.
		 * @return ApplyWhen builder
		 */
		public static ApplyWhen always(RawNode apply, RawNode... additionalNodes)
		{
			return new ApplyWhen(Lists.asList(apply, additionalNodes), Optional.empty());
		}
	}
	
	/**
	 * Component of Multipart definitions representing a blockstate predicate.
	 * Each entry in this map is of the form "property": "values", using '|' to unionize multiple possible values,
	 * e.g. "side": "north|south|east".
	 * Each entry in the map must be true for a given blockstate for this case to allow
	 * nodes to be applied to that state.
	 * 
	 * @param conditions Map of property-value conditions.
	 */
	public static record Case(Map<String,String> conditions) implements Function<Block, DataResult<Predicate<BlockState>>>
	{
		/** codec **/
		public static final Codec<Case> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
			.xmap(Case::new, Case::conditions);
		
		public DataResult<Predicate<BlockState>> apply(Block block)
		{
			var stateDefinition = block.getStateDefinition();
			Map<Property<?>, Set<Comparable<?>>> allowedPropertyValues = new HashMap<>(); 
			for (var entry : this.conditions.entrySet())
			{
				String propertyName = entry.getKey();
				String valueString = entry.getValue();
				Property<?> prop = stateDefinition.getProperty(propertyName);
				if (prop == null)
				{
					return DataResult.error(() -> String.format("Invalid property %s for block %s", propertyName, block)); 
				}
				String[] values = valueString.split("\\|");
				Set<Comparable<?>> valueSet = new HashSet<>();
				for (String value : values)
				{
					var parsedValue = prop.getValue(value);
					if (parsedValue.isEmpty())
					{
						return DataResult.error(() -> String.format("Invalid value %s for property %s in block %s", value, propertyName, block));
					}
					var propertyValue = parsedValue.get();
					valueSet.add(propertyValue);
				}
				allowedPropertyValues.put(prop, valueSet);
			}
			
			// predicate returns true if all state-value predicates are true for block
			return DataResult.success(state -> {
				for (var entry : allowedPropertyValues.entrySet())
				{
					if (!entry.getValue().contains(state.getValue(entry.getKey())))
					{
						return false;
					}
				}
				return true;
			});
		}
		
		/**
		 * Datagen convenience method returning a single-property case
		 * @param <T> The property's value type, e.g. facing properties use {@link Direction}
		 * @param property Blockstate property, e.g. {@link BlockStateProperties#FACING}
		 * @param value A property value, e.g. {@link Direction#EAST}
		 * @param additionalValues optional additional values to add.
		 * Multipart allows multiple values per property in a given case,
		 * e.g. "east": "side|up" from redstone
		 * @return this
		 */
		@SafeVarargs
		public static <T extends Comparable<T>> Case create(Property<T> property, T value, T... additionalValues)
		{
			return Case.builder().addCondition(property, value, additionalValues);
		}
		
		/**
		 * Builder-like factory for convenience
		 * @return A CaseDefinition with mutable conditions
		 */
		public static Case builder()
		{
			return new Case(new HashMap<>());
		}
		
		/**
		 * Datagen helper; mutably adds a blockstate property condition for one or more property values to this case
		 * @param <T> The property's value type, e.g. facing properties use {@link Direction}
		 * @param property Blockstate property, e.g. {@link BlockStateProperties#FACING}
		 * @param value A property value, e.g. {@link Direction#EAST}
		 * @param additionalValues optional additional values to add.
		 * Multipart allows multiple values per property in a given case,
		 * e.g. "east": "side|up" from redstone
		 * @return this
		 */
		@SafeVarargs
		public final <T extends Comparable<T>> Case addCondition(Property<T> property, T value, T... additionalValues)
		{
			// multipart allows multiple values to be or'd with |, e.g. "east": "side|up"
			StringBuilder combinedValues = new StringBuilder(property.getName(value));
			for (T v : additionalValues)
			{
				combinedValues.append("|" + property.getName(v));
			}
			this.conditions.put(property.getName(), combinedValues.toString());
			return this;
		}
	}
	
	/**
	 * Component of Multipart definitions' 'when' blocks. Represents a union of blockstate predicates,
	 * where if any of the cases are true for a given state, then the nodes specified in the corresponding 'apply'
	 * block will be used for that blockstate.
	 * 
	 * @param cases List of Cases and/or OrCases to predicate blockstates with
	 */
	public static record OrCase(List<Either<OrCase, Case>> cases) implements Function<Block, DataResult<Predicate<BlockState>>>
	{
		/** codec **/
		public static final Codec<OrCase> CODEC =
			Codec.either(Codec.lazyInitialized(() -> OrCase.CODEC), Case.CODEC)
				.listOf().fieldOf("OR").codec()
				.xmap(OrCase::new, OrCase::cases);
		
		public DataResult<Predicate<BlockState>> apply(Block block)
		{
			List<Predicate<BlockState>> predicates = new ArrayList<>();
			for (var either : this.cases)
			{
				var dataResult = either.map(
					orCase -> orCase.apply(block),
					c -> c.apply(block));
				if (dataResult.isError())
					return dataResult;
				predicates.add(dataResult.result().get());
			}
			return DataResult.success(state -> {
				for (Predicate<BlockState> p : predicates)
				{
					if (p.test(state))
						return true;
				}
				return false;
			});
		}
		
		/**
		 * Builder-like helper for datageneration
		 * @return OrCase with mutable list of conditions
		 */
		public static OrCase builder()
		{
			return new OrCase(new ArrayList<>());
		}
		
		/**
		 * Adds a case to this OrCase's list of cases (only usable if list is mutable)
		 * @param theCase Case to add
		 * @return this
		 */
		public OrCase addCase(Case theCase)
		{
			this.cases.add(Either.right(theCase));
			return this;
		}
		
		/**
		 * Adds an OrCase to this OrCase's list of cases (only usable if list is mutable)
		 * @param orCase OrCase to add
		 * @return this
		 */
		public OrCase addOrCase(OrCase orCase)
		{
			this.cases.add(Either.left(orCase));
			return this;
		}
	}
}
