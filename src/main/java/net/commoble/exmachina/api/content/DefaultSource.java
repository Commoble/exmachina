package net.commoble.exmachina.api.content;

import java.util.Map;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.Face;
import net.commoble.exmachina.api.SignalSource;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;

/**
 * Default SignalSource used for blocks that have no source assigned to them.
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
public enum DefaultSource implements SignalSource
{
	/** Singleton instance of DefaultSource */
	INSTANCE;
	
	/** exmachina:signal_source_type / exmachina:default */
	public static final ResourceKey<MapCodec<? extends SignalSource>> KEY = ResourceKey.create(ExMachinaRegistries.SIGNAL_SOURCE_TYPE, ExMachina.id("default"));
	
	/** <pre>{"type": "exmachina:default"}</pre> */
	public static final MapCodec<DefaultSource> CODEC = MapCodec.unit(INSTANCE);


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
	public MapCodec<? extends SignalSource> codec()
	{
		return CODEC;
	}

	@Override
	public Map<Channel, ToIntFunction<LevelReader>> getSupplierEndpoints(BlockGetter level, BlockPos supplierPos, BlockState supplierState, Direction supplierSide, Face connectedFace)
	{
		// we allow wires to connect to vanilla power emitters by default if the block is redstone-connectable and has a connectable voxelshape
		BlockPos wirePos = connectedFace.pos();
		BlockPos offsetFromNeighbor = supplierPos.subtract(wirePos);
		@Nullable Direction directionFromNeighbor = Direction.fromDelta(offsetFromNeighbor.getX(), offsetFromNeighbor.getY(), offsetFromNeighbor.getZ()); 
		if (!supplierState.canRedstoneConnectTo(level, wirePos, directionFromNeighbor))
			return Map.of();
		Direction directionToWire = directionFromNeighbor.getOpposite();
		VoxelShape wireTestShape = SMALL_NODE_SHAPES[connectedFace.attachmentSide().ordinal()];
		VoxelShape neighborShape = supplierState.getBlockSupportShape(level, supplierPos);
		VoxelShape projectedNeighborShape = neighborShape.getFaceShape(directionToWire);
		// if the projected neighbor shape entirely overlaps the line shape,
		// then the neighbor shape can be connected to by the wire
		// we can test this by doing an ONLY_SECOND comparison on the shapes
		// if this returns true, then there are places where the second shape is not overlapped by the first
		// so if this returns false, then we can proceed
		return Shapes.joinIsNotEmpty(projectedNeighborShape, wireTestShape, BooleanOp.ONLY_SECOND)
			? Map.of()
			: Map.of(Channel.redstone(), reader -> reader.getSignal(supplierPos, directionFromNeighbor));
	}
}
