package net.commoble.exmachina.internal.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaDataMaps;
import net.commoble.exmachina.api.Face;
import net.commoble.exmachina.api.Receiver;
import net.commoble.exmachina.api.SignalReceiver;
import net.commoble.exmachina.api.SignalStrength;
import net.commoble.exmachina.api.SignalTransmitter;
import net.commoble.exmachina.api.StateWirer;
import net.commoble.exmachina.api.TransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Buffer in which signal graph updates are enqueued.
 * All enqueued signal graph updates run at the end of each tick.
 */
@ApiStatus.Internal
public final class SignalGraphBuffer extends SavedData
{
	private SignalGraphBuffer() {}
	
	private static final String ID = "exmachina/signalgraphbuffer";
	
	private static final SavedData.Factory<SignalGraphBuffer> FACTORY = new SavedData.Factory<>(SignalGraphBuffer::new, (tag,registries) -> new SignalGraphBuffer());
	
	private Set<BlockPos> positions = new HashSet<>();

	/**
	 * {@return SignalGraphBuffer for a given ServerLevel}
	 * @param level ServerLevel to get the SignalGraphBuffer for
	 */
	@ApiStatus.Internal
	public static SignalGraphBuffer get(ServerLevel level)
	{
		return level.getDataStorage().computeIfAbsent(FACTORY, ID);
	}

	/**
	 * Enqueues a blockpos to run a signal graph update at at the end of the tick
	 * @param pos BlockPos to enqueue a signal graph update at
	 */
	@ApiStatus.Internal
	public void enqueue(BlockPos pos)
	{
		this.positions.add(pos);
	}

	/**
	 * Invoked at the end of each level tick to run enqueued signal graph updates
	 * @param level ServerLevel being ticked
	 */
	@ApiStatus.Internal
	public void tick(ServerLevel level)
	{
		if (this.positions.isEmpty())
			return;

		Set<BlockPos> originPositions = this.positions;
		this.positions = new HashSet<>();
		
		// construct graph from each origin node
		List<SignalGraph> graphs = new ArrayList<>();
		Map<BlockPos, StateWirer> knownWirers = new HashMap<>();
		Map<NodePos, TransmissionNode> originNodes = new HashMap<>();
		
		// if there's any receivers at the origin node(s)
		// which are NOT tied to a graph
		// then we should invoke them with power=0 after graphing everything else out
		Map<PosReceiver, Set<Receiver>> unusedReceivers = new HashMap<>();
		
		for (BlockPos originPos : originPositions)
		{
			// collect all of the nodes in this block
			BlockState originState = level.getBlockState(originPos);
			Block originBlock = originState.getBlock();
			@SuppressWarnings("deprecation")
			SignalTransmitter originTransmitter = BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_TRANSMITTER, originBlock.builtInRegistryHolder().key());
			@SuppressWarnings("deprecation")
			SignalReceiver originReceiver = BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_RECEIVER, originBlock.builtInRegistryHolder().key());
			if (originTransmitter != null)
			{
				for (Direction face : Direction.values()) {
					originTransmitter.getTransmissionNodes(level, originPos, originState, face).forEach((channel,node) -> {
						originNodes.put(new NodePos(new Face(originPos, face), channel), node);
					});
				}
			}
			if (originReceiver != null)
			{
				PosReceiver posReceiver = new PosReceiver(originPos, originReceiver);
				for (Channel channel : Channel.ALL)
				{
					for (Receiver receiver : originReceiver.getAllReceivers(level, originPos, originState, channel))
					{
						 unusedReceivers.computeIfAbsent(posReceiver, foo -> new HashSet<>()).add(receiver);
					}
				}
			}
			
		}
		originNodes.forEach((nodePos, node) -> {
			// as we construct graphs, ignore origin nodes that exist in existing graphs
			for (SignalGraph priorGraph : graphs)
			{
				if (priorGraph.hasTransmissionNode(nodePos))
				{
					return;
				}
			}
			
			SignalGraph graph = SignalGraph.fromOriginNode(level, nodePos, node, knownWirers, unusedReceivers);
			graphs.add(graph);
		});
		
		// each graph has calculated the highest input power into the graph
		
		// inform all listeners in all graphs of the new power
		// keep track of neighbors to update
		// how do neighbor updates work?
		// each time we proc a listener on a transition node at nodepos, it gives us a set of directions to proc neighbor updates in
		// we store these as a face (nodePos+direction)
		Map<Face, SignalStrength> nodesUpdatingNeighbors = new HashMap<>();
		for (SignalGraph graph : graphs)
		{
			graph.updateListeners(level).forEach((face,signalStrength) -> nodesUpdatingNeighbors.merge(face, signalStrength, SignalStrength::max));
		}
		
		// give power 0 to receivers that were marked for update but never became part of a graph
		unusedReceivers.forEach((receiversAtPos, receiversOnChannels) -> {
			receiversAtPos.receiver().resetUnusedReceivers(level, receiversAtPos.pos(), receiversOnChannels);
		});
		
		// invoke block updates on blocks which are adjacent to the graph but have no transmission nodes within it
		nodesUpdatingNeighbors.forEach((updatedNodeFace, signalStrength) -> {
			BlockPos nodeBlockPos = updatedNodeFace.pos();
			Direction directionToNeighbor = updatedNodeFace.attachmentSide();
			BlockPos neighborPos = nodeBlockPos.relative(directionToNeighbor);
			if (!SignalGraph.isBlockInAnyGraph(neighborPos, graphs))
			{
				Block nodeBlock = level.getBlockState(nodeBlockPos).getBlock();
				Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, directionToNeighbor, null);
				level.neighborChanged(neighborPos, nodeBlock, orientation);
				if (signalStrength == SignalStrength.STRONG)
				{
					level.updateNeighborsAtExceptFromFacing(neighborPos, nodeBlock, directionToNeighbor.getOpposite(), orientation);
				}
			}
		});
	}

	@Override
	public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries)
	{
		return compound; //noop
	}

	@Override
	public boolean isDirty()
	{
		return false; // never save to disk
	}
}
