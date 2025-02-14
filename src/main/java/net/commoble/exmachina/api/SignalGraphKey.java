package net.commoble.exmachina.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Uniquely identifiable key in the signal graph
 * @param levelKey ResourceKey of the level where this node is
 * @param pos BlockPos where this node is
 * @param shape NodeShape of this node
 * @param channel Channel this node is on
 */
public record SignalGraphKey(ResourceKey<Level> levelKey, BlockPos pos, NodeShape shape, Channel channel)
{
	/**
	 * {@return true if this node exists at the position of a preferred node and the nodeshapes and channels are compatible}
	 * @param preferredNode Node which the thing which is trying to connect to this node is trying to connect to
	 */
	public boolean isValidFor(SignalGraphKey preferredNode)
	{
		return this.levelKey == preferredNode.levelKey
			&& this.pos.equals(preferredNode.pos)
			&& this.shape.isValidFor(preferredNode.shape)
			&& this.channel.getConnectableChannels().contains(preferredNode.channel);
	}
}
