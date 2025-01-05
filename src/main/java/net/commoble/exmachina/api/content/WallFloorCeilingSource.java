package net.commoble.exmachina.api.content;

import java.util.Map;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.Node;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.SignalSource;
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
 * SignalSource intended for {@link FaceAttachedHorizontalDirectionalBlock}s such as buttons and levers.
 * Blocks with this source type can connect to graph nodes which are adjacent to this block and are attached to the same side.
 * 
<pre>
{
	"type": "exmachina:wall_floor_ceiling",
	"invert": true
}
</pre>
 * @param invertHorizontalFacing if true, the Face of the connecting node must be the opposite of
 * this block's facing property to connect (necessary for buttons and levers which "face away" from their attached face)
*/
public record WallFloorCeilingSource(boolean invertHorizontalFacing) implements SignalSource
{
	public static final ResourceKey<MapCodec<? extends SignalSource>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_SOURCE_TYPE, ExMachina.id("wall_floor_ceiling"));
	public static final MapCodec<WallFloorCeilingSource> CODEC = Codec.BOOL.optionalFieldOf("invert_horizontal_facing", false).xmap(WallFloorCeilingSource::new, WallFloorCeilingSource::invertHorizontalFacing);

	@Override
	public MapCodec<? extends SignalSource> codec()
	{
		return CODEC;
	}

	@Override
	public Map<Channel, ToIntFunction<LevelReader>> getSupplierEndpoints(ResourceKey<Level> levelKey, BlockGetter level, BlockPos supplierPos, BlockState supplierState, NodeShape preferredSupplierShape, Node connectingNode)
	{
		// it would be nice if we could just say "if the nodeshapes are compatible, allow the connection"
		// then stuff could remotely grab the power of levers n' things
		// but, we need to check the power output of a specific side of the supplier
		// so we should probably require that the connector is adjacent
		
		if (connectingNode.levelKey() != levelKey)
		{
			return Map.of();
		}
		BlockPos connectingPos = connectingNode.pos();
		@Nullable Direction directionToSupplier = Direction.getNearest(connectingPos.subtract(supplierPos), null);
		Map<Channel, ToIntFunction<LevelReader>> validMap = Map.of(Channel.redstone(), reader -> reader.getSignal(supplierPos, directionToSupplier));
		if (directionToSupplier == null || !supplierPos.relative(directionToSupplier).equals(connectingPos))
		{
			return Map.of();
		}
		// okay, block is adjacent, and we have the power query direction
		// so now we just need to check if the nodeshapes are compatible
		// so we need to figure out which side the lever is attached to
		if (supplierState.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE))
		{
			AttachFace attachFace = supplierState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
			if (attachFace == AttachFace.FLOOR)
			{
				return NodeShape.ofSide(Direction.DOWN).isValidFor(preferredSupplierShape)
					? validMap
					: Map.of();
			}
			else if (attachFace == AttachFace.CEILING)
			{
				return NodeShape.ofSide(Direction.UP).isValidFor(preferredSupplierShape)
					? validMap
					: Map.of();
			}
		}
		if (!supplierState.hasProperty(HorizontalDirectionalBlock.FACING))
			return Map.of();

		Direction facing = supplierState.getValue(HorizontalDirectionalBlock.FACING);
		if (invertHorizontalFacing)
			facing = facing.getOpposite();
		
		return NodeShape.ofSide(facing).isValidFor(preferredSupplierShape)
			? validMap
			: Map.of();
	}
}
