package com.github.commoble.exmachina.content.tileentity;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.api.electrical.Circuit;
import com.github.commoble.exmachina.api.electrical.CircuitElement;
import com.github.commoble.exmachina.api.electrical.CircuitHelper;
import com.github.commoble.exmachina.api.electrical.ElectricalValues;
import com.github.commoble.exmachina.api.electrical.Node;
import com.github.commoble.exmachina.api.electrical.VoltageSourceElement;
import com.github.commoble.exmachina.content.block.BlockWithAllFacing;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TileEntityBattery extends TileEntity implements ICircuitElementHolderTE, ITickableTileEntity
{
	
	@Nonnull
	public Circuit circuit = Circuit.INVALID_CIRCUIT;
	public VoltageSourceElement element = null;
	
	public TileEntityBattery(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}
	
	public TileEntityBattery()
	{
		super(TileEntityRegistrar.teBatteryType);
	}

	@Override
	public void tick()
	{
		if (!this.world.isRemote)
		{
			this.onPossibleCircuitUpdate();
		}
	}
	
	public Direction getFrontFace()	// positive end
	{
		BlockState state = this.world.getBlockState(this.pos);
		return state.get(BlockWithAllFacing.FACING);
	}

	@Override
	public CircuitElement createCircuitElement(Node nodeA, Node nodeB)
	{
		this.element = new VoltageSourceElement(this.world, this.pos, nodeA, nodeB, 10D);
		return this.element;
	}

	@Override
	public CircuitElement getCircuitElement()
	{
		// TODO Auto-generated method stub
		return this.element;
	}
	
	@Override
	public void invalidateCircuit()
	{
		this.circuit.invalidate(this.world);
	}
	
	@Override
	public Circuit getCircuit()
	{
		return this.circuit;
	}
	
	@Override
	public void setCircuit(Circuit circuit)
	{
		this.circuit = circuit;
	}
	
	public ElectricalValues getElectricalValues()
	{
		if (this.element != null && this.circuit.isValid())
		{
			double voltage = this.element.getNominalVoltage();
			double power = this.element.power; 
			double current = this.element.power/voltage;
			double resistance = voltage/current;
			return new ElectricalValues(voltage, current, resistance, power);
		}
		return ElectricalValues.NULL_VALUES;
	}
	
	@Override
	public void onPossibleCircuitUpdate()
	{
		if (!this.circuit.isValid())
		{
			BlockPos selfPos = this.pos;
			Direction front = this.getFrontFace();
			BlockPos nextPos = selfPos.offset(front);
			//if (nextBlock instanceof IElectricalBlock && prevBlock instanceof IElectricalBlock)
			{
				// check if this is part of a complete circuit
				//if (CircuitHelper.isCompleteCircuit(this.world, selfPos))
				{
					this.circuit = CircuitHelper.buildCircuit(this.world, selfPos, nextPos);
				}
			}
		}
	}
	
}