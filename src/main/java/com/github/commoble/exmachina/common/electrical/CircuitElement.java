package com.github.commoble.exmachina.common.electrical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A representation of a component in the circuit
 * indicating the component's position, as well as its adjacent
 * nodes and its electrical characteristics
 */
public abstract class CircuitElement
{
	public BlockPos componentPos;
	public IBlockState componentState;
	public Node nodeA;
	public Node nodeB;
		// they may be the same node
	
	public double power;	// this is set whenever the circuit is built or rebuilt

	public int identifier; // identifier of the component as seen by the circuit it is contained in
		// (determined when solving the circuit)
		// resistors will be 0,1,2, etc
		// sources will be 0,1,2, etc, independant from resistors

	public CircuitElement(World world, BlockPos componentPos, Node nodeA, Node nodeB)
	{
		this.componentPos = componentPos;
		this.componentState = world.getBlockState(componentPos);
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}
	
	@Override
	public int hashCode()
	{
		return this.componentPos.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof CircuitElement && ((CircuitElement)o).componentPos.equals(this.componentPos));
	}
	
	public abstract double getCurrent();
}
