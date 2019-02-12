package com.github.commoble.exmachina.common.electrical;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.commoble.exmachina.common.block.CategoriesOfBlocks;
import com.github.commoble.exmachina.common.block.IElectricalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Representation of a "node" in an electrical circuit,
 * where a "node" represents the wires between two or more electrical components.
 * (Note that this differs from a "node" in a traditional Graph, where the "node" would
 * refer to what we are referring to as "components")
 */
public class Node
{
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
	
	/**
	 * A "virtual node" in this context is a node between two adjacent components
	 */
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
	
	@Nullable
	public static Node buildNodeFrom(World world, BlockPos firstComponentPos, BlockPos firstWirePos)
	{
		Node node = new Node();
		node.connectedComponents.add(firstComponentPos);
		Block firstBlockInNode = world.getBlockState(firstWirePos).getBlock();
		
		if (!(firstBlockInNode instanceof IElectricalBlock))
		{
			System.out.println("Wire block at " + firstWirePos.toString() + " not marked electrical, returning null node");
			return null;
		}
		
		if (CategoriesOfBlocks.wireBlocks.contains(firstBlockInNode))
		{	// Node contains at least one wire, find the volume of the node and the components that touch it
			node = recursivelyBuildNodeFrom(world, node, firstWirePos, firstComponentPos);
			return node;
		}
		else if (CategoriesOfBlocks.passiveComponentBlocks.contains(firstBlockInNode))
		{	// The "Node" contains no physical blocks, it just connects two adjacent components
			node.connectedComponents.add(firstWirePos);
			return node;
		}
		else if (CategoriesOfBlocks.activeComponentBlocks.contains(firstBlockInNode))
		{	// The "Node" contains no physical blocks, it just connects two adjacent components
			node.connectedComponents.add(firstWirePos);
			return node;
		}
		else
		{
			System.out.println("Node block at " + firstWirePos.toString() + " not marked as any block category, returning null node");
			return null;
		}
	}
	
	private static Node recursivelyBuildNodeFrom(World world, Node node, BlockPos checkPos, BlockPos prevPos)
	{
		// if node already contains this position, ignore and return
		IBlockState checkState = world.getBlockState(checkPos);
		Block checkBlock = checkState.getBlock();
		
		if ((!(checkBlock instanceof IElectricalBlock)) || node.contains(checkPos))
		{
			return node;
		}
		
		if (CategoriesOfBlocks.wireBlocks.contains(checkBlock))
		{
			node.wireBlocks.add(checkPos);
			Set<EnumFacing> facesToCheck = ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, checkPos);
			
			for(EnumFacing face : facesToCheck)
			{
				BlockPos nextCheck = checkPos.offset(face);
				if (!nextCheck.equals(prevPos))
				{
					node = recursivelyBuildNodeFrom(world, node, nextCheck, checkPos);
				}
			}
			
			return node;
		}
		else if (CategoriesOfBlocks.passiveComponentBlocks.contains(checkBlock))
		{
			Set<EnumFacing> facesToCheck = ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, checkPos);
			
			for(EnumFacing face : facesToCheck)
			{
				BlockPos nextCheck = checkPos.offset(face);
				if (!nextCheck.equals(prevPos)
						&& world.getBlockState(nextCheck).getBlock() instanceof IElectricalBlock)
				{
					node.connectedComponents.add(checkPos);

					return node;
				}
			}
		}
		else if (CategoriesOfBlocks.activeComponentBlocks.contains(checkBlock))
		{
			Set<EnumFacing> facesToCheck = ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, checkPos);
			
			for(EnumFacing face : facesToCheck)
			{
				BlockPos nextCheck = checkPos.offset(face);
				if (!nextCheck.equals(prevPos)
						&& world.getBlockState(nextCheck).getBlock() instanceof IElectricalBlock)
				{
					node.connectedComponents.add(checkPos);

					return node;
				}
			}
		}

		return node;
	}
}