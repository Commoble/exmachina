package net.commoble.exmachina.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record MechanicalGraphKey(ResourceKey<Level> levelKey, BlockPos pos, NodeShape shape)
{
	public boolean isValidFor(MechanicalGraphKey preferredKey)
	{
		return this.levelKey == preferredKey.levelKey
			&& this.pos.equals(preferredKey.pos)
			&& this.shape.isValidFor(preferredKey.shape);
	}
}
