package net.commoble.exmachina.internal.mechanical;

import net.commoble.exmachina.api.MechanicalStateComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cached BlockState + its associated MechanicalStateComponent
 * @param state some BlockState
 * @param component the blockstate's MechanicalStateComponent
 */
public record MechanicalBlockState(BlockState state, MechanicalStateComponent component)
{
	/**
	 * Gets the BlockState at a given position and its MechanicalStateComponent, if any
	 * @param blockGetter BlockGetter to query for the blockstate
	 * @param pos BlockPos to get the blockstate at
	 * @param registries RegistryAccess to evaluate the MechanicalStateComponent with if it needs to be created
	 * @return MechanicalBlockState of the state at the given position + its MechanicalStateComponent
	 * (which may be MechanicalStateComponent.EMPTY if the block has no associated component)
	 */
	public static MechanicalBlockState getOrDefault(BlockGetter blockGetter, BlockPos pos, RegistryAccess registries)
	{
		BlockState state = blockGetter.getBlockState(pos);
		MechanicalStateComponent component = MechanicalComponentBaker.INSTANCE.getStateComponent(state,registries);
		return new MechanicalBlockState(state, component);
	}
}
