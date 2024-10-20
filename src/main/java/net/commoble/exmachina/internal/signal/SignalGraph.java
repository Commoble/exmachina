package net.commoble.exmachina.internal.signal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.Face;
import net.commoble.exmachina.api.Receiver;
import net.commoble.exmachina.api.SignalStrength;
import net.commoble.exmachina.api.SignalTransmitter;
import net.commoble.exmachina.api.StateWirer;
import net.commoble.exmachina.api.TransmissionNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a graph of connected signal nodes.
 * SignalGraphs are used to determine which blocks are "wired" together,
 * reads the highest level of redstone power accessible by that network,
 * and updates all blocks in the network simultaneously.
 * 
 * SignalGraphs are created at the end of a level tick within which a graph update is required, and discarded after updates occur.
 * 
 * @param nodesInGraph TransmissionNodes in this graph
 * @param blocksInGraph Set of BlockPos where signal transmitter nodes exist
 * @param receiverNodes List of Receiver listeners to notify of power updates after the graph update occurs
 * @param power The highest level of redstone power provided to this graph
 */
@ApiStatus.Internal
public record SignalGraph(Map<NodePos, TransmissionNode> nodesInGraph, Set<BlockPos> blocksInGraph, List<Receiver> receiverNodes, int power)
{
	/**
	 * Creates a SignalGraph at a given origin context
	 * @param level ServerLevel where the signal graph is being created
	 * @param originNodePos NodePos (blockpos + direction + channel) of the origin node
	 * @param originNode TransmissionNode at the origin nodepos
	 * @param knownWirers StateWirer lookup cache
	 * @param unusedReceivers Receiver listeners which were at requested graph update positions but have not been included in any signal graph yet during this graph update
	 * @return SignalGraph created from the origin context
	 */
	@ApiStatus.Internal
	public static SignalGraph fromOriginNode(ServerLevel level, NodePos originNodePos, TransmissionNode originNode, Map<BlockPos, StateWirer> knownWirers, Map<PosReceiver, Set<Receiver>> unusedReceivers)
	{
		// build graph
		Map<NodePos, TransmissionNode> nodesInGraph = new HashMap<>();
		Set<BlockPos> blocksInGraph = new HashSet<>();
		Queue<NodeAtPos> uncheckedNodesInGraph = new LinkedList<>();
		List<Receiver> receiverNodes = new ArrayList<>();
		nodesInGraph.put(originNodePos, originNode);
		blocksInGraph.add(originNodePos.face().pos());
		uncheckedNodesInGraph.add(new NodeAtPos(originNodePos, originNode));
		int maxSize = ExMachina.COMMON_CONFIG.maxSignalGraphSize().getAsInt();
		Function<BlockPos, StateWirer> wirerLookup = targetPos -> knownWirers.computeIfAbsent(targetPos, pos -> StateWirer.getOrDefault(level, pos));
		SignalGetter signalGetter = new WireIgnoringSignalGetter(level, wirerLookup);
		
		int highestPowerFound = 0;
		
		// iterate over the list of nodes we have added to the graph
		// "next" node: in the graph but not processed neighbors yet
		whileLoop:
		while (uncheckedNodesInGraph.poll() instanceof NodeAtPos(NodePos(Face nextFace, Channel nextChannel), TransmissionNode nextNode))
		{
			// process next node's neighbors
			// "target" node: a node which next node can conceivably connect to, and may form a mutual connection, in which case it should be in the graph
			for (Channel targetChannel : nextChannel.getConnectableChannels())
			{
				// look for target nodes
				for (Face targetFace : nextNode.connectableNodes())
				{
					NodePos targetNodePos = new NodePos(targetFace, targetChannel);
					// skip target if it's already in the graph
					if (!nodesInGraph.containsKey(targetNodePos))
					{
						BlockPos targetPos = targetFace.pos();
						StateWirer targetStateWirer = wirerLookup.apply(targetPos);
						BlockState targetState = targetStateWirer.state();
						SignalTransmitter targetTransmitter = targetStateWirer.transmitter();
						
						Direction targetSide = targetFace.attachmentSide();
						@Nullable TransmissionNode targetNode = targetTransmitter.getTransmissionNodes(level, targetPos, targetState, targetSide).get(targetChannel);
						if (targetNode != null)
						{
							// target node exists! but can it connect back?
							if (targetNode.connectableNodes().contains(nextFace))
							{
								// yes
								nodesInGraph.put(targetNodePos, targetNode);
								blocksInGraph.add(targetPos);
								uncheckedNodesInGraph.add(new NodeAtPos(targetNodePos, targetNode));
								if (nodesInGraph.size() >= maxSize)
									break whileLoop;
							}
						}
						
						// check receiver nodes, we need to remember the listeners
						@Nullable Receiver targetReceiver = targetStateWirer.receiver().getReceiverEndpoint(level, targetPos, targetState, targetSide, nextFace, targetChannel);
						if (targetReceiver != null)
						{
							receiverNodes.add(targetReceiver);
							@Nullable Set<Receiver> receiversOnChannel = unusedReceivers.get(new PosReceiver(targetPos, targetStateWirer.receiver()));
							if (receiversOnChannel != null)
							{
								receiversOnChannel.remove(targetReceiver);
							}
						}
						
						// check supplier nodes too
						if (highestPowerFound < 15)
						{
							powerLoop:
							for (var channelPower : targetStateWirer.source().getSupplierEndpoints(level, targetPos, targetState, targetSide, nextFace).entrySet())
							{
								if (channelPower.getKey() == targetChannel)
								{
									int suppliedPower = channelPower.getValue().applyAsInt(level);
									if (suppliedPower > highestPowerFound)
									{
										highestPowerFound = suppliedPower;
										if (highestPowerFound >= 15)
											break powerLoop;
									}
								}
							}
						}
					}
				}
			}
		}
		
		// calculate highest input power
		// these are the places input power can come from:
		// A) supplier endpoint connected to a transmission node
		// B) vanilla power reader supplied by a transmission node
		// highest power level is 15, if we observe this then we can skip the rest of the graph
		// but if our graph is too large then we ignore power and set everything to 0
		// we delay this until after the graph is built because we do not read vanilla signals from blockspos in the graph
		int power;
		if (nodesInGraph.size() >= maxSize)
		{
			power = 0;
		}
		else if (highestPowerFound >= 15)
		{
			power = 15;
		}
		else
		{
			powerReaderLoop:
			for (var entry : nodesInGraph.entrySet())
			{
				NodePos nodePos = entry.getKey();
				TransmissionNode node = entry.getValue();
				Face nodeFace = nodePos.face();
				BlockPos nodeBlockPos = nodeFace.pos();
				for (Direction directionToNeighbor : node.powerReaders())
				{
					BlockPos neighborPos = nodeBlockPos.relative(directionToNeighbor);
					if (!blocksInGraph.contains(neighborPos))
					{
						int neighborPower = signalGetter.getSignal(nodeBlockPos.relative(directionToNeighbor), directionToNeighbor);
						if (neighborPower > highestPowerFound)
						{
							highestPowerFound = neighborPower;
							if (highestPowerFound >= 15)
								break powerReaderLoop;
						}
					}
				}
			}
			
			power = highestPowerFound;
		}
		
		return new SignalGraph(nodesInGraph, blocksInGraph, receiverNodes, power);
	}
	
