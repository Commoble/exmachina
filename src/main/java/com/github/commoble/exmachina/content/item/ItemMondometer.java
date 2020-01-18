package com.github.commoble.exmachina.content.item;

import com.github.commoble.exmachina.api.electrical.ElectricalValues;
import com.github.commoble.exmachina.api.electrical.EngineeringNotation;
import com.github.commoble.exmachina.content.block.IElectricalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemMondometer extends Item
{
	public ItemMondometer(Item.Properties props)
	{
		super(props);
	}

    /**
     * Called when a Block is right-clicked with this Item
     */
    @Override
	public ActionResultType onItemUse(ItemUseContext context)
    {
    	World world = context.getWorld();
    	if (!world.isRemote)
    	{
        	BlockPos pos = context.getPos();
        	BlockState state = world.getBlockState(pos);
        	Block block = state.getBlock();
        	if (world.getBlockState(pos).getBlock() instanceof IElectricalBlock)
        	{
        		ElectricalValues ev = ((IElectricalBlock)block).getElectricalValues(world, state, pos);

        		PlayerEntity player = context.getPlayer();
                player.sendStatusMessage(new StringTextComponent(
                		EngineeringNotation.toSIUnit(ev.voltage, "V") + "    " +
                		EngineeringNotation.toSIUnit(ev.current, "A") + "    " +
                		EngineeringNotation.toSIUnit(ev.resistance, "Ω") + "    " +
                		EngineeringNotation.toSIUnit(ev.power, "W")), false);
        		
        		return ActionResultType.SUCCESS;
        	}
    	}
        return super.onItemUse(context);
    }
}
