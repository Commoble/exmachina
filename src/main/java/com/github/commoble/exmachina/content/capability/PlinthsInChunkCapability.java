package com.github.commoble.exmachina.content.capability;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlinthsInChunkCapability implements IPositionSet, ICapabilityProvider, INBTSerializable<CompoundNBT>
{	
	private final LazyOptional<IPositionSet> holder = LazyOptional.of(() -> this);
	
	private Set<BlockPos> positions = new HashSet<>();
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == IPositionSet.POSITION_SET_CAPABILITY)
		{
			return IPositionSet.POSITION_SET_CAPABILITY.orEmpty(cap, this.holder);
		}
		else
		{
			return LazyOptional.empty();
		}
	}

	@Override
	public Set<BlockPos> getPositions()
	{
		return this.positions;
	}

	@Override
	public void setPositions(Set<BlockPos> set)
	{
		this.positions = set;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT)IPositionSet.POSITION_SET_CAPABILITY.getStorage().writeNBT(IPositionSet.POSITION_SET_CAPABILITY, this, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		IPositionSet.POSITION_SET_CAPABILITY.getStorage().readNBT(IPositionSet.POSITION_SET_CAPABILITY, this, null, nbt);
	}

}
