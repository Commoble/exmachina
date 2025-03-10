package net.commoble.exmachina.internal.mechanical;

import java.util.Objects;

import net.commoble.exmachina.api.ExMachinaDataMaps;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.content.NoneMechanicalComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public record MechanicalBlockState(BlockState state, MechanicalComponent component)
{
	public static MechanicalBlockState getOrDefault(BlockGetter blockGetter, BlockPos pos)
	{
		BlockState state = blockGetter.getBlockState(pos);
		MechanicalComponent component = Objects.requireNonNullElse(
			BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.MECHANICAL_COMPONENT, state.getBlockHolder().getKey()),
			NoneMechanicalComponent.INSTANCE);
		return new MechanicalBlockState(state, component);
	}
}
