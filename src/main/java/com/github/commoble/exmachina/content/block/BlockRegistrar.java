package com.github.commoble.exmachina.content.block;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.RegistryNames;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering blocks and storing their references
 */
@ObjectHolder(ExMachinaMod.MODID)
public class BlockRegistrar
{		
	@ObjectHolder(RegistryNames.BATTERY)
	public static final BlockBattery battery = null;
	
	@ObjectHolder(RegistryNames.LIGHTBULB)
	public static final BlockLightbulb lightbulb = null;
	
	

	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(registry, new BlockBattery(Block.Properties.create(Material.IRON, MaterialColor.BROWN).hardnessAndResistance(2F, 5F)), RegistryNames.BATTERY);
		registerBlock(registry, new BlockLightbulb(Block.Properties.create(Material.GLASS, MaterialColor.GOLD).hardnessAndResistance(0.3F, 0F).sound(SoundType.GLASS)), RegistryNames.LIGHTBULB);
		
	}
	
	private static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T newBlock, String name)
	{
		String prefixedName = ExMachinaMod.MODID + ":" + name;
		newBlock.setRegistryName(prefixedName);
		registry.register(newBlock);
		return newBlock;
	}
}
