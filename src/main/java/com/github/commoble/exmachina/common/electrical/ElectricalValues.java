package com.github.commoble.exmachina.common.electrical;

import javax.annotation.Nonnull;

/**
 * Contains information about an IElectricalBlock
 */
@Nonnull
public class ElectricalValues
{
	public final double voltage;
	public final double current;
	public final double resistance;
	public final double power;
	
	public static final ElectricalValues NULL_VALUES = new ElectricalValues(0D, 0D, Double.MAX_VALUE, 0D);
	
	public ElectricalValues(double voltage, double current, double resistance, double power)
	{
		this.voltage = voltage;
		this.current = current;
		this.resistance = resistance;
		this.power = power;
	}
}
