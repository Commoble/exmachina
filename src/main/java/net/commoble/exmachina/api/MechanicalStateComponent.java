package net.commoble.exmachina.api;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * MechanicalComponent baked and validated for a specific state of the component's associated block.
 * Responsible for providing nodes and connections when queried by the mechanical grapher.
 */
@FunctionalInterface
public interface MechanicalStateComponent
{
	/** Represents empty/invalid component **/
	public static final MechanicalStateComponent EMPTY = (levelKey, level, pos) -> List.of();
	
	/**
	 * Provides the MechanicalNodes at a given position where this blockstate is
	 * @param levelKey ResourceKey identifying the level where this component is
	 * @param level BlockGetter where this component is
	 * @param pos BlockPos where this component is
	 * @return Collection of MechanicalNodes at the given position
	 */
	public abstract Collection<MechanicalNode> getNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos);
	
	/**
	 * {@return true if this isn't the EMPTY component}
	 */
	public default boolean isPresent()
	{
		return this == EMPTY;
	}
}
