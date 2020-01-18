package com.github.commoble.exmachina.content.block;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.api.electrical.CircuitElement;
import com.github.commoble.exmachina.api.electrical.CircuitHelper;
import com.github.commoble.exmachina.api.electrical.ElectricalValues;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

// copper wire has resistance of about 1 uOhm
public class BlockWire extends Block implements IElectricalBlock
{
	public static final EnumSet<Direction> CONNECTABLE_FACES = EnumSet.allOf(Direction.class);
	public static final double WIRE_RESISTANCE = 0.000001D;
	

	public BlockWire(Block.Properties props)
	{
		super(props);
	}

	@Override
	@Nonnull
	public Set<Direction> getConnectingFaces(IWorld world, BlockState blockState, BlockPos pos)
	{
		// TODO Auto-generated method stub
		return BlockWire.CONNECTABLE_FACES;
	}

	@Override
	@Nonnull
	public ElectricalValues getElectricalValues(World world, BlockState blockState, BlockPos pos)
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

	/**
	 * This is called after another block is placed next to a position containing this block
	 * 
	* Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
	* For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
	* returns its solidified counterpart.
	* Note that this method should ideally consider only the specific face passed in.
	*  
	* @param facingState The state that is currently at the position offset of the provided face to the stateIn at
	* currentPos is the position of this block
	* facingPos is the position of the adjacent block that triggered this method
	*/
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if (!worldIn.isRemote())
		{
			CircuitHelper.updateCircuit(worldIn, currentPos);
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

}
