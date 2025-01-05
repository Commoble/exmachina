package net.commoble.exmachina.api;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

/**
 * Definition of a {@link SignalTransmitter} graph node.
 * @param shape NodeShape of the node
 * @param powerReaders Set of Directions to read adjacent vanilla redstone signal power into this graph from (only blocks outside of the graph are read from)
 * @param connectableNodes Set of Nodes which this graph node can connect to. This should specify the smallest connectable NodeShape; larger nodeshapes at the target pos will form connections, while smaller nodeshapes will not.
 * @param graphListener BiFunction to run when a graph update occurs.
 * The provided integer is the graph's new signal power level in the range [0,15],
 * while the Map indicates what adjacent directions should receive neighbor updates after the signal graph updates.
 */
public record TransmissionNode(
	NodeShape shape,
	Set<Direction> powerReaders,
	Set<Node> connectableNodes,
	BiFunction<LevelAccessor,Integer,Map<Direction, SignalStrength>> graphListener)
{
}
