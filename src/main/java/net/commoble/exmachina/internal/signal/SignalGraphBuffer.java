package net.commoble.exmachina.internal.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaDataMaps;
import net.commoble.exmachina.api.SignalGraphKey;
import net.commoble.exmachina.api.SignalStrength;
import net.commoble.exmachina.api.SignalComponent;
import net.commoble.exmachina.api.StateWirer;
import net.commoble.exmachina.api.TransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
	
	private Map<ResourceKey<Level>, Set<BlockPos>> positions = new HashMap<>();

	/**
	 * {@return SignalGraphBuffer for the overworld}
	 * @param server MinecraftServer to get the buffer for
	 */
	@ApiStatus.Internal
	public static SignalGraphBuffer get(MinecraftServer server)
	{
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
	}

	/**
	 * Enqueues a blockpos to run a signal graph update at at the end of the tick
	 * @param levelKey ResourceKey of the level to enqueue an update at
	 * @param pos BlockPos to enqueue a signal graph update at
	 */
	@ApiStatus.Internal
	public void enqueue(ResourceKey<Level> levelKey, BlockPos pos)
	{
		var levelPositions = this.positions.computeIfAbsent(levelKey, level -> new HashSet<>());
		levelPositions.add(pos);
		for (Direction dir : Direction.values()) {
			levelPositions.add(pos.relative(dir));
		}
	}

	/**
	 * Invoked at the end of each server tick to run enqueued signal graph updates
	 * @param server MinecraftServer being ticked
	 */
	@ApiStatus.Internal
	public void tick(MinecraftServer server)
	{
		if (this.positions.isEmpty())
			return;

		Map<ResourceKey<Level>, Set<BlockPos>> originPositionsByLevel = this.positions;
		this.positions = new HashMap<>();
		
		// construct graph from each origin node
		List<SignalGraph> graphs = new ArrayList<>();
		Map<ServerLevel, Map<BlockPos, StateWirer>> knownWirers = new HashMap<>();
		Map<SignalGraphKey, TransmissionNode> originNodes = new HashMap<>();
		
		for (var entry : originPositionsByLevel.entrySet())
		{
			var levelKey = entry.getKey();
			ServerLevel originLevel = server.getLevel(levelKey);
			if (originLevel == null)
			{
				continue;
			}
			var originPositions = entry.getValue();
			for (BlockPos originPos : originPositions)
			{
				// collect all of the nodes in this block
				BlockState originState = originLevel.getBlockState(originPos);
				Block originBlock = originState.getBlock();
				@SuppressWarnings("deprecation")
				SignalComponent originTransmitter = BuiltInRegistries.BLOCK.getData(ExMachinaDataMaps.SIGNAL_COMPONENT, originBlock.builtInRegistryHolder().key());
				if (originTransmitter != null)
				{
					for (Channel channel : Channel.ALL)
					{
						originTransmitter.getTransmissionNodes(levelKey, originLevel, originPos, originState, channel).forEach(node -> {
							originNodes.put(new SignalGraphKey(originLevel.dimension(), originPos, node.shape(), channel), node);
						});
					}
				}
			}
		}
		originNodes.forEach((key, transmissionNode) -> {
			ServerLevel originLevel = server.getLevel(key.levelKey());
			if (originLevel == null)
			{
				return;
			}
			// as we construct graphs, ignore origin nodes that exist in existing graphs
			for (SignalGraph priorGraph : graphs)
			{
				if (priorGraph.hasKey(key))
				{
					return;
				}
			}
			
			SignalGraph graph = SignalGraph.fromOriginNode(originLevel, key, transmissionNode, knownWirers);
			graphs.add(graph);
		});
		
		// each graph has calculated the highest input power into the graph
		
		// inform all listeners in all graphs of the new power
		// keep track of neighbors to update
		// how do neighbor updates work?
		// each time we proc a listener on a transition node at nodepos, it gives us a set of directions to proc neighbor updates in
		// we store these as a face (nodePos+direction)
		Map<ResourceKey<Level>, Map<BlockPos, Map<Direction, SignalStrength>>> nodesUpdatingNeighbors = new HashMap<>();
		Map<ServerLevel, Set<BlockPos>> nodesUpdatingSelf = new IdentityHashMap<>();
		for (SignalGraph graph : graphs)
		{
			graph.updateListeners(server).forEach((levelKey,byPos) -> {
				var toUpdateByPos = nodesUpdatingNeighbors.computeIfAbsent(levelKey, $ -> new HashMap<>());
				byPos.forEach((pos, byDirection) -> {
					var toUpdateByDirection = toUpdateByPos.computeIfAbsent(pos, $ -> new HashMap<>());
					byDirection.forEach((direction, signalStrength) -> toUpdateByDirection.merge(direction, signalStrength, SignalStrength::max));
				});
			});
			graph.nodesUpdatingSelf().forEach((level, positions) -> {
				nodesUpdatingSelf.computeIfAbsent(level, $ -> new HashSet<>()).addAll(positions);
			});
		}
		
		// invoke block updates on blocks which are adjacent to the graph but have no transmission nodes within it
		nodesUpdatingNeighbors.forEach((levelKey, posToDirections) -> {
			ServerLevel targetLevel = server.getLevel(levelKey);
			if (targetLevel == null)
				return;
			posToDirections.forEach((nodeBlockPos, directionToSignalStrength) -> {
				directionToSignalStrength.forEach((directionToNeighbor, signalStrength) -> {
					BlockPos neighborPos = nodeBlockPos.relative(directionToNeighbor);
					if (!SignalGraph.isBlockInAnyGraph(targetLevel, neighborPos, graphs))
					{
						Block nodeBlock = targetLevel.getBlockState(nodeBlockPos).getBlock();
						Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(targetLevel, directionToNeighbor, null);
						targetLevel.neighborChanged(neighborPos, nodeBlock, orientation);
						if (signalStrength == SignalStrength.STRONG)
						{
							targetLevel.updateNeighborsAtExceptFromFacing(neighborPos, nodeBlock, directionToNeighbor.getOpposite(), orientation);
						}
					}
				});
			});
		});
		
		nodesUpdatingSelf.forEach((level, positions) -> {
			for (BlockPos pos : positions)
			{
				level.neighborChanged(pos, Blocks.AIR, null); // redstone seems to ignore updates from block==redstone
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
