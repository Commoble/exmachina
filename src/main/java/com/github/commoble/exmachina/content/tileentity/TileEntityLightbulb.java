package com.github.commoble.exmachina.common.tileentity;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.common.block.BlockWithAllFacing;
import com.github.commoble.exmachina.common.electrical.Circuit;
import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.CircuitHelper;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.electrical.Node;
import com.github.commoble.exmachina.common.electrical.ResistorElement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityLightbulb extends TileEntity implements ICircuitElementHolderTE, ITickable
{
	@Nonnull
	public Circuit circuit = Circuit.INVALID_CIRCUIT;
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
	public void tick()
	{
		if (!world.isRemote)
		{
			this.onPossibleCircuitUpdate();
		}
	}
	
	public EnumFacing getFrontFace()
	{
		IBlockState state = this.world.getBlockState(this.pos);
		return state.get(BlockWithAllFacing.FACING);
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
			double resistance = this.element.getNominalResistance();
			double power = this.element.power; 
			double voltage = Math.sqrt(resistance*power);
			double current = voltage/resistance;
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
