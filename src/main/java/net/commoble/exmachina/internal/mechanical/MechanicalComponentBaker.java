package net.commoble.exmachina.internal.mechanical;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.MechanicalBlockComponent;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.MechanicalStateComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cache of mechanical graph components.
 * Lets components effectively validate/parse with their block in context on load.
 */
@ApiStatus.Internal
public enum MechanicalComponentBaker
{
	/** the Mechanical Component Baker **/
	INSTANCE;
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Object2ObjectMap<Block, MechanicalBlockComponent> bakedBlockComponents = new Object2ObjectOpenHashMap<>();
	private Object2ObjectMap<BlockState, MechanicalStateComponent> bakedStateComponents = new Object2ObjectOpenHashMap<>();
	
	/**
	 * Clears the cache
	 */
	public void clear()
	{
		this.bakedBlockComponents = new Object2ObjectOpenHashMap<>();
		this.bakedStateComponents = new Object2ObjectOpenHashMap<>();
	}
	
	/**
	 * {@return MechanicalStateComponent for the given state}
	 * @param state BlockState to get the state component for
	 * @param registries RegistryAccess for creating the state component if needed
	 */
	public MechanicalStateComponent getStateComponent(BlockState state, RegistryAccess registries)
	{
		return bakedStateComponents.computeIfAbsent(state, theState -> this.computeStateComponent(state, registries));
	}
	
	/**
	 * Bakes block components. Invoked on server start.
	 * @param registries RegistryAccess for baing/validating block components, if needed
	 */
	public void preBake(RegistryAccess registries)
	{
		this.clear();
		this.bakedBlockComponents = bakeBlockComponents(registries);
	}
	
	private static Object2ObjectMap<Block, MechanicalBlockComponent> bakeBlockComponents(RegistryAccess registries)
	{
		Object2ObjectMap<Block, MechanicalBlockComponent> map = new Object2ObjectOpenHashMap<>();
		for (var entry : registries.lookupOrThrow(ExMachinaRegistries.MECHANICAL_COMPONENT).entrySet())
		{
			ResourceKey<MechanicalComponent> key = entry.getKey();
			ResourceLocation blockId = key.location();
			MechanicalComponent mechanicalComponent = entry.getValue();
			Block block = BuiltInRegistries.BLOCK.getValue(blockId);
			// make sure block exists and isn't air
			if (block == Blocks.AIR)
			{
				LOGGER.error("{} is invalid as no valid matching block exists", key);
				continue;
			}
			mechanicalComponent.bake(block, registries)
				.resultOrPartial(err -> LOGGER.error("Failed to bake {}: {}", key, err))
				.filter(MechanicalBlockComponent::isPresent)
				.ifPresent(blockComponent -> map.put(block, blockComponent));
		}
		return map;
	}
	
	private MechanicalStateComponent computeStateComponent(BlockState state, RegistryAccess registries)
	{
		Block block = state.getBlock();
		MechanicalBlockComponent blockComponent = this.bakedBlockComponents.get(block);
		return blockComponent == null || !blockComponent.isPresent()
			? MechanicalStateComponent.EMPTY
			: blockComponent.bake(state, registries);
	}
}
