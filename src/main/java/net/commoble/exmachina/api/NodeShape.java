package net.commoble.exmachina.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Representation of the volume or connectibility of a graph node
 */
public sealed interface NodeShape permits NodeShape.Cube, NodeShape.Side, NodeShape.SideSide
{
	/**
	 * Three valid formats:
	 * <pre>
	 * {
	 *   "someCubeNode": "cube",
	 *   "someSideNode": "north", // any direction
	 *   "someSideSideNode": {"face": "north", "side": "down"} // any two directions
	 * }
	 * </pre>
	 */
	public static final Codec<NodeShape> CODEC = Codec.either(
			Codec.either(
				SideSide.CODEC,
				Side.CODEC),
			Cube.CODEC)
		.xmap(
			eitherEither -> eitherEither.map(
				eitherSide -> eitherSide.map(sideSide -> sideSide, side -> side),
				cube -> cube),
			shape -> switch(shape) {
				case Cube cube -> Either.right(cube);
				case Side side -> Either.left(Either.right(side));
				case SideSide sideSide -> Either.left(Either.left(sideSide));
			});
	
	/** codec but stream **/
	public static final StreamCodec<ByteBuf, NodeShape> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
			
	/**
	 * {@return NodeShape which encompasses an entire BlockPos}
	 */
	public static NodeShape ofCube()
	{
		return Cube.CUBE;
	}
	
	/**
	 * {@return NodeShape which represents a node on the internal face of a blockpos}
	 * @param directionToNeighbor Direction from the node's BlockPos to its neighbor
	 */
	public static NodeShape ofSide(Direction directionToNeighbor)
	{
		return new Side(directionToNeighbor);
	}
	
	/**
	 * {@return NodeShape which represents a node which lies on the internal face of a BlockPos but on touches one of its four orthagonal neighbors}
	 * @param directionToNeighbor Direction from the center of the blockpos to the face on which the node lies
	 * @param secondaryDirection Direction from the center of the primary face to the neighbor which can connect to that node
	 */
	public static NodeShape ofSideSide(Direction directionToNeighbor, Direction secondaryDirection)
	{
		return new SideSide(directionToNeighbor, secondaryDirection);
	}
	
	/**
	 * {@return true if this targeted NodeShape is usable when a preferred node is requested for a connection}
	 * @param preferredNode NodeShape which another node is trying to form a connection to
	 */
	public boolean isValidFor(NodeShape preferredNode);
	
	/**
	 * NodeShape which encompasses an entire BlockPos
	 */
	public static enum Cube implements NodeShape
	{
		/** the Cube */
		CUBE;
		
		/**
		 * <pre>
		 * "shape": "cube"
		 * </pre>
		 */
		public static final Codec<Cube> CODEC = Codec.STRING.comapFlatMap(
			s -> s.equals("cube") ? DataResult.success(CUBE) : DataResult.error(() -> "string shape must be \"cube\" or a direction such as \"north\""),
			cube -> "cube");

		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return true;
		}
	}
	
	/**
	 * NodeShape which represents a node on the internal face of a blockpos
	 * @param face Direction from the node's BlockPos to its neighbor
	 */
	public static record Side(Direction face) implements NodeShape
	{
		/**
		 * <pre>
		 * "shape": "north"
		 * </pre>
		 */
		public static final Codec<Side> CODEC = Direction.CODEC.xmap(Side::new, Side::face);

		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return switch(otherNode)
			{
				case Cube thatCube -> true;
				case Side thatSide -> this.face == thatSide.face;
				case SideSide thatSideSide -> this.face == thatSideSide.face;
			};
		}
	}

	
	/**
	 * NodeShape which represents a node which lies on the internal face of a BlockPos but on touches one of its four orthagonal neighbors
	 * @param face Direction from the center of the blockpos to the face on which the node lies
	 * @param side Direction from the center of the primary face to the neighbor in parallel with that face
	 */
	public static record SideSide(Direction face, Direction side) implements NodeShape
	{
		/**
		 * <pre>
		 * "shape": {"face": "north", "side": "up"}
		 * </pre>
		 */
		public static final Codec<SideSide> CODEC = RecordCodecBuilder.create(builder -> builder.group(
					Direction.CODEC.fieldOf("face").forGetter(SideSide::face),
					Direction.CODEC.fieldOf("side").forGetter(SideSide::side)
				).apply(builder, SideSide::new));
		
		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return switch(otherNode)
			{
				case Cube thatCube -> true;
				case Side thatSide -> this.face == thatSide.face;
				case SideSide thatSideSide -> this.equals(thatSideSide);
			};
		}
	}
}
