package com.github.commoble.exmachina.common.electrical;

import java.util.EnumSet;
import java.util.HashMap;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CircuitHelper
{
	/**
	 * Gathers data to build a Circuit data structure, starting from a component
	 * Precondition: The three positions given represent electrical blocks and are part of a complete circuit 
	 * @param world The world
	 * @param sourcePos The component's position
	 * @param startPos A connected position adjacent to the component
	 * @param endPos Another connected position adjacent to the component
	 * @return
	 */
	@Nonnull
	public static Circuit buildCircuit(World world, BlockPos sourcePos, BlockPos startPos)
	{
		// we need to find at least one true node
		// once we do that we can expand the circuit naturally from that node
		// we only know that sourcePos is a component, we don't know whether startPos
			// is component, wire, or non-electrical block
		
		// Possibility A) startPos is a wire that connects to sourcePos
		// Possibility B) startPos is a component that connects to sourcePos
		// possibility C) sourcePos is a dead node
		
		// NEED TO CHECK FOR BIDIRECTIONAL CONNECTION
		
		Block startBlock = world.getBlockState(startPos).getBlock();
		Node groundNode;
		if (CircuitHelper.doTwoBlocksConnect(world, sourcePos, startPos))
		{
			if (CategoriesOfBlocks.wireBlocks.contains(startBlock))	// normal node
			{
				groundNode = Node.buildNodeFrom(world, startPos);
			}
			else if (CategoriesOfBlocks.isAnyComponentBlock(startBlock))	// virtual node
			{
				groundNode = Node.createVirtualNode(sourcePos, startPos);
			}
			else
			{
				// shouldn't happen, create a dead node
				System.out.println("Block that was marked as electrical but not a wire or component was found at either " + sourcePos.toString() + " or " + startPos.toString());

				groundNode = Node.createDeadNode(sourcePos);
			}
		}
		else	// dead node
		{
			groundNode = Node.createDeadNode(sourcePos);
		}
		return Circuit.buildCircuitFromGround(world, groundNode);
	}
	
	public static boolean isWireBlock(World world, BlockPos pos)
	{
		return CategoriesOfBlocks.wireBlocks.contains(world.getBlockState(pos).getBlock());
	}
	
	/**
	 * Called when a circuit needs to be marked as needing to be updated after a change to a wire block.
	 * The wire block does not have access to the circuit, so it must find the nearest ICircuitElementHolderTE,
	 * and use that to invalidate the circuit
	 * @param circuit
	 * @param pos
	 */
	public static void updateCircuit(IWorld world, BlockPos pos)
	{
		// quick note on an edge case (not really an edge case since this will be very common)
		// suppose a wire is broken in a manner that divides a circuit into two separate circuits
		// we only need to invalidate one element
		// since each component in either division will have had the same circuit
		// but we do need to notify each component that the circuit needs to be rebuilt
			// (solved: make invalidate ask each component in the circuit to try to rebuild)
		
		// reusing this for now
		// TODO replace with more efficient algorithm
		// we don't NEED to find the "nearest" connected element
		// we just need *a* connected element
		// getNearestCircuitElement examines the entire connected circuit whether it needs to or not
		// which is more work than necessary for what we need here
		CircuitElement nearestElement = getNearestCircuitElement(world, pos);
		if (nearestElement != null)
		{
			BlockPos targetPos = nearestElement.componentPos;
			IBlockState targetState = world.getBlockState(targetPos);
			if (targetState.hasTileEntity())
			{
				TileEntity te = world.getTileEntity(targetPos);
				if (te instanceof ICircuitElementHolderTE)
				{
					((ICircuitElementHolderTE)te).invalidateCircuit();
				}
			}
		}
		
		// if nearestElement is null we don't care about anything
	}
	
	/** Gets the nearest CircuitElement to a given wire block position
	 * following along IElectricalBlocks
	 * returns Null if none is found
	 */
	@Nullable
	public static CircuitElement getNearestCircuitElement(IWorld world, BlockPos startPos)
	{
		// current implementation is based on Djikstra's algorithm
		// 
		HashMap<BlockPos, Integer> dists = new HashMap<BlockPos, Integer>();	// known distance from position to start

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
				TileEntity te = world.getTileEntity(closest.pos);
				if (te instanceof ICircuitElementHolderTE && ((ICircuitElementHolderTE)te).getCircuitElement() != null)
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
	public static EnumSet<EnumFacing> getBiConnectedElectricalBlocks(IWorld world, BlockPos pos)
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
	
	public static boolean doTwoBlocksConnect(World world, BlockPos pos1, BlockPos pos2)
	{
		IBlockState state1 = world.getBlockState(pos1);
		Block block1 = state1.getBlock();
		if (!(block1 instanceof IElectricalBlock))
		{
			return false;
		}
		IBlockState state2 = world.getBlockState(pos2);
		Block block2 = state2.getBlock();
		if (!(block2 instanceof IElectricalBlock))
		{
			return false;
		}

		IElectricalBlock ieb1 = (IElectricalBlock)block1;
		IElectricalBlock ieb2 = (IElectricalBlock)block2;
		return (ieb1.doesThisBlockConnectTo(world, state1, pos1, pos2) && ieb2.doesThisBlockConnectTo(world, state2, pos2, pos1));
	}
}
