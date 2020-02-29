package com.github.commoble.exmachina.content.wireplinth;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlinthsInChunk implements IPlinthsInChunk, ICapabilityProvider, INBTSerializable<CompoundNBT>
{	
	private final LazyOptional<IPlinthsInChunk> holder = LazyOptional.of(() -> this);
	
	private Set<BlockPos> positions = new HashSet<>();
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == PlinthsInChunkCapability.INSTANCE)
		{
			return PlinthsInChunkCapability.INSTANCE.orEmpty(cap, this.holder);
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
		return (CompoundNBT)PlinthsInChunkCapability.INSTANCE.getStorage().writeNBT(PlinthsInChunkCapability.INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		PlinthsInChunkCapability.INSTANCE.getStorage().readNBT(PlinthsInChunkCapability.INSTANCE, this, null, nbt);
	}

}
