package com.github.commoble.exmachina.common.item;

import com.github.commoble.exmachina.common.ExMachinaMod;
import com.github.commoble.exmachina.common.block.BlockNames;
import com.github.commoble.exmachina.common.block.BlockRegistrar;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering items and itemblocks, and keeping their references
 * also handle creative tabs since those are closely related to items
 */
public class ItemRegistrar
{
	// creative tab for the stuff
	public static final ItemGroup tab = new ItemGroup(ExMachinaMod.MODID) {
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(Blocks.SANDSTONE);
		}
	};
	
	//itemblocks
	@ObjectHolder("exmachina:ash")
	public static final Item ash = null;

	@ObjectHolder("exmachina:battery")
	public static final Item battery = null;
	@ObjectHolder("exmachina:lightbulb")
	public static final Item lightbulb = null;
	@ObjectHolder("exmachina:wire")
	public static final Item wire = null;
	
	// real items
	@ObjectHolder("exmachina:mondometer")
	public static final Item mondometer = null;
	
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		//ItemLedger.itemBlockTransporter = registerItemBlock(event.getRegistry(), new ItemBlock(BlockLedger.blockTransporter), BlockLedger.TRANSPORTER_REGISTRY_NAME);
		//grinderItemBlock = registerItemBlock(event.getRegistry(), new ItemBlock(BlockLedger.grinderBlock), "grinder");
		//grinderItemBlock.setCreativeTab(trtab);
		registerItem(registry, new ItemBlock(BlockRegistrar.ash, new Item.Properties().group(ItemRegistrar.tab)), BlockNames.ASH_NAME);
		
		// itemblocks
		registerItem(registry, new ItemBlock(BlockRegistrar.battery, new Item.Properties().group(ItemRegistrar.tab)), BlockNames.BATTERY_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.wire, new Item.Properties().group(ItemRegistrar.tab)), BlockNames.WIRE_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.lightbulb, new Item.Properties().group(ItemRegistrar.tab)), BlockNames.LIGHTBULB_NAME);
		
		// real items
		registerItem(registry, new ItemMondometer(new Item.Properties().group(ItemRegistrar.tab).maxStackSize(1)), ItemNames.MONDOMETER_NAME);
	}
	
	private static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T newItem, String name)
	{
		String prefixedName = ExMachinaMod.MODID + ":" + name;
		newItem.setRegistryName(prefixedName);
		registry.register(newItem);
		return newItem;
	}
}
