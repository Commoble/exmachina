package com.github.commoble.exmachina.api.circuit;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;

public class ElementContext implements IConnectionProvider
{
	public final BlockContext context;
	public final ElementProperties properties;
	
	private TwoTerminalConnection connection = null;
	private Set<BlockPos> connectionSet = null;
	
	public ElementContext(final BlockContext context, final ElementProperties properties)
	{
		this.context = context;
		this.properties = properties;
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
	
	@Nonnull
	public TwoTerminalConnection getTerminals()
	{
		if (this.connection == null)
		{
			this.connection = this.properties.getAllowedConnections(this.context);
		}
		return this.connection;
	}

	@Override
	public boolean canThisConnectTo(BlockContext context)
	{
		return this.getPotentialConnections().contains(context.pos);
	}

	@Override
	public Set<BlockPos> getPotentialConnections()
	{
		if (this.connectionSet == null)
		{
			this.connectionSet = this.getTerminals().toSet();
		}
		return this.getTerminals().toSet();
	}
}
