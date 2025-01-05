package net.commoble.exmachina.internal.signal;

import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.Node;

/**
 * Represents a node position in a signal graph
 * @param node Node in the graph (position and volume of the node)
 * @param channel Channel which channel the signal is propagating on
 */
@ApiStatus.Internal
public record NodeOnChannel(Node node, Channel channel)
{
	/**
	 * {@return true if the thing trying to connect to the preferred node can connect to this node}
	 * @param preferredNode the thing which the thing which is trying to connect to this node is trying to connect to
	 */
	public boolean isValidFor(NodeOnChannel preferredNode)
	{
		return this.isValidFor(preferredNode.node, preferredNode.channel);
	}

	/**
	 * {@return true if the thing trying to connect to the preferred node can connect to this node}
	 * @param preferredNode the thing which the thing which is trying to connect to this node is trying to connect to
	 * @param preferredChannel the channel which the thing which is trying to connect to this node is trying to connect to
	 */
	public boolean isValidFor(Node preferredNode, Channel preferredChannel)
	{
		return this.node.isValidFor(preferredNode)
			&& this.channel.getConnectableChannels().contains(preferredChannel);
	}
}
