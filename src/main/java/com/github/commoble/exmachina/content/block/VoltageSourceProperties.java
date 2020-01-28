package com.github.commoble.exmachina.content.block;

import javax.annotation.concurrent.Immutable;

import com.github.commoble.exmachina.api.circuit.BlockContext;
import com.github.commoble.exmachina.api.circuit.ElementProperties;
import com.github.commoble.exmachina.api.circuit.ElementType;
import com.github.commoble.exmachina.api.circuit.TwoTerminalConnection;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

@Immutable
public class VoltageSourceProperties extends ElementProperties
{
	public final double voltage;
	
	public VoltageSourceProperties(double voltage)
	{
		super(ElementType.VOLTAGE_SOURCE);
		this.voltage = voltage;
	}

	@Override
	public TwoTerminalConnection getAllowedConnections(BlockContext context)
	{
		// assume block is facing in the positive direction
		if (context.state.has(BlockStateProperties.FACING))
		{
			Direction offset = context.state.get(BlockStateProperties.FACING);
			return new TwoTerminalConnection(context.pos.offset(offset), context.pos.offset(offset.getOpposite()));
		}
		else
		{
			return new TwoTerminalConnection(context.pos, context.pos);
		}
	}
	
	@Override
	public double getNominalValue(BlockContext context)
	{
		return this.voltage;
	}
	
	@Override
	public double getCurrent(double power, BlockContext context)
	{
		// P = V * I, I = P / V
		return power / this.getVoltage(power, context);
	}

	@Override
	public double getResistance(double power, BlockContext context)
	{
		// P = V^2 / R, R = P / V^2
		double voltage = this.getVoltage(power, context);
		return power / (voltage * voltage);
	}

	@Override
	public double getVoltage(double power, BlockContext context)
	{
		return this.voltage;
	}

}
