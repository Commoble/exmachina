package net.commoble.exmachina.api;

import net.minecraft.core.Direction;

/**
 * Representation of the volume or connectibility of a graph node
 */
public sealed interface NodeShape permits NodeShape.Cube, NodeShape.Side, NodeShape.SideSide
{
	/**
	 * {@return NodeShape which encompasses an entire BlockPos}
	 */
	public static NodeShape ofCube()
	{
		return Cube.INSTANCE;
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
		INSTANCE;

		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return true;
		}
	}
	
	/**
	 * NodeShape which represents a node on the internal face of a blockpos
	 * @param directionToNeighbor Direction from the node's BlockPos to its neighbor
	 */
	public static record Side(Direction directionToNeighbor) implements NodeShape
	{
		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return switch(otherNode)
			{
				case Cube thatCube -> true;
				case Side thatSide -> this.directionToNeighbor == thatSide.directionToNeighbor;
				case SideSide thatSideSide -> this.directionToNeighbor == thatSideSide.directionToNeighbor;
			};
		}
	}

	
	/**
	 * NodeShape which represents a node which lies on the internal face of a BlockPos but on touches one of its four orthagonal neighbors
	 * @param directionToNeighbor Direction from the center of the blockpos to the face on which the node lies
	 * @param secondaryDirection Direction from the center of the primary face to the neighbor which can connect to that node
	 */
	public static record SideSide(Direction directionToNeighbor, Direction secondaryDirection) implements NodeShape
	{
		@Override
		public boolean isValidFor(NodeShape otherNode)
		{
			return switch(otherNode)
			{
				case Cube thatCube -> true;
				case Side thatSide -> this.directionToNeighbor == thatSide.directionToNeighbor;
				case SideSide thatSideSide -> this.equals(thatSideSide);
			};
		}
	}
}
