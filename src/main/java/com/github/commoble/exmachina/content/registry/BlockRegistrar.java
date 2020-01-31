package com.github.commoble.exmachina.content.registry;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.RegistryNames;
import com.github.commoble.exmachina.content.ResourceLocations;
import com.github.commoble.exmachina.content.block.BlockBattery;
import com.github.commoble.exmachina.content.block.BlockLightbulb;
import com.github.commoble.exmachina.content.util.RegistryHelper;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthBlock;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
	
	@ObjectHolder(RegistryNames.WIRE_PLINTH)
	public static final WirePlinthBlock wire_plinth = null;
	
	

	public static void registerBlocks(IForgeRegistry<Block> registry)
	{
		RegistryHelper.register(registry, ResourceLocations.BATTERY, new BlockBattery(Block.Properties.create(Material.IRON, MaterialColor.BROWN).hardnessAndResistance(2F, 5F)));
		RegistryHelper.register(registry, ResourceLocations.LIGHTBULB, new BlockLightbulb(Block.Properties.create(Material.GLASS, MaterialColor.GOLD).hardnessAndResistance(0.3F, 0F).sound(SoundType.GLASS)));
		RegistryHelper.register(registry, ResourceLocations.WIRE_PLINTH, new WirePlinthBlock(Block.Properties.create(Material.IRON, MaterialColor.GOLD).hardnessAndResistance(2F, 5F)));
		
	}
}
