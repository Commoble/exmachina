package com.github.commoble.exmachina.content;

import java.util.function.Supplier;

import com.github.commoble.exmachina.ExMachina;
import com.github.commoble.exmachina.content.wire_post.WireSpoolItem;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistrar
{
	public static final DeferredRegister<Item> ITEMS = ExMachina.createDeferredRegister(ForgeRegistries.ITEMS);
	
	public static final ItemGroup CREATIVE_TAB = new ItemGroup(ExMachina.MODID)
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(Items.ACACIA_BOAT);
		}
	};
	
	static
	{
		registerBlockItem(Names.WIRE_POST, BlockRegistrar.WIRE_POST);
		registerBlockItem(Names.BATTERY, BlockRegistrar.CURRENT_SOURCE);
		registerBlockItem(Names.ELECTRIC_FURNACE, BlockRegistrar.ELECTRIC_FURNACE);
		
		ITEMS.register(Names.MONDOMETER, () -> new MondometerItem(new Item.Properties().group(CREATIVE_TAB)));
		ITEMS.register(Names.WIRE_SPOOL, () -> new WireSpoolItem(new Item.Properties().group(CREATIVE_TAB)));
		
	}
	
	public static RegistryObject<BlockItem> registerBlockItem(String name, Supplier<? extends Block> blockGetter)
	{
		return ITEMS.register(name, () -> new BlockItem(blockGetter.get(), new Item.Properties().group(CREATIVE_TAB)));
	}
}
