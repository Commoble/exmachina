package net.commoble.exmachina.internal.mechanical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.ApiStatus;

import net.commoble.exmachina.api.MechanicalGraphKey;
import net.commoble.exmachina.api.MechanicalNode;
import net.commoble.exmachina.internal.mechanical.MechanicalGraph.GearRatio;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Buffer in which mechanical graph updates in serverlevels are enqueued.
 * All enqueued updates run at the end of each server tick. 
 */
public final class MechanicalGraphBuffer extends SavedData
{
	private static final String ID = "exmachina/mechanicalgraphbuffer";
	private static final SavedData.Factory<MechanicalGraphBuffer> FACTORY = new SavedData.Factory<MechanicalGraphBuffer>(MechanicalGraphBuffer::new, MechanicalGraphBuffer::load);
	
	private Map<ResourceKey<Level>, Set<BlockPos>> positions = new HashMap<>();
	
	private MechanicalGraphBuffer() {}
	private static MechanicalGraphBuffer load(CompoundTag tag, HolderLookup.Provider registries)
	{
		return new MechanicalGraphBuffer();
	}
	
	/**
	 * {@return MechanicalGraphBuffer for the overworld}
	 * @param server MinecraftServer to get the buffer for
	 */
	@ApiStatus.Internal
	public static MechanicalGraphBuffer get(MinecraftServer server)
	{
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
	}
	
	/**
	 * Enqueues a blockpos to run a mechanical graph update at at the end of the tick
	 * @param levelKey ResourceKey of the level to enqueue an update at
	 * @param pos BlockPos to enqueue a mechanical graph update at
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
	 * Invoked at the end of each server tick to proc mechanical graph updates
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
		List<MechanicalGraph> graphs = new ArrayList<>();
		Map<BlockGetter, Map<BlockPos, MechanicalBlockState>> knownComponents = new HashMap<>();
		Function<BlockGetter, Function<BlockPos, MechanicalBlockState>> componentLookup = targetLevel -> targetPos -> knownComponents
			.computeIfAbsent(targetLevel, key -> new HashMap<>())
			.computeIfAbsent(targetPos, pos -> MechanicalBlockState.getOrDefault(targetLevel, pos, server.registryAccess()));
		Map<MechanicalGraphKey, MechanicalNode> originNodes = new HashMap<>();
		Set<MechanicalGraphKey> existingKeysInGraphs = new HashSet<>();
		Map<Fraction, GearRatio> gearCache = new HashMap<>();
		
		for (var entry : originPositionsByLevel.entrySet())
		{
			var levelKey = entry.getKey();
			ServerLevel originLevel = server.getLevel(levelKey);
			if (originLevel == null)
				continue;
			var originPositions = entry.getValue();
			for (BlockPos originPos : originPositions)
			{
				// collect all nodes in block
				MechanicalBlockState originState = knownComponents
					.computeIfAbsent(originLevel, $ -> new HashMap<>())
					.computeIfAbsent(originPos, p -> MechanicalBlockState.getOrDefault(originLevel, p, server.registryAccess())); 
				for (MechanicalNode node : originState.component().getNodes(levelKey, originLevel, originPos))
				{
					originNodes.put(new MechanicalGraphKey(originLevel.dimension(), originPos, node.shape()), node);
				}
			}
		}
		originNodes.forEach((key, node) -> {
			ServerLevel originLevel = server.getLevel(key.levelKey());
			if (originLevel == null)
				return;
			// ignore origin nodes that have already been found in other graphs
			if (existingKeysInGraphs.contains(key))
				return;
			MechanicalGraph graph = MechanicalGraph.fromOriginNode(originLevel, key, node, server::getLevel, componentLookup, gearCache);
			graphs.add(graph);
			existingKeysInGraphs.addAll(graph.nodesInGraph().keySet());
		});
		// each graph has calculated angular velocities for all nodes
		// power can be calculated for each node, run update listeners
		for (MechanicalGraph graph : graphs)
		{
			graph.updateListeners(gearCache);
		}
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
