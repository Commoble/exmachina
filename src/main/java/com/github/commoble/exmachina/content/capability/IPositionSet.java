package com.github.commoble.exmachina.content.capability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.github.commoble.exmachina.content.util.NBTListHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IPositionSet
{
	@CapabilityInject(IPositionSet.class)
	static Capability<IPositionSet> POSITION_SET_CAPABILITY = null;
	
	/** get the set of blockpositions in the chunk (local to the chunk) **/
	public Set<BlockPos> getPositions();
	
	/** set a new set of positions to the chunk **/ 
	public void setPositions(Set<BlockPos> set);
	
	public static class Storage implements Capability.IStorage<IPositionSet>
	{
		public static final String POSITIONS = "positions";
		
		private static final NBTListHelper<BlockPos> POS_LISTER = new NBTListHelper<>(
			POSITIONS,
			pos -> NBTUtil.writeBlockPos(pos),
			nbt -> NBTUtil.readBlockPos(nbt)
			);
		
		@Override
		public INBT writeNBT(Capability<IPositionSet> capability, IPositionSet instance, Direction side)
		{
			return POS_LISTER.write(new ArrayList<>(instance.getPositions()), new CompoundNBT());
		}

		@Override
		public void readNBT(Capability<IPositionSet> capability, IPositionSet instance, Direction side, INBT nbt)
		{
			if (nbt instanceof CompoundNBT)
			{
				instance.setPositions(new HashSet<>(POS_LISTER.read((CompoundNBT)nbt)));
			}
		}
		
	}
}
