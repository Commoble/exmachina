package com.github.commoble.exmachina.common.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSingleMinable extends WorldGenerator
{
	private Block block;
	private int blockmeta;
	private Block target;
	
	public WorldGenSingleMinable(Block block, int meta, Block target)
	{
		this.block = block;
		this.blockmeta = meta;
		this.target = target;
	}
	
	public WorldGenSingleMinable(Block block, Block target)
	{
		this(block, 0, target);
	}
	
	public WorldGenSingleMinable(Block block)
	{
		this(block, Blocks.STONE);
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isReplaceableOreGen(state,  world,  pos,  BlockMatcher.forBlock(this.target)))
		{
			world.setBlockState(pos,  block.getStateFromMeta(this.blockmeta));
		}
		return true;
	}
}
