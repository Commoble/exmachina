package com.github.commoble.exmachina.common.tileentity;

import com.github.commoble.exmachina.common.block.IElectricalBlock;
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

public class TileEntityBattery extends TileEntity implements ITickable, ICircuitElementHolderTE
{

	protected EnumFacing positiveSide;
	protected EnumFacing negativeSide;
	
	public TileEntityBattery(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}
	
	public TileEntityBattery()
	{
		super(TileEntityRegistrar.teBatteryType);
	}

	public boolean circuit_update_check_pending = false;	// only set this on server
	
	public VoltageSourceElement element = null;

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
	
	public ElectricalValues getElectricalValues()
	{
		if (this.element != null)
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
	public void tick()
	{
		if (this.circuit_update_check_pending)
		{
			this.circuit_update_check_pending = false;
			System.out.println("Update pending");
			BlockPos selfPos = this.pos;
			BlockPos nextPos = selfPos.offset(this.positiveSide);
			BlockPos prevPos = selfPos.offset(this.negativeSide);
			IBlockState nextState = this.world.getBlockState(nextPos);
			Block nextBlock = nextState.getBlock();
			Block prevBlock = this.world.getBlockState(prevPos).getBlock();
			if (nextBlock instanceof IElectricalBlock && prevBlock instanceof IElectricalBlock)
			{
				// check if this is part of a complete circuit
				if (CircuitHelper.isCompleteCircuit(this.world, selfPos))
				{
					System.out.println("Complete circuit, building circuit");
					Circuit circuit = CircuitHelper.buildCircuit(this.world, selfPos, nextPos, prevPos);
				}
				else
				{
					System.out.println("Not complete circuit");
				}
			}
		}
	}
	
	public void setFacing(EnumFacing positiveSide)
	{
		this.positiveSide = positiveSide;
		this.negativeSide = positiveSide.getOpposite();
	}

	
}
