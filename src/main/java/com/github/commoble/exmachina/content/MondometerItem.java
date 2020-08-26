package com.github.commoble.exmachina.content;

import com.github.commoble.exmachina.api.Circuit;
import com.github.commoble.exmachina.api.CircuitManager;
import com.github.commoble.exmachina.api.CircuitManagerCapability;
import com.github.commoble.exmachina.util.EngineeringNotation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class MondometerItem extends Item
{
	public static final ITextComponent NO_CIRCUIT_MESSAGE = new TranslationTextComponent("exmachina.mondometer.no_circuit");

	public MondometerItem(Properties props)
	{
		super(props);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		if (!world.isRemote && player != null)
		{
			BlockPos pos = context.getPos();
			context.getWorld().getCapability(CircuitManagerCapability.INSTANCE)
				.ifPresent(manager -> getAndSendInfo(world, manager, pos, player));
		}
		
		return super.onItemUse(context);
	}

	public static void getAndSendInfo(World world, CircuitManager manager, BlockPos pos, PlayerEntity player)
	{
		ITextComponent message = manager.getCircuit(pos)
			.map(circuit -> getCircuitMessage(world, circuit, pos))
			.orElse(NO_CIRCUIT_MESSAGE);
		player.sendStatusMessage(message, true);
	}
	
	public static ITextComponent getCircuitMessage(World world, Circuit circuit, BlockPos pos)
	{
		double current = circuit.getCurrent();
		double power = circuit.getPowerSuppliedTo(pos);
		if (current == 0 || power == 0)
		{
			return NO_CIRCUIT_MESSAGE; // avoid divide-by-zero issues
		}
		double volts = power / current;
		double resistance = volts / current;
		String ampsInfo = EngineeringNotation.toSIUnit(current, "A", 2);
		String wattsInfo = EngineeringNotation.toSIUnit(power, "W", 2);
		String voltsInfo = EngineeringNotation.toSIUnit(volts, "V", 2);
		String ohmsInfo = EngineeringNotation.toSIUnit(resistance, "Ω", 2);
		String message = String.format("%s, %s, %s, %s", voltsInfo, ampsInfo, ohmsInfo, wattsInfo);
		return new StringTextComponent(message);
		
	}
}
