package net.commoble.exmachina.internal.signal;

import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.Face;

/**
 * Represents a node position in a signal graph
 * @param face BlockPos + Direction, where the Direction is an internal face of the blockpos (down = bottom of that pos)
 * @param channel Channel which channel the signal is propagating on
 */
@ApiStatus.Internal
public record NodePos(Face face, Channel channel)
{
}
