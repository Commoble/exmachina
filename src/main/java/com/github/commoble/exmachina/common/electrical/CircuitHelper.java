package com.github.commoble.exmachina.common.electrical;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.exmachina.common.block.CategoriesOfBlocks;
import com.github.commoble.exmachina.common.block.IElectricalBlock;
import com.github.commoble.exmachina.common.tileentity.ICircuitElementHolderTE;
import com.github.commoble.exmachina.common.util.BlockPosWithDist;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CircuitHelper
{
	public static boolean isCompleteCircuit(World world, BlockPos startPos)
	{
		// cycle detection in graphs:
		// for every visited node V, if there is an adjacent U such that U is already visited and U is not parent of V,
		// then there is a cycle in the graph
		// additionally, we are only interested if the starting node is part of a cycle
		// so we are only interested if this U is also the starting node
		
		// so check each node in a depth-first search, keeping track of
		//	-all previously visited nodes
		//	-the immediately-previously visited node
		//	-the first node to have been visited
		// check all adjacent nodes
		//	-if node is invalid (not electrical) ignore
		//	-if node has not been visited, visit it
		//	-otherwise, if previously-visited node is not immediately previously visited node AND is original node,
			//	then complete circuit has been found; otherwise ignore it
		// also the first block must be electrical

		HashSet<BlockPos> traversed = new HashSet<BlockPos>();
		
		return isCompleteCircuit(world, startPos, startPos, startPos, traversed);
	}
	
	private static boolean isCompleteCircuit(World world, BlockPos checkPos, BlockPos prevPos, BlockPos firstPos, HashSet<BlockPos> traversed)
	{
		// add to traversed even if not electrical so it still won't be checked twice
		// doing it this way, however, means that firstpos must be electrical for it the implementation to work
		traversed.add(checkPos);
		IBlockState checkState = world.getBlockState(checkPos);
		Block checkBlock = checkState.getBlock();
		if (checkBlock instanceof IElectricalBlock)
		{
			for(EnumFacing face : ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, checkPos))
			{
				BlockPos nextCheck = checkPos.offset(face);

				if (!traversed.contains(nextCheck))
				{
					boolean found = isCompleteCircuit(world, nextCheck, checkPos, firstPos, traversed);
					if (found) return true; 
				}
				else	// has been visited
				{
					if (!nextCheck.equals(prevPos) && nextCheck.equals(firstPos))
					{
						return true;
					}
				}
			}
			
			return false;
		}
		else // not electrical block
		{
			return false;
		}
	}

	/**
	 * Gathers data to build a Circuit data structure, starting from a voltage source.
	 * Precondition: The three positions given represent electrical blocks and are part of a complete circuit 
	 * @param world The world
	 * @param sourcePos The voltage source's position
	 * @param startPos The positive-side position adjacent to the source
	 * @param endPos The negative-side position adjacent to the source
	 * @return
	 */
	@Nullable
	public static Circuit buildCircuit(World world, BlockPos sourcePos, BlockPos startPos, BlockPos endPos)
	{
		Node groundNode = Node.buildNodeFrom(world, sourcePos, endPos);
		if (groundNode == null)
		{
			world.setBlockToAir(sourcePos);
			world.createExplosion(null, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(), 1F, false);
			System.out.println("Ground node returned null, exploding");
			return null;
		}
		Circuit circuit = Circuit.buildCircuitFromGround(world, groundNode);
		if (circuit == null)
		{
			System.out.println("Circuit returned null, exploding");
			world.setBlockToAir(sourcePos);
			world.createExplosion(null, sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(), 1F, false);
			return null;
		}
		
		circuit.printToConsole(world);
		return circuit;
	}
	
	public static boolean isWireBlock(World world, BlockPos pos)
	{
		return CategoriesOfBlocks.wireBlocks.contains(world.getBlockState(pos).getBlock());
	}
	
	/**
	 * Called when a circuit needs to be marked as needing to be updated after a change to a wire block.
	 * The wire block does not have access to the circuit, so it must find the nearest ICircuitElementHolderTE,
	 * and use that to nullify the circuit
	 * @param circuit
	 * @param pos
	 */
	public static void updateCircuit(World world, BlockPos pos)
	{
		HashSet<BlockPos> traversed = new HashSet<BlockPos>();
		traversed.add(pos);
	}
	
	/** Gets the nearest CircuitElement to a given wire block position
	 * following along IElectricalBlocks
	 * returns Null if none is found
	 */
	@Nullable
	public static CircuitElement getNearestCircuitElement(World world, BlockPos startPos)
	{
		// current implementation is based on Djikstra's algorithm
		// 
		HashMap<BlockPos, Integer> dists = new HashMap<BlockPos, Integer>();	// known distance from position to start
		//HashMap<BlockPos, BlockPos> prevs = new HashMap<BlockPos, BlockPos>();	// pos -> previous pos in search
		CircuitElement nearestCircuitElement = null;

		// the head of this queue is the "lowest" member of the queue
		// in this case, it will be the blockpos with the lowest/shortest distance to the start
		PriorityQueue<BlockPosWithDist> pq = new PriorityQueue<BlockPosWithDist>();
		pq.add(new BlockPosWithDist(0, startPos));
		dists.put(startPos, 0);
		ICircuitElementHolderTE nearestRelevantTE = null;
		int nrtedist = Integer.MAX_VALUE;
		
		while(!pq.isEmpty())
		{
			BlockPosWithDist closest = pq.poll();// won't be null because PQ is not empty
			if (nearestRelevantTE == null || closest.dist < nrtedist)
			{
				IBlockState closestState = world.getBlockState(closest.pos);
				Block closestBlock = closestState.getBlock();
				
				TileEntity te = world.getTileEntity(closest.pos);
				if (te instanceof ICircuitElementHolderTE)
				{
					nearestRelevantTE = (ICircuitElementHolderTE)te;
					nrtedist = closest.dist;
				}
			}
			// get all adjacent connected blocks
			EnumSet<EnumFacing> checkFaces = CircuitHelper.getBiConnectedElectricalBlocks(world, closest.pos);
			for (EnumFacing face : checkFaces)
			{
				BlockPos nextPos = closest.pos.offset(face);
				if (!dists.containsKey(nextPos) || dists.get(nextPos) > closest.dist + 1)	// TODO replace with wire resist
				{
					dists.put(nextPos, closest.dist+1);
					//prevs.put(nextPos, closest.pos);
					pq.add(new BlockPosWithDist(closest.dist+1, nextPos));
				}
			}
		}
		
		if (nearestRelevantTE == null)
		{
			return null;
		}
		else
		{
			return nearestRelevantTE.getCircuitElement();
		}
	}
	
	/** get a set of the faces that this block connects to that also connect back to this block **/
	@Nonnull
	public static EnumSet<EnumFacing> getBiConnectedElectricalBlocks(World world, BlockPos pos)
	{
		IBlockState checkState = world.getBlockState(pos);
		Block checkBlock = checkState.getBlock();
		EnumSet<EnumFacing> returnFaces = EnumSet.noneOf(EnumFacing.class);
		
		if (checkBlock instanceof IElectricalBlock)
		{
			Set<EnumFacing> checkFaces = ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, pos);
			for (EnumFacing face : checkFaces)
			{
				BlockPos nextPos = pos.offset(face);
				IBlockState nextState = world.getBlockState(nextPos);
				Block nextBlock = nextState.getBlock();
				if (nextBlock instanceof IElectricalBlock)
				{
					Set<EnumFacing> nextBlocksFaces = ((IElectricalBlock)nextBlock).getConnectingFaces(world, nextState, nextPos);
					for (EnumFacing nextFace : nextBlocksFaces)
					{
						if (nextFace.getOpposite() == face)
						{
							returnFaces.add(face);
						}
					}
				}
			}
		}
		return returnFaces;
	}
}
