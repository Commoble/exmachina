package com.github.commoble.exmachina.api.electrical;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.content.block.CategoriesOfBlocks;
import com.github.commoble.exmachina.content.block.IElectricalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Representation of a "node" in an electrical circuit,
 * where a "node" represents the wires between two or more electrical components.
 * (Note that this differs from a "node" in a traditional Graph, where the "node" would
 * refer to what we are referring to as "components")
 * 
 * For reference, there are four kinds of Nodes referred to throughout this project:
 * 
 * -regular/standard/normal node: contains at least one wire block (and usually at least one component block)
 * -virtual node: contains two component blocks and no wire blocks
 * -dead node: contains one component block and no wire blocks
 * -the Empty Node: contains no component blocks and no wire blocks (use this instead of null)
 */
public class Node
{
	/** Use this instead of null **/
	public static final Node EMPTY_NODE = new Node().setID(Integer.MIN_VALUE);
	
	
	/** The identifier for this node, for the purpose of solving the circuit containing it (-1 = the ground node) **/
	public int identifier;
	
	/**
	 * The set of all block positions that represent wires in the node
	 */
	public HashSet<BlockPos> wireBlocks;
	
	/**
	 * The set of all components that this node connects
	 */
	public HashSet<BlockPos> connectedComponents;
	
	public Node()
	{
		this.wireBlocks = new HashSet<BlockPos>();
		this.connectedComponents = new HashSet<BlockPos>();
	}

	@Nonnull
	public Node setID(int id)
	{
		this.identifier = id;
		return this;
	}
	
	/**
	 * A "virtual node" in this context is a node between two adjacent components
	 */
	@Nonnull
	public static Node createVirtualNode(BlockPos component1, BlockPos component2)
	{
		Node node = new Node();
		node.connectedComponents.add(component1);
		node.connectedComponents.add(component2);
		return node;
	}
	
	/*
	 * A virtual node with only one component
	 */
	@Nonnull
	public static Node createDeadNode(BlockPos component)
	{
		Node node = new Node();
		node.connectedComponents.add(component);
		return node;
	}
	
	/**
	 * Returns true if these nodes represent the same sets of blocks
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof Node)
		{
			if (o.hashCode() != this.hashCode())
			{
				return false;
			}
			Node otherNode = (Node)o;
			return (this.wireBlocks.equals(otherNode.wireBlocks) && this.connectedComponents.equals(otherNode.connectedComponents));
		}
		else
		{
			return false;
		}
		
	}
	
	@Override
	public int hashCode()
	{
		return this.wireBlocks.hashCode() * this.connectedComponents.hashCode();
	}
	
	/**
	 * Returns true if the Node contains the specified position in any of its sets
	 */
	public boolean contains(BlockPos pos)
	{
		return this.wireBlocks.contains(pos) || this.connectedComponents.contains(pos);
	}
	
	@Nonnull
	/**
	 * Given a block position, build a Node based on the following:
	 * -if startPos is a wire pos, build a Node recursively from that wire
	 * -if startPos is a component pos, return a dead node containing that component
	 * -if startPos is not electrical, return NULL
	 */
	public static Node buildNodeFrom(World world, BlockPos startPos)//BlockPos firstComponentPos, BlockPos firstWirePos)
	{
		Node node = new Node();
		Block startBlock = world.getBlockState(startPos).getBlock();
		if (CategoriesOfBlocks.wireBlocks.contains(startBlock))
		{
			return Node.recursivelyBuildNodeFrom(world, node, startPos);
		}
		else if (CategoriesOfBlocks.isAnyComponentBlock(startBlock))
		{
			return Node.createDeadNode(startPos);
		}
		else
		{
			return Node.EMPTY_NODE;
		}
	}
	
	@Nonnull
	private static Node recursivelyBuildNodeFrom(World world, @Nonnull Node node, BlockPos startPos)// BlockPos checkPos, BlockPos prevPos)
	{
		// if node already contains this position, ignore and return
		BlockState startState = world.getBlockState(startPos);
		Block startBlock = startState.getBlock();
		
		if ((!(startBlock instanceof IElectricalBlock)) || node.contains(startPos))
		{
			return node;
		}
		
		// if this is a wire block, continue recursively building the node from this position
		if (CategoriesOfBlocks.wireBlocks.contains(startBlock))
		{
			node.wireBlocks.add(startPos);
			Set<Direction> facesToCheck = ((IElectricalBlock)startBlock).getConnectingFaces(world, startState, startPos);
			
			for(Direction face : facesToCheck)
			{
				BlockPos nextCheck = startPos.offset(face);
				if (!node.contains(nextCheck) && CircuitHelper.doTwoBlocksConnect(world, startPos, nextCheck))
				{
					node = recursivelyBuildNodeFrom(world, node, nextCheck);
				}
			}
			
			return node;
		}
		// if this is a component block, add it to the node but do not continue recursively building
		else if (CategoriesOfBlocks.isAnyComponentBlock(startBlock))
		{
			node.connectedComponents.add(startPos);
			return node;
		}

		return node;
	}
}