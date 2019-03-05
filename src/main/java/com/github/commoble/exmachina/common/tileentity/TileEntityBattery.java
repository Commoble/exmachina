package com.github.commoble.exmachina.common.tileentity;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.common.block.BlockWithFacing;
import com.github.commoble.exmachina.common.electrical.Circuit;
import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.CircuitHelper;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.electrical.Node;
import com.github.commoble.exmachina.common.electrical.VoltageSourceElement;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityBattery extends TileEntity implements ICircuitElementHolderTE, ITickable
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
		if (!world.isRemote)
		{
			this.onPossibleCircuitUpdate();
		}
	}
	
	public EnumFacing getFrontFace()	// positive end
	{
		IBlockState state = this.world.getBlockState(this.pos);
		return state.get(BlockWithFacing.FACING);
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
			EnumFacing front = this.getFrontFace();
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
