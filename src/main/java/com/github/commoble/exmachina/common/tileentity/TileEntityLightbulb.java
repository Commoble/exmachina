package com.github.commoble.exmachina.common.tileentity;

import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.electrical.Node;
import com.github.commoble.exmachina.common.electrical.ResistorElement;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityLightbulb extends TileEntity implements ICircuitElementHolderTE
{
	public ResistorElement element;
	
	public TileEntityLightbulb(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}
	
	public TileEntityLightbulb()
	{
		super(TileEntityRegistrar.teLightbulbType);
	}

	@Override
	public CircuitElement createCircuitElement(Node nodeA, Node nodeB)
	{
		this.element = new ResistorElement(this.world, this.pos, nodeA, nodeB, 1000D);
		return this.element;
	}

	@Override
	public CircuitElement getCircuitElement()
	{
		// TODO Auto-generated method stub
		return this.element;
	}
	
	public ElectricalValues getElectricalValues()
	{
		if (this.element != null)
		{
			double resistance = this.element.getNominalResistance();
			double power = this.element.power; 
			double voltage = Math.sqrt(resistance*power);
			double current = voltage/resistance;
			return new ElectricalValues(voltage, current, resistance, power);
		}
		return ElectricalValues.NULL_VALUES;
	}
}
