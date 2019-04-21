package com.github.commoble.exmachina.common.item;

import com.github.commoble.exmachina.common.ExMachinaMod;
import com.github.commoble.exmachina.common.block.BlockRegistrar;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabs
{

	// creative tab for the stuff
	public static final ItemGroup tab = new ItemGroup(ExMachinaMod.MODID) {
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(BlockRegistrar.battery);
		}
	};

}
