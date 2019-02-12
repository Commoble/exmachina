package com.github.commoble.exmachina.common.electrical;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A representation of a resistor in the abstract circuit
 */
public class ResistorElement extends CircuitElement
{
	private final double nominalResistance;	// the known resistance value of the resistor
	
	public ResistorElement(World world, BlockPos componentPos, Node nodeA, Node nodeB, double resistance)
	{
		super(world, componentPos, nodeA, nodeB);
		this.nominalResistance = resistance;
	}
	
	public double getNominalResistance()
	{
		return this.nominalResistance;
	}
}
