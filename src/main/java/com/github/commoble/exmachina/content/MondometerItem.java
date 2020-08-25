package com.github.commoble.exmachina.content;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.exmachina.api.Circuit;
import com.github.commoble.exmachina.api.CircuitComponent;
import com.github.commoble.exmachina.api.CircuitManager;
import com.github.commoble.exmachina.api.CircuitManagerCapability;
import com.github.commoble.exmachina.util.EngineeringNotation;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class MondometerItem extends Item
{
	public static final ITextComponent NO_CIRCUIT_MESSAGE = new TranslationTextComponent("exmachina.mondometer.no_circuit");
	public static final String LOAD_MESSAGE = "exmachina.mondometer.load";
	public static final String SOURCE_MESSAGE = "exmachina.mondometer.source";

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
		player.sendStatusMessage(message, false);
	}
	
	public static ITextComponent getCircuitMessage(World world, Circuit circuit, BlockPos pos)
	{
		// what do we want to display?
		// current going through the circuit
		// if we have a load, display its resistance, power flux, and voltage
		// if we have a voltage source, display its voltage and power flux
		// if we have both a load and power source (e.g. a rechargeable battery), display these separately
		Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> cache = circuit.getComponentCache();
		Pair<BlockState, ? extends CircuitComponent> pair = cache.get(pos);
		if (pair != null) // this probably isn't necessary but will do it for maximum safety
		{	// (we could also just get the blockstate from the world and the component from the data manager)
			
			double current = circuit.getCurrent();
			if (current == 0)
			{
				return NO_CIRCUIT_MESSAGE; // avoid divide-by-zero issues
			}
			
			String ampsInfo = EngineeringNotation.toSIUnit(current, "A", 2);
//			ITextComponent message = new TranslationTextComponent("exmachina.mondometer.current").append(new StringTextComponent(EngineeringNotation.toSIUnit(current, "A", 2)));
			
			BlockState state = pair.getLeft();
			CircuitComponent component = pair.getRight();
			double load = component.getLoad(world, state, pos);
			double source = component.getSource(world, state, pos);
			if (load > 0D)
			{
				TranslationTextComponent loadMessage = getLoadMessage(current, load, ampsInfo);
				if (source > 0D)
				{
					TranslationTextComponent sourceMessage = getSourceMessage(current, source, ampsInfo);
					return sourceMessage.append("\n").append(loadMessage);
				}
				else
				{
					return loadMessage;
				}
			}
			else if (source > 0D)
			{
				return getSourceMessage(current, source, ampsInfo);
			}
			else
			{
				return NO_CIRCUIT_MESSAGE;
			}
		}
		else
		{
			return NO_CIRCUIT_MESSAGE;
		}
		
	}
	
	public static TranslationTextComponent getLoadMessage(double current, double load, String ampsInfo)
	{
		double loadVolts = current * load;
		String loadVoltsInfo = EngineeringNotation.toSIUnit(loadVolts, "V", 2);
		String loadOhmsInfo = EngineeringNotation.toSIUnit(load, "Ω", 2);
		double loadWatts = current * loadVolts;
		String loadWattsInfo = EngineeringNotation.toSIUnit(loadWatts, "W", 2);
		return new TranslationTextComponent(LOAD_MESSAGE, loadVoltsInfo, ampsInfo, loadOhmsInfo, loadWattsInfo);
	}
	
	public static TranslationTextComponent getSourceMessage(double current, double source, String ampsInfo)
	{
		double voltageDrop = -source; // voltage is rising, nominal source is positive, so the drop is negative
		String sourceVoltsInfo = EngineeringNotation.toSIUnit(voltageDrop, "V", 2);
		double sourceOhms = voltageDrop / current;
		String sourceOhmsInfo = EngineeringNotation.toSIUnit(sourceOhms, "Ω", 2);
		double watts = voltageDrop * current;
		String sourceWattsInfo = EngineeringNotation.toSIUnit(watts, "W", 2);
		return new TranslationTextComponent(SOURCE_MESSAGE, sourceVoltsInfo, ampsInfo, sourceOhmsInfo, sourceWattsInfo);
	}
}
