package com.github.commoble.exmachina.api.circuit;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.github.commoble.exmachina.api.util.BlockContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@Immutable
public abstract class WireProperties
{
	public final double wireResistance;
	
	public WireProperties(final double wireResistance)
	{
		this.wireResistance = wireResistance;
	}
	
	public WireContext getWireContext(final BlockContext context)
	{
		return new WireContext(context, this);
	}
	
	public ElectricalValues getElectricalValues(BlockContext context, IWorld world)
	{
		// wire blocks have no element associated with them
		// to estimate wire resistance, find the nearest element connecting to this wire's node,
		// and get that element's current
		// it's not perfect but it'll work in most circumstances
		double resistance = this.wireResistance;
		double current = CircuitHelper.getNearestCircuitElement(world, context)
			.map(element -> element.getElectricalValues().current)
			.orElse(0D);
		double voltage = resistance * current;
		double power = voltage * current;
		return new ElectricalValues(voltage, current, resistance, power);
	}
	
	/**
	 * Returns a set of the positions that a given wire is allowed to connect to
	 * The returned set may be immutable and no attempt should be made to add or remove positions from it
	 */
	public abstract Set<BlockPos> getAllowedConnections(IWorld world, BlockContext context);
}
