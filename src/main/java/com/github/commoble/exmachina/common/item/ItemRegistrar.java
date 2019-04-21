package com.github.commoble.exmachina.common.item;

import com.github.commoble.exmachina.common.ExMachinaMod;
import com.github.commoble.exmachina.common.block.BlockNames;
import com.github.commoble.exmachina.common.block.BlockRegistrar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Class for registering items and itemblocks, and keeping their references
 * also handle creative tabs since those are closely related to items
 */
@ObjectHolder(ExMachinaMod.MODID)
public class ItemRegistrar
{
	//itemblocks
	@ObjectHolder(BlockNames.ASH_NAME)
	public static final Item ash = null;

	@ObjectHolder(BlockNames.BATTERY_NAME)
	public static final Item battery = null;
	
	@ObjectHolder(BlockNames.LIGHTBULB_NAME)
	public static final Item lightbulb = null;
	
	@ObjectHolder(BlockNames.WIRE_NAME)
	public static final Item wire = null;
	
	@ObjectHolder(BlockNames.ELECTRIC_FURNACE_NAME)
	public static final Item electric_furnace = null;
	
	@ObjectHolder(BlockNames.BRASS_TUBE_NAME)
	public static final Item brass_tube = null;
	
	// real items
	@ObjectHolder("exmachina:mondometer")
	public static final Item mondometer = null;
	
	
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		//ItemLedger.itemBlockTransporter = registerItemBlock(event.getRegistry(), new ItemBlock(BlockLedger.blockTransporter), BlockLedger.TRANSPORTER_REGISTRY_NAME);
		//grinderItemBlock = registerItemBlock(event.getRegistry(), new ItemBlock(BlockLedger.grinderBlock), "grinder");
		//grinderItemBlock.setCreativeTab(trtab);
		registerItem(registry, new ItemBlock(BlockRegistrar.ash, new Item.Properties().group(CreativeTabs.tab)), BlockNames.ASH_NAME);
		
		// itemblocks
		registerItem(registry, new ItemBlock(BlockRegistrar.battery, new Item.Properties().group(CreativeTabs.tab)), BlockNames.BATTERY_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.wire, new Item.Properties().group(CreativeTabs.tab)), BlockNames.WIRE_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.lightbulb, new Item.Properties().group(CreativeTabs.tab)), BlockNames.LIGHTBULB_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.electric_furnace, new Item.Properties().group(CreativeTabs.tab)), BlockNames.ELECTRIC_FURNACE_NAME);
		registerItem(registry, new ItemBlock(BlockRegistrar.brass_tube, new Item.Properties().group(CreativeTabs.tab)), BlockNames.BRASS_TUBE_NAME);
		
		// real items
		registerItem(registry, new ItemMondometer(new Item.Properties().group(CreativeTabs.tab).maxStackSize(1)), ItemNames.MONDOMETER_NAME);
	}
	
	private static <T extends Item> T registerItem(IForgeRegistry<Item> registry, T newItem, String name)
	{
		String prefixedName = ExMachinaMod.MODID + ":" + name;
		newItem.setRegistryName(prefixedName);
		registry.register(newItem);
		return newItem;
	}
}
