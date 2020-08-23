package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.ExMachina;
import com.github.commoble.exmachina.content.wire_post.WirePostBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistrar
{
	public static final DeferredRegister<Block> BLOCKS = ExMachina.createDeferredRegister(ForgeRegistries.BLOCKS);
	
	public static final RegistryObject<Block> WIRE_POST = BLOCKS.register(Names.WIRE_POST, () -> new WirePostBlock(AbstractBlock.Properties.from(Blocks.PISTON)));
	
	public static final RegistryObject<Block> CURRENT_SOURCE = BLOCKS.register(Names.BATTERY, () -> new Block(AbstractBlock.Properties.from(Blocks.field_235405_no_))); // from lodestone
	public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register(Names.ELECTRIC_FURNACE, () -> new Block(AbstractBlock.Properties.from(Blocks.BLAST_FURNACE)));
}
