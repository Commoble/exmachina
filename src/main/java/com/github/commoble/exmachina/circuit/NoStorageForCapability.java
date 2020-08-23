package com.github.commoble.exmachina.circuit;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

/** Transient capability, no data is stored **/
public class NoStorageForCapability<T> implements IStorage<T>
{

		@Override
		public INBT writeNBT(Capability<T> capability, T instance, Direction side)
		{
			return null;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt)
		{
		}
}
