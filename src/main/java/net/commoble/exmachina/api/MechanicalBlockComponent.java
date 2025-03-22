package net.commoble.exmachina.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cached and validated MechanicalComponent for the block the component is assigned to.
 * 
 * The purpose of this is to allow json formats sensitive to e.g. blockstate properties or block classes to verify
 * that the format is correct for the given block and log errors immediately after datapack registries load.
 */
@FunctionalInterface
public interface MechanicalBlockComponent
{
	/** MechanicalBlockComponent for blocks which have no assigned component **/
	public static final MechanicalBlockComponent EMPTY = (state,registries) -> MechanicalStateComponent.EMPTY;
	
	/**
	 * Creates a MechanicalStateComponent for the given blockstate. Called once per blockstate when first needed during server runtime.
	 * @param state BlockState to bake the component for
	 * @param registries RegistryAccess containing datapack registries, if needed
	 * @return MechanicalStateComponent for the given blockstate
	 */
	public abstract MechanicalStateComponent bake(BlockState state, RegistryAccess registries);
	
	/**
	 * {@return true if this is a "real" component, false if this is the EMPTY component (e.g. because validation failed)}
	 */
	public default boolean isPresent()
	{
		return this != EMPTY;
	}
}
