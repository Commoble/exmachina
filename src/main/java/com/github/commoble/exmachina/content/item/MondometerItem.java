package com.github.commoble.exmachina.content.item;

import com.github.commoble.exmachina.api.circuit.ComponentRegistry;
import com.github.commoble.exmachina.api.circuit.ElectricalValues;
import com.github.commoble.exmachina.api.util.BlockContext;
import com.github.commoble.exmachina.api.util.EngineeringNotation;

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
	public ActionResultType onItemUse(ItemUseContext useContext)
    {
    	World world = useContext.getWorld();
    	if (!world.isRemote)
    	{
        	BlockPos pos = useContext.getPos();
        	BlockState state = world.getBlockState(pos);
        	BlockContext blockContext = new BlockContext(state, pos);
        	ElectricalValues ev = ComponentRegistry.getElectricalValues(useContext.getWorld(), blockContext);

    		PlayerEntity player = useContext.getPlayer();
            player.sendStatusMessage(new StringTextComponent(
            		EngineeringNotation.toSIUnit(ev.voltage, "V") + "    " +
            		EngineeringNotation.toSIUnit(ev.current, "A") + "    " +
            		EngineeringNotation.toSIUnit(ev.resistance, "Ω") + "    " +
            		EngineeringNotation.toSIUnit(ev.power, "W")), false);
    	}
        return super.onItemUse(useContext);
    }
}
