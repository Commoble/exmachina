package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.SignalGraphKey;
import net.commoble.exmachina.api.SignalComponent;
import net.commoble.exmachina.api.TransmissionNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * SignalComponent intended for {@link FaceAttachedHorizontalDirectionalBlock}s such as buttons and levers.
 * Blocks with this source type can connect to graph nodes which are adjacent to this block and are attached to the same side.
 * 
<pre>
{
	"type": "exmachina:wall_floor_ceiling",
	"invert_horizontal_facing": false, // optional boolean; if true, faces away from direction of attachment when attached to walls
	"receives_power": false // optional boolean; if true, will recheck neighbor power after a graph update
}
</pre>
 * @param invertHorizontalFacing if true, the Face of the connecting node must be the opposite of
 * this block's facing property to connect (necessary for buttons and levers which "face away" from their attached face
 * @param receivesPower boolean which indicates neighbor updates should be invoked on this block after a graph update
*/
public record WallFloorCeilingSignalComponent(boolean invertHorizontalFacing, boolean receivesPower) implements SignalComponent
{
	public static final ResourceKey<MapCodec<? extends SignalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, ExMachina.id("wall_floor_ceiling"));
	public static final MapCodec<WallFloorCeilingSignalComponent> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
		Codec.BOOL.optionalFieldOf("invert_horizontal_facing", false).forGetter(WallFloorCeilingSignalComponent::invertHorizontalFacing),
		Codec.BOOL.optionalFieldOf("receives_power", false).forGetter(WallFloorCeilingSignalComponent::receivesPower)
	).apply(builder, WallFloorCeilingSignalComponent::new));

	@Override
	public MapCodec<? extends SignalComponent> codec()
	{
		return CODEC;
	}
	
	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		if (channel != Channel.redstone())
			return List.of();
		
		// so we need to figure out which side the lever is attached to
		if (state.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE))
		{
			AttachFace attachFace = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
			if (attachFace == AttachFace.FLOOR)
			{
				return makeNodes(levelKey, level, pos, state, Direction.DOWN);
			}
			else if (attachFace == AttachFace.CEILING)
			{
				return makeNodes(levelKey, level, pos, state, Direction.UP);
			}
		}
		if (!state.hasProperty(HorizontalDirectionalBlock.FACING))
			return List.of();

		Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
		if (invertHorizontalFacing)
			facing = facing.getOpposite();
		
		return makeNodes(levelKey, level, pos, state, facing);
	}
	
	/**
	 * make nodes for a given attachment side and context
	 * @param levelKey level key
	 * @param level level
	 * @param pos pos
	 * @param state state
	 * @param attachmentSide side
	 * @return nodes
	 */
	public List<TransmissionNode> makeNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Direction attachmentSide)
	{
		List<TransmissionNode> nodes = new ArrayList<>();
		Direction awaySide = attachmentSide.getOpposite();
		
		for (Direction directionToNeighbor : Direction.values())
		{
			if (directionToNeighbor == attachmentSide || directionToNeighbor == awaySide)
				continue;
			Direction directionFromNeighbor = directionToNeighbor.getOpposite();
			nodes.add(new TransmissionNode(
				NodeShape.ofSideSide(attachmentSide, directionToNeighbor),
				reader -> reader.getSignal(pos, directionFromNeighbor),
				Set.of(),
				Set.of(new SignalGraphKey(
					levelKey,
					pos.relative(directionToNeighbor),
					NodeShape.ofSideSide(attachmentSide, directionFromNeighbor),
					Channel.redstone()
				)),
				(levelAccess,power) -> Map.of()));
		}
		
		return nodes;
	}

	@Override
	public boolean updateSelfFromNeighborsAfterGraphUpdate(LevelReader level, BlockState state, BlockPos pos)
	{
		return this.receivesPower;
	}
}
