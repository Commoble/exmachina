package com.github.commoble.exmachina.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Like regular fire, but sometimes burns blocks to ash instead of destroying them completely
 * @author Joseph
 *
 */
public class BlockExtendedFire extends BlockFire
{
	public static final float ASH_CHANCE = 1.0F;
	
	public BlockExtendedFire()
	{
		super(Block.Properties.create(Material.FIRE, MaterialColor.TNT).doesNotBlockMovement().needsRandomTick().hardnessAndResistance(0.0F).lightValue(15).sound(SoundType.CLOTH));
	}
	
	// TODO fix ATs
	
	/*@Override
	// too lazy to access transform, copied from BlockFire
	public void tick(IBlockState state, World worldIn, BlockPos pos, Random random)
	{
	      if (worldIn.getGameRules().getBoolean("doFireTick"))
	      {
	         if (!worldIn.isAreaLoaded(pos, 2)) return; // Forge: prevent loading unloaded chunks when spreading fire
	         if (!state.isValidPosition(worldIn, pos)) {
	            worldIn.removeBlock(pos);
	         }

	         IBlockState other = worldIn.getBlockState(pos.down());
	         boolean flag = other.isFireSource(worldIn, pos.down(), EnumFacing.UP);
	         int i = state.get(AGE);
	         if (!flag && worldIn.isRaining() && this.canDie(worldIn, pos) && random.nextFloat() < 0.2F + (float)i * 0.03F) {
	            worldIn.removeBlock(pos);
	         }
	         else
	         {
	            int j = Math.min(15, i + random.nextInt(3) / 2);
	            if (i != j)
	            {
	               state = state.with(AGE, Integer.valueOf(j));
	               worldIn.setBlockState(pos, state, 4);
	            }

	            if (!flag)
	            {
	               worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn) + random.nextInt(10));
	               if (!this.func_196447_a(worldIn, pos))
	               {
	                  if (worldIn.getBlockState(pos.down()).getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) != BlockFaceShape.SOLID || i > 3)
	                  {
	                     worldIn.removeBlock(pos);
	                  }

	                  return;
	               }

	               if (i == 15 && random.nextInt(4) == 0 && !this.canCatchFire(worldIn, pos.down(), EnumFacing.UP))
	               {
	                  worldIn.removeBlock(pos);
	                  return;
	               }
	            }

	            boolean flag1 = worldIn.isBlockinHighHumidity(pos);
	            int k = flag1 ? -50 : 0;
	            this.tryBurn(worldIn, pos.east(), 300 + k, random, i, EnumFacing.WEST);
	            this.tryBurn(worldIn, pos.west(), 300 + k, random, i, EnumFacing.EAST);
	            this.tryBurn(worldIn, pos.down(), 250 + k, random, i, EnumFacing.UP);
	            this.tryBurn(worldIn, pos.up(), 250 + k, random, i, EnumFacing.DOWN);
	            this.tryBurn(worldIn, pos.north(), 300 + k, random, i, EnumFacing.SOUTH);
	            this.tryBurn(worldIn, pos.south(), 300 + k, random, i, EnumFacing.NORTH);
	            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

	            for(int l = -1; l <= 1; ++l)
	            {
	               for(int i1 = -1; i1 <= 1; ++i1)
	               {
	                  for(int j1 = -1; j1 <= 4; ++j1)
	                  {
	                     if (l != 0 || j1 != 0 || i1 != 0)
	                     {
	                        int k1 = 100;
	                        if (j1 > 1)
	                        {
	                           k1 += (j1 - 1) * 100;
	                        }

	                        blockpos$mutableblockpos.setPos(pos).move(l, j1, i1);
	                        int l1 = this.getNeighborEncouragement(worldIn, blockpos$mutableblockpos);
	                        if (l1 > 0)
	                        {
	                           int i2 = (l1 + 40 + worldIn.getDifficulty().getId() * 7) / (i + 30);
	                           if (flag1)
	                           {
	                              i2 /= 2;
	                           }

	                           if (i2 > 0 && random.nextInt(k1) <= i2 && (!worldIn.isRaining() || !this.canDie(worldIn, blockpos$mutableblockpos)))
	                           {
	                              int j2 = Math.min(15, i + random.nextInt(5) / 4);
	                              worldIn.setBlockState(blockpos$mutableblockpos, this.getStateForPlacement(worldIn, blockpos$mutableblockpos).with(AGE, Integer.valueOf(j2)), 3);
	                           }
	                        }
	                     }
	                  }
	               }
	            }

	         }
	      }
	   }
	
	// mostly copied from private void tryCatchFire in BlockFire, with edit to burn to ash instead of removing block
	private void tryBurn(World worldIn, BlockPos pos, int chance, Random random, int age, EnumFacing face)
    {
        int i = worldIn.getBlockState(pos).getBlock().getFlammability(worldIn, pos, face);
        IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate == Blocks.GRASS.getDefaultState())
		{
			worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
		}

        else if (random.nextInt(chance) < i)
        {

            if (random.nextInt(age + 10) < 5 && !worldIn.isRainingAt(pos))
            {
                int j = age + random.nextInt(5) / 4;

                if (j > 15)
                {
                    j = 15;
                }

                worldIn.setBlockState(pos, this.getDefaultState().withProperty(AGE, Integer.valueOf(j)), 3);
            }
            else
            {
            	// check whether to make ash before setting the block to air because
            	// only collidable blocks create ash
            	boolean make_ash = !iblockstate.getBlock().isPassable(worldIn, pos) && worldIn.rand.nextFloat() < ASH_CHANCE;
            	
            	worldIn.setBlockToAir(pos);
            	
            	if (make_ash)
                {
            		BlockAsh.tryGenerateAsh(worldIn, pos);
                }
            }

            if (iblockstate.getBlock() == Blocks.TNT)
            {
                Blocks.TNT.onPlayerDestroy(worldIn, pos, iblockstate.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }
    }*/
}
