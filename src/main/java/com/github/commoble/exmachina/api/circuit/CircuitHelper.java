package com.github.commoble.exmachina.api.circuit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.api.util.BlockContext;
import com.github.commoble.exmachina.util.BlockContextWithDist;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

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
	public static Circuit buildCircuit(IWorld world, BlockPos sourcePos, BlockPos startPos)
	{
		// we need to find at least one true node
		// once we do that we can expand the circuit naturally from that node
		// we only know that sourcePos is a component, we don't know whether startPos
			// is component, wire, or non-electrical block
		
		// Possibility A) startPos is a wire that connects to sourcePos
		// Possibility B) startPos is a component that connects to sourcePos
		// possibility C) sourcePos is a dead node
		
		// NEED TO CHECK FOR BIDIRECTIONAL CONNECTION
		
		Node groundNode;
		BlockContext sourceContext = BlockContext.getContext(sourcePos, world);
		BlockContext startContext = BlockContext.getContext(startPos, world);
		//Block startBlock = startContext.state.getBlock();
		if (CircuitHelper.doTwoBlocksConnect(sourceContext, startContext))
		{
			if (isWireBlock(startContext))	// normal node
			{
				groundNode = Node.buildNodeFrom(startContext, world);
			}
			else if (isElementBlock(startContext))	// virtual node
			{
				groundNode = Node.createVirtualNode(sourcePos, startPos);
			}
			else
			{
				// shouldn't happen, create a dead node
				// TODO reevaluate
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
	
	public static boolean isWireBlock(BlockContext context)
	{
		return ComponentRegistry.WIRES.containsKey(context.state.getBlock());
//		return CategoriesOfBlocks.wireBlocks.contains(world.getBlockState(pos).getBlock());
	}
	
	public static boolean isElementBlock(BlockContext context)
	{
		return ComponentRegistry.ELEMENTS.containsKey(context.state.getBlock());
	}
	
	// if an electrical block is added, we want to invalidate any circuit at that location AND any circuit
	// that the new block could potentially connect to
		// we don't need to know the old connections, just the location and the new connections
		// use the player-add-block event for this
	public static void onCircuitBlockAdded(IWorld world, BlockContext context)
	{
		BlockPos startPos = context.pos;
		IConnectionProvider connector = ComponentRegistry.getConnectionProvider(context);
		Set<BlockPos> connectionSet = connector.getPotentialConnections();
		for (BlockPos pos : connectionSet)
		{
			WorldCircuitManager.invalidateCircuitAt(world, pos);
		}
		if (!connectionSet.contains(startPos))	// also invalidate the position of the block that was added if it wasn't already
		{
			WorldCircuitManager.invalidateCircuitAt(world, startPos);
		}
	}
	
	// if an electrical block has been removed, we want to invalidate any circuit that existed at that location
	// we don't need to know the old connections, just the location
	// things that can remove blocks:
		// player breaking
		// pistons
		// explosions
	// These and anything else is covered by using the NotifyNeighborEvent and checking if
	// an electrical block used to be here but isn't now (NNE fires after blockstate change)
	public static void onCircuitBlockRemoved(IWorld world, BlockPos pos)
	{
		WorldCircuitManager.invalidateCircuitAt(world, pos);
	}
	
	// any other physical update to circuits
	// (such as a blockstate changing itself to a differently-connecting blockstate)
	// must manually invalidate the circuit
	
	
	
	
	
	
	public static void validateCircuitAt(IWorld world, BlockContext context)
	{
		BlockState newState = context.state;
		Block newBlock = newState.getBlock();
		
		Circuit oldCircuit = WorldCircuitManager.getCircuit(world, context.pos);
		ElementProperties properties = ComponentRegistry.ELEMENTS.get(newBlock);
		if (properties != null) // we have an element block
		{
			if (oldCircuit == null || !oldCircuit.isValid())
			{
				// we have an element block that needs a new circuit built
				BlockPos nextPos = properties.getAllowedConnections(context).positiveEnd;
				WorldCircuitManager.addCircuit(world, CircuitHelper.buildCircuit(world, context.pos, nextPos));
			}
		}
		
	}
	
	/** Gets the nearest CircuitElement to a given wire block position
	 * following along IElectricalBlocks
	 * returns empty if none is found
	 */
	public static Optional<CircuitElement> getNearestCircuitElement(IWorld world, BlockContext startContext)
	{
		// current implementation is based on Djikstra's algorithm
		// 
		// if we start on an element, just return that
		if (CircuitHelper.isElementBlock(startContext))
		{
			return WorldCircuitManager.getElement(world, startContext);
		}
		
		HashMap<BlockPos, Integer> dists = new HashMap<BlockPos, Integer>();	// known distance from position to start

		// the head of this queue is the "lowest" member of the queue
		// in this case, it will be the blockpos with the lowest/shortest distance to the start
		PriorityQueue<BlockContextWithDist> pq = new PriorityQueue<BlockContextWithDist>();
		pq.add(new BlockContextWithDist(0, startContext));
		dists.put(startContext.pos, 0);
		BlockContext nearestKnownElement = null;
		int distanceToNearestKnownElement = Integer.MAX_VALUE;
		
		while(!pq.isEmpty())
		{
			BlockContextWithDist closest = pq.poll();// won't be null because PQ is not empty
			if (nearestKnownElement == null || closest.dist < distanceToNearestKnownElement)
			{				
				if (CircuitHelper.isElementBlock(closest.context))
				{
					nearestKnownElement = closest.context;
					distanceToNearestKnownElement = closest.dist;
				}
			}
			// get all adjacent connected blocks
//			EnumSet<Direction> checkFaces = CircuitHelper.getBiConnectedElectricalBlocks(world, closest.pos);
			Set<BlockContext> checkPositions = CircuitHelper.getBiConnectedElectricalBlocks(closest.context, world);
			for (BlockContext nextContext : checkPositions)
			{
				if (!dists.containsKey(nextContext.pos) || dists.get(nextContext.pos) > closest.dist + 1)	// TODO replace with wire resist
				{
					dists.put(nextContext.pos, closest.dist+1);
					//prevs.put(nextPos, closest.pos);
					pq.add(new BlockContextWithDist(closest.dist+1, nextContext));
				}
			}
		}
		return WorldCircuitManager.getElement(world, nearestKnownElement);
	}
	
	/** get a set of the blocks that this block connects to that also connect back to this block **/
	
	@Nonnull
	public static Set<BlockContext> getBiConnectedElectricalBlocks(final BlockContext context, final IWorld world)
	{
		return ComponentRegistry.getConnectionProvider(context).getPotentialConnections().stream()
			.map(pos -> context.getNewPosContext(pos, world))
			.filter(otherContext -> CircuitHelper.doTwoBlocksConnect(context, otherContext))
			.collect(Collectors.toCollection(HashSet::new));
	}
	
//	@Nonnull
//	public static EnumSet<Direction> getBiConnectedElectricalBlocks(IWorld world, BlockPos pos)
//	{
//		BlockState checkState = world.getBlockState(pos);
//		Block checkBlock = checkState.getBlock();
//		EnumSet<Direction> returnFaces = EnumSet.noneOf(Direction.class);
//		
////		if (checkBlock instanceof IElectricalBlock) // TODO
////		{
////			Set<Direction> checkFaces = ((IElectricalBlock)checkBlock).getConnectingFaces(world, checkState, pos);
////			for (Direction face : checkFaces)
////			{
////				BlockPos nextPos = pos.offset(face);
////				BlockState nextState = world.getBlockState(nextPos);
////				Block nextBlock = nextState.getBlock();
////				if (nextBlock instanceof IElectricalBlock)
////				{
////					Set<Direction> nextBlocksFaces = ((IElectricalBlock)nextBlock).getConnectingFaces(world, nextState, nextPos);
////					for (Direction nextFace : nextBlocksFaces)
////					{
////						if (nextFace.getOpposite() == face)
////						{
////							returnFaces.add(face);
////						}
////					}
////				}
////			}
////		}
//		return returnFaces;
//	}
	
	public static boolean doTwoBlocksConnect(BlockContext context1, BlockContext context2)
	{
//		BlockState state1 = world.getBlockState(pos1);
//		Block block1 = state1.getBlock();
//		if (!(ComponentRegistry.contains(block1)))
//		{
//			return false;
//		}
//		BlockState state2 = world.getBlockState(pos2);
//		Block block2 = state2.getBlock();
//		if (!(ComponentRegistry.contains(block2)))
//		{
//			return false;
//		}
		IConnectionProvider block1 = ComponentRegistry.getConnectionProvider(context1);
		IConnectionProvider block2 = ComponentRegistry.getConnectionProvider(context2);
		return block1.canThisConnectTo(context2) && block2.canThisConnectTo(context1);
		//return false;
//		BlockState state1 = world.getBlockState(pos1);
//		Block block1 = state1.getBlock();
//		if (!(block1 instanceof IElectricalBlock))
//		{
//			return false;
//		}
//		BlockState state2 = world.getBlockState(pos2);
//		Block block2 = state2.getBlock();
//		if (!(block2 instanceof IElectricalBlock))
//		{
//			return false;
//		}
//
//		IElectricalBlock ieb1 = (IElectricalBlock)block1;
//		IElectricalBlock ieb2 = (IElectricalBlock)block2;
//		return (ieb1.doesThisBlockConnectTo(world, state1, pos1, pos2) && ieb2.doesThisBlockConnectTo(world, state2, pos2, pos1));
	}
}
