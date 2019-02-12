package com.github.commoble.exmachina.common.tileentity;

import com.github.commoble.exmachina.common.block.IElectricalBlock;
import com.github.commoble.exmachina.common.electrical.Circuit;
import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.CircuitHelper;
import com.github.commoble.exmachina.common.electrical.Node;
import com.github.commoble.exmachina.common.electrical.VoltageSourceElement;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityBattery extends TileEntity implements ITickable, ICircuitElementHolderTE
{
	public static final double NOMINAL_VOLTAGE = 12D;	// voltage output under ideal conditions
	
	protected EnumFacing positiveSide;
	protected EnumFacing negativeSide;
	
	protected double real_voltage = NOMINAL_VOLTAGE;
	protected double real_current = 0D;

	public boolean circuit_update_check_pending = false;	// only set this on server
	
	public VoltageSourceElement element = null;

	@Override
	public CircuitElement createCircuitElement(Node nodeA, Node nodeB)
	{
		this.element = new VoltageSourceElement(this.world, this.pos, nodeA, nodeB, 10D);
		return this.element;
	}

	@Override
	public void update()
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
