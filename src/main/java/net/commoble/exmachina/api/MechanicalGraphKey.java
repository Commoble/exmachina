package net.commoble.exmachina.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Unique keys of nodes in mechanical graphs.
 * @param levelKey ResourceKey of the level the node is in
 * @param pos BlockPos where the node is
 * @param shape NodeSHape identifying the node's position within that blockpos
 */
public record MechanicalGraphKey(ResourceKey<Level> levelKey, BlockPos pos, NodeShape shape)
{
	/**
	 * {@return true if this node key is compatible with some connection request given the preferred node key} 
	 * @param preferredKey MechanicalGraphKey which some requesting node is trying to form a connection to
	 */
	public boolean isValidFor(MechanicalGraphKey preferredKey)
	{
		return this.levelKey == preferredKey.levelKey
			&& this.pos.equals(preferredKey.pos)
			&& this.shape.isValidFor(preferredKey.shape);
	}
}
