package com.github.commoble.exmachina.api.circuit;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;

@Immutable
public class ElementContext implements IConnectionProvider
{
	public final BlockContext context;
	public final ElementProperties properties;
	public final TwoTerminalConnection connection;
	
	public ElementContext(final BlockContext context, final ElementProperties properties)
	{
		this.context = context;
		this.properties = properties;
		this.connection = this.properties.getAllowedConnections(context);
	}
	
	public CircuitElement createCircuitElement(Node positiveNode, Node negativeNode)
	{
		return new CircuitElement(this, positiveNode, negativeNode);
	}
	
	public double getElementValue()
	{
		return this.properties.getNominalValue(this.context);
	}
	
	public ElectricalValues getElectricalValues(CircuitElement element)
	{
		double power = element.power;
		double voltage = this.properties.getVoltage(power, this.context);
		double current = this.properties.getCurrent(power, this.context);
		double resistance = this.properties.getResistance(power, this.context);
		
		return new ElectricalValues(voltage, current, resistance, power);
	}

	@Override
	public boolean canThisConnectTo(BlockContext context)
	{
		return this.getPotentialConnections().contains(context.pos);
	}

	@Override
	public Set<BlockPos> getPotentialConnections()
	{
		return this.connection.set;
	}
}
