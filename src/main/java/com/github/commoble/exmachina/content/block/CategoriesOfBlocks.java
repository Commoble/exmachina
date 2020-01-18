package com.github.commoble.exmachina.common.block;

import java.util.HashSet;

import net.minecraft.block.Block;

public class CategoriesOfBlocks
{
	public static HashSet<Block> wireBlocks = new HashSet<Block>();
	public static HashSet<Block> activeComponentBlocks = new HashSet<Block>();	// blocks that create power
	public static HashSet<Block> passiveComponentBlocks = new HashSet<Block>();	// blocks that do not create power, only consume it
	
	public static void addWireBlock(Block block)
	{
		wireBlocks.add(block);
	}
	
	public static void addActiveComponentBlock(Block block)
	{
		activeComponentBlocks.add(block);
	}
	
	public static void addPassiveComponentBlock(Block block)
	{
		passiveComponentBlocks.add(block);
	}
	
	public static boolean isAnyComponentBlock(Block block)
	{
		return passiveComponentBlocks.contains(block) || activeComponentBlocks.contains(block);
	}
}