	/**
	 * {@return whether this graph has a node at the given NodePos}
	 * @param nodePos NodePos to check whether this graph contains
	 */
	@ApiStatus.Internal
	public boolean hasTransmissionNode(NodePos nodePos)
	{
		return nodesInGraph().containsKey(nodePos);
	}

	/**
	 * {@return true if any of the provided graphs contain a transmission node at the given blockpos}
	 * @param pos BlockPos to check whether any graph is containing
	 * @param graphs SignalGraphs to check for the containment of the BlockPos
	 */
	@ApiStatus.Internal
	public static boolean isBlockInAnyGraph(BlockPos pos, Collection<SignalGraph> graphs)
	{
		for (SignalGraph graph : graphs)
		{
			if (graph.blocksInGraph().contains(pos))
				return true;
		}
		return false;
	}

	/**
	 * Updates all SignalTransmitter and SignalReceiver listeners and determines which positions adjacent to the graph should receive neighbor updates.
	 * After all listeners in all graphs update, neighbor updates on blocks which are adjacent to but not within any signal graph occur.
	 * @param serverLevel ServerLevel where this signal graph is
	 * @return Map of node Faces whose neighbors should be updated, e.g. (pos+west) = the block to the west of pos should receive a neighbor update
	 */
	@ApiStatus.Internal
	public Map<Face, SignalStrength> updateListeners(ServerLevel serverLevel)
	{
		Map<Face, SignalStrength> neighborUpdatingNodes = new HashMap<>();
		
		for (var entry : this.nodesInGraph.entrySet())
		{
			NodePos nodePos = entry.getKey();
			BlockPos nodeBlockPos = nodePos.face().pos();
			TransmissionNode node = entry.getValue();
			node.graphListener().apply(serverLevel, this.power).forEach((directionToNeighbor, signalStrength) -> {
				neighborUpdatingNodes.merge(new Face(nodeBlockPos, directionToNeighbor), signalStrength, SignalStrength::max);
			});
		}
		
		for (Receiver receiverNode : this.receiverNodes)
		{
			receiverNode.accept(serverLevel, this.power);
		}
		
		return neighborUpdatingNodes;
	}
	
	private static record NodeAtPos(NodePos pos, TransmissionNode node)
	{

	}
}
