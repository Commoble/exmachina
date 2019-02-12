package com.github.commoble.exmachina.common.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenManager implements IWorldGenerator
{
	/**
	 * Called by the game registry during world generation
	 */
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		// check which plane we're generating in;
		// -1 = nether, 0 = overworld, 1 = END, other ID = some mod
		switch(world.provider.getDimension())
		{
			case -1:
				this.generateNether(world, random, chunkX*16, chunkZ*16);
			case 0:
				this.generateSurface(world, random, chunkX*16, chunkZ*16);
			case 1:
				this.generateEnd(world, random, chunkX*16, chunkZ*16);
		}
	}
	
	/**
	 * Handle Nether generation
	 * x,z = global tilespace coordinates, will be 0,0 in the local tilespace for the chunk
	 */
	private void generateNether(World world, Random random, int x, int z)
	{
		
	}
	
	/**
	 * Handle Surface generation
	 * x,z = global tilespace coordinates, will be 0,0 in the local tilespace for the chunk
	 */
	private void generateSurface(World world, Random random, int x, int z)
	{
		
	}
	
	/**
	 * Handle End generation
	 * x,z = global tilespace coordinates, will be 0,0 in the local tilespace for the chunk
	 */
	private void generateEnd(World world, Random random, int x, int z)
	{
		
	}
	
	
	private void addOreSingle(Block block, World world, Random random, int x, int z, int xMax, int zMax, int chancesToSpawn, int yMin, int yMax)
	{
		assert yMax > yMin : "addOreSingle: The Maximum Y must be greater than the minimum Y";
		assert yMin > 0 : "addOreSingle: The minimum Y must be greater than 0";
		assert xMax > 0 && xMax <= 16 : "addOreSingle: The Maximum X must be greater than 0 and no more than 16";
		assert zMax > 0 && zMax <= 16 : "addOreSingle: The Maximum Z must be greater than 0 and no more than 16";
		assert yMax < 256 && yMax > 0 : "addOreSingle: The Maximum Y must be less than 256 but greater than 0";
		
		WorldGenSingleMinable gen = new WorldGenSingleMinable(block);
		for (int i=0; i<chancesToSpawn; i++)
		{
			int posX = x + random.nextInt(xMax);
			int posZ = z + random.nextInt(zMax);
			int posY = yMin + random.nextInt(yMax - yMin);
			BlockPos pos = new BlockPos(posX, posY, posZ);
			gen.generate(world, random, pos);
		}
	}
	
	private void addOreVein(IBlockState block, World world, Random random, int x, int z, int xMax, int zMax, int maxVeinSize, int chancesToSpawn, int yMin, int yMax)
	{
		assert yMax > yMin : "addOreVein: The Maximum Y must be greater than the minimum Y";
		assert yMin > 0 : "addOreVein: The minimum Y must be greater than 0";
		assert xMax > 0 && xMax <= 16 : "addOreVein: The Maximum X must be greater than 0 and no more than 16";
		assert zMax > 0 && zMax <= 16 : "addOreVein: The Maximum Z must be greater than 0 and no more than 16";
		assert yMax < 256 && yMax > 0 : "addOreVein: The Maximum Y must be less than 256 but greater than 0";
		
		WorldGenMinable gen = new WorldGenMinable(block, maxVeinSize);
		
		for (int i=0; i<chancesToSpawn; i++)
		{
			int posX = x + random.nextInt(xMax);
			int posZ = z + random.nextInt(zMax);
			int posY = yMin + random.nextInt(yMax - yMin);
			gen.generate(world, random, new BlockPos(posX, posY, posZ));
		}
	}
	
}
