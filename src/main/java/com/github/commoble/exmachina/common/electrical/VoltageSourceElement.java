package com.github.commoble.exmachina.common.electrical;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VoltageSourceElement extends CircuitElement
{
	private double nominalVoltage;	// the known voltage value of the source

	// For circuit analysis purposes it is assumed that nodeA will be touching the "positive" end of the source
	// and nodeB will be touching the "negative" end
	// (if nominalVoltage is negative for whatever reason, positiveNode will be touching the negative end of the source, etc)
	public VoltageSourceElement(World world, BlockPos componentPos, Node positiveNode, Node negativeNode, double nominalVoltage)
	{
		super(world, componentPos, positiveNode, negativeNode);
		this.nominalVoltage = nominalVoltage;
	}

	public double getNominalVoltage()
	{
		return this.nominalVoltage;
	}
	
	public Node getPositiveNode()
	{
		return this.nodeA;
	}
	
	public Node getNegativeNode()
	{
		return this.nodeB;
	}

	@Override
	public double getCurrent()
	{
		return this.power * this.nominalVoltage;
	}
}
