package net.commoble.exmachina.api.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.MapCodec;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;

/**
 * Default SignalComponent used for blocks that have no source assigned to them.
 * Permits a given Face in the block to connect to a graph if: if the block's support shape touches the outer side of the blockpos cube
 * <ul>
 * <li>{@link IBlockStateExtension#canRedstoneConnectTo} is true for this block</li>
 * <li>The Face of this block shares the same Direction as the connecting face</li>
 * <li>The connecting block is adjacent to this block</li>
 * <li>This block's support shape touches the outer side of the blockpos cube</li>
 * <li>and this block's support shape on the touching face is at least 2x2 sixteenths on the center edge:</li>
 * </ul>
<pre> 
/----------------\
|       xx       |
|       xx       |
|                |
|                |
|                |
|                |
|                |
|xx            xx|
|xx            xx|
|                |
|                |
|                |
|                |
|                |
|       xx       |
|       xx       |
\----------------/
</pre>
<pre>
{
	"type": "exmachina:default"
}
</pre>
 */
public enum DefaultSignalComponent implements SignalComponent
{
	/** Singleton instance of DefaultSource */
	INSTANCE;
	
	/** exmachina:signal_source_type / exmachina:default */
	public static final ResourceKey<MapCodec<? extends SignalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, ExMachina.id("default"));
	
	/** <pre>{"type": "exmachina:default"}</pre> */
	public static final MapCodec<DefaultSignalComponent> CODEC = MapCodec.unit(INSTANCE);


	/**
	 * Creates and returns an array of six voxelshapes for the wire nodes in dunswe order
	 * @param xzRadius The radius of the node shape on the axes parallel to the attachment face
	 * @param height The height of the node shape perpendicular to the attachment face
	 * @return An array of six voxelshapes in dunswe order, where the ordinal of an attachment face's direction is the respective index
	 */
	private static VoxelShape[] makeNodeShapes(int xzRadius, int height)
	{
		int min = 0;
		int max = 16;
		int minPlusHeight = min + height;
		int maxMinusHeight = max - height;
		int minWidth = 8 - xzRadius;
		int maxWidth = 8 + xzRadius;
		return new VoxelShape[]
		{
			Block.box(minWidth, min, minWidth, maxWidth, minPlusHeight, maxWidth),
			Block.box(minWidth, maxMinusHeight, minWidth, maxWidth, max, maxWidth),
			Block.box(minWidth, minWidth, min, maxWidth, maxWidth, minPlusHeight),
			Block.box(minWidth, minWidth, maxMinusHeight, maxWidth, maxWidth, max),
			Block.box(min, minWidth, minWidth, minPlusHeight, maxWidth, maxWidth),
			Block.box(maxMinusHeight, minWidth, minWidth, max, maxWidth, maxWidth)
		};
	}
	private static final VoxelShape[] SMALL_NODE_SHAPES = makeNodeShapes(1,2);

	@Override
	public MapCodec<? extends SignalComponent> codec()
	{
		return CODEC;
	}	

	@Override
	public Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel)
	{
		List<TransmissionNode> nodes = new ArrayList<>();
		if (channel != Channel.redstone())
			return nodes;
		
		for (Direction directionToNeighbor : Direction.values())
		{
			Direction directionFromNeighbor = directionToNeighbor.getOpposite();
			if (!state.canRedstoneConnectTo(level, pos, directionFromNeighbor))
				continue;
			BlockPos neighborPos = pos.relative(directionToNeighbor);
			for (Direction faceSide : Direction.values())
			{
				if (faceSide == directionToNeighbor || faceSide == directionFromNeighbor)
					continue;

				VoxelShape wireTestShape = SMALL_NODE_SHAPES[faceSide.ordinal()];
				VoxelShape transmitterShape = state.getBlockSupportShape(level, pos);
				VoxelShape projectedShape = transmitterShape.getFaceShape(directionToNeighbor);
				boolean canConnect = !Shapes.joinIsNotEmpty(projectedShape, wireTestShape, BooleanOp.ONLY_SECOND);
				if (canConnect)
				{
					nodes.add(new TransmissionNode(
						NodeShape.ofSideSide(faceSide, directionToNeighbor),
						reader -> reader.getSignal(pos, directionFromNeighbor),
						Set.of(),
						Set.of(new SignalGraphKey(levelKey, neighborPos, NodeShape.ofSideSide(faceSide, directionFromNeighbor), Channel.redstone())),
						(levelAccess, power) -> Map.of()
					));
				}
			}
		}
		
		return nodes;
	}

	// needed because this can include delayed output blocks such as repeaters
	@Override
	public boolean updateSelfFromNeighborsAfterGraphUpdate(LevelReader level, BlockState state, BlockPos pos)
	{
		return true;
	}	
}
