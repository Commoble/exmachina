package com.github.commoble.exmachina.common.block;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.CircuitHelper;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// copper wire has resistance of about 1 uOhm
public class BlockWire extends Block implements IElectricalBlock
{
	public static final EnumSet<EnumFacing> CONNECTABLE_FACES = EnumSet.allOf(EnumFacing.class);
	public static final double WIRE_RESISTANCE = 0.000001D;
	

	public BlockWire(Block.Properties props)
	{
		super(props);
	}

	@Override
	@Nonnull
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos)
	{
		// TODO Auto-generated method stub
		return BlockWire.CONNECTABLE_FACES;
	}

	@Override
	@Nonnull
	public ElectricalValues getElectricalValues(World world, IBlockState blockState, BlockPos pos)
	{
		// Problem: wire blocks have no element associated with them
		// solution: find the nearest component with a real element and get its current
		// not a perfect implementation but it'll work in most circumstances
		CircuitElement element = CircuitHelper.getNearestCircuitElement(world, pos);
		if (element == null)
		{
			return new ElectricalValues(0D, 0D, BlockWire.WIRE_RESISTANCE, 0D);
		}
		else
		{
			double current = Math.abs(element.getCurrent());
			double resistance = BlockWire.WIRE_RESISTANCE;
			double voltage = current*resistance;
			double power = voltage*current;
			return new ElectricalValues(voltage, current, resistance, power);
		}
	}
}
