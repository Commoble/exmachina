package net.commoble.exmachina.api;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

/**
 * Properties of a {@link SignalComponent} graph node.
 * @param shape NodeShape of the node
 * @param source ToIntFunction which provides power to the graph (which should NOT be based on neighbor power, use powerReaders for that instead)
 * @param powerReaders Set of Directions to read adjacent vanilla redstone signal power into this graph from (only blocks outside of the graph are read from)
 * @param connectableNodes Set of Nodes which this graph node can connect to. This should specify the smallest connectable NodeShape; larger nodeshapes at the target pos will form connections, while smaller nodeshapes will not.
 * @param graphListener BiFunction to run when a graph update occurs.
 * The provided integer is the graph's new signal power level in the range [0,15],
 * while the Map indicates what adjacent directions should receive neighbor updates after the signal graph updates.
 */
public record TransmissionNode(
	NodeShape shape,
	ToIntFunction<LevelReader> source,
	Set<Direction> powerReaders,
	Set<SignalGraphKey> connectableNodes,
	BiFunction<LevelAccessor,Integer,Map<Direction, SignalStrength>> graphListener)
{
}
