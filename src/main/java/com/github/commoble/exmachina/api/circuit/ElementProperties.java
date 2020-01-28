package com.github.commoble.exmachina.api.circuit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class ElementProperties
{
	public final ElementType type;
	
	public ElementProperties(ElementType type)
	{
		this.type = type;
	}
	
	public ElementContext getElementContext(final BlockContext context)
	{
		return new ElementContext(context, this);
	}
	
	/** value corresponding to ElementType, used for solving circuits **/
	public abstract double getNominalValue(BlockContext context);

	/** Used for meter readouts **/
	public abstract double getCurrent(double power, BlockContext context);
	
	/** Used for meter readouts **/
	public abstract double getResistance(double power, BlockContext context);
	
	/** Used for meter readouts **/
	public abstract double getVoltage(double power, BlockContext context);
	
	@Nonnull
	public abstract TwoTerminalConnection getAllowedConnections(BlockContext context);
}
