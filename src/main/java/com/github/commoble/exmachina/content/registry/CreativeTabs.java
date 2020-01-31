package com.github.commoble.exmachina.content.registry;

import com.github.commoble.exmachina.ExMachinaMod;

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
