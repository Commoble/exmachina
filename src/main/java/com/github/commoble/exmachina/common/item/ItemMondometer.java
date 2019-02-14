package com.github.commoble.exmachina.common.item;

import com.github.commoble.exmachina.common.block.IElectricalBlock;
import com.github.commoble.exmachina.common.electrical.ElectricalValues;
import com.github.commoble.exmachina.common.electrical.EngineeringNotation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemMondometer extends Item
{
	public ItemMondometer()
	{
		this.setCreativeTab(ItemRegistrar.tab);
		this.setMaxStackSize(1);
	}

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	if (!world.isRemote)
    	{
        	IBlockState state = world.getBlockState(pos);
        	Block block = state.getBlock();
        	if (world.getBlockState(pos).getBlock() instanceof IElectricalBlock)
        	{
        		ElectricalValues ev = ((IElectricalBlock)block).getElectricalValues(world, state, pos);

                player.sendStatusMessage(new TextComponentTranslation(
                		EngineeringNotation.toSIUnit(ev.voltage, "V") + "    " +
                		EngineeringNotation.toSIUnit(ev.current, "A") + "    " +
                		EngineeringNotation.toSIUnit(ev.resistance, "Ω") + "    " +
                		EngineeringNotation.toSIUnit(ev.power, "W"), new Object[0]), false);
        		
        		return EnumActionResult.SUCCESS;
        	}
    	}
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
