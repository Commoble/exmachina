package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import net.commoble.exmachina.api.MechanicalConnection;
import net.commoble.exmachina.api.MechanicalGraphKey;
import net.commoble.exmachina.api.MechanicalNode;
import net.commoble.exmachina.api.MechanicalNodeStates;
import net.commoble.exmachina.api.MechanicalState;
import net.commoble.exmachina.api.MechanicalStateComponent;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.content.RawNode.RawConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * MechanicalStateComponent used by {@link VariantsMechanicalComponent} and {@link MultipartMechanicalComponent}
 * to provide mechanical nodes
 * @param save boolean which if true causes mechanical update results to be automatically stored/persisted in {@link MechanicalNodeStates} data attachment
 * @param bakedNodes List of RawNodes provided by this component's blockstate
 */
public record SimpleMechanicalStateComponent(boolean save, List<RawNode> bakedNodes) implements MechanicalStateComponent
{
	private static final BiConsumer<LevelAccessor, MechanicalState> NO_LISTENER = ($,$$) -> {};
	
	@Override
	public Collection<MechanicalNode> getNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos)
	{
		List<MechanicalNode> nodes = new ArrayList<>();
		for (RawNode rawNode : bakedNodes)
		{
			Map<MechanicalGraphKey,MechanicalConnection> connections = new HashMap<>();
			for (RawConnection rawConnection : rawNode.connections())
			{
				connections.put(new MechanicalGraphKey(
					levelKey,
					rawConnection.direction().map(d -> pos.relative(d)).orElse(pos),
					rawConnection.neighborShape()),
					new MechanicalConnection(rawConnection.parity(), rawConnection.teeth()));
			}
			nodes.add(new MechanicalNode(
				rawNode.shape(),
				rawNode.torque(),
				rawNode.positiveCounterTorque(),
				rawNode.negativeCounterTorque(),
				rawNode.inertia(),
				connections,
				makeUpdateListener(save, pos, rawNode.shape())));
		}
		return nodes;
	}
	
	private static BiConsumer<LevelAccessor, MechanicalState> makeUpdateListener(boolean save, BlockPos pos, NodeShape shape)
	{
		return save
			? (levelAccess,update) -> {
				BlockEntity be = levelAccess.getBlockEntity(pos);
				if (be != null)
				{
					var nodeStates = be.getData(MechanicalNodeStates.HOLDER.get());
					nodeStates.put(shape, update);
					be.setChanged();
				}
			}
			: NO_LISTENER;
	}
}