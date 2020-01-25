package com.github.commoble.exmachina.api.circuit;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;

public class ComponentRegistry
{
	public static final Map<Block, WireProperties> WIRES = new HashMap<>();
	public static final Map<Block, ElementProperties> ELEMENTS = new HashMap<>();
	
	public static boolean contains(Block block)
	{
		return WIRES.containsKey(block) || ELEMENTS.containsKey(block);
	}
	
	public static IConnectionProvider getConnectionProvider(BlockContext context)
	{
		Block block = context.state.getBlock();
		if (WIRES.containsKey(block))
		{
			return WIRES.get(block).getWireContext(context);
		}
		else if (ELEMENTS.containsKey(block))
		{
			return ELEMENTS.get(block).getElementContext(context);
		}
		else
		{
			return IConnectionProvider.NULL_CONNECTOR;
		}
	}
	
	public static ElectricalValues getElectricalValues(BlockContext context)
	{
		Block block = context.state.getBlock();
		if (WIRES.containsKey(block))
		{
			return WIRES.get(block).getElectricalValues(context);
		}
		else if (ELEMENTS.containsKey(block))
		{
			return CircuitHelper.getNearestCircuitElement(context)
				.map(CircuitElement::getElectricalValues)
				.orElse(ElectricalValues.NULL_VALUES);
		}
		else
		{
			return ElectricalValues.NULL_VALUES;
		}
	}
}
