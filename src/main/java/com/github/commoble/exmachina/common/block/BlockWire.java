package com.github.commoble.exmachina.common.block;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.commoble.exmachina.common.electrical.CircuitElement;
import com.github.commoble.exmachina.common.electrical.CircuitHelper;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.item.ItemRegistrar;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// copper wire has resistance of about 1 uOhm
public class BlockWire extends Block implements IElectricalBlock
{
	public static final EnumSet<EnumFacing> CONNECTABLE_FACES = EnumSet.allOf(EnumFacing.class);
	public static final double WIRE_RESISTANCE = 0.000001D;
	

	public BlockWire()
	{
		super(Material.CIRCUITS);
		this.setCreativeTab(ItemRegistrar.tab);
		this.setSoundType(SoundType.STONE);
		this.setHardness(0.2F);
	}

	@Override
	@Nonnull
	public Set<EnumFacing> getConnectingFaces(World world, IBlockState blockState, BlockPos pos)
	{
		// TODO Auto-generated method stub
		return this.CONNECTABLE_FACES;
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
			return ElectricalValues.NULL_VALUES;
		}
		else
		{
			double current = element.getCurrent();
			double resistance = BlockWire.WIRE_RESISTANCE;
			double voltage = current*resistance;
			double power = voltage*current;
			return new ElectricalValues(voltage, current, resistance, power);
		}
	}
	
	@Override
    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
	/*
	@Override
    @Deprecated
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
		
    }
    */
}
