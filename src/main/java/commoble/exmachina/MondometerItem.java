package commoble.exmachina;

import commoble.exmachina.engine.api.Circuit;
import commoble.exmachina.engine.api.CircuitManager;
import commoble.exmachina.util.EngineeringNotation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class MondometerItem extends Item
{
	public static final String NO_CIRCUIT_MESSAGE = "exmachinaessentials.mondometer.no_circuit";
	public MondometerItem(Properties props)
	{
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		if (context.getLevel() instanceof ServerLevel serverLevel)
		{
			BlockPos pos = context.getClickedPos();
			Player player = context.getPlayer();
			getAndSendInfo(serverLevel, CircuitManager.get(serverLevel), pos, player);
		}
		
		return super.useOn(context);
	}

	public static void getAndSendInfo(ServerLevel serverLevel, CircuitManager manager, BlockPos pos, Player player)
	{
		Circuit circuit = manager.getCircuit(pos);
		Component message = circuit.isPresent()
			? getCircuitMessage(serverLevel, circuit, pos)
			: Component.translatable(NO_CIRCUIT_MESSAGE);
		player.displayClientMessage(message, true);
	}
	
	public static Component getCircuitMessage(ServerLevel serverLevel, Circuit circuit, BlockPos pos)
	{
		double current = circuit.getCurrent();
		double power = circuit.getPowerSuppliedTo(pos);
		if (current == 0 || power == 0)
		{
			return Component.translatable(NO_CIRCUIT_MESSAGE); // avoid divide-by-zero issues
		}
		double volts = power / current;
		double resistance = volts / current;
		String ampsInfo = EngineeringNotation.toSIUnit(current, "A", 2);
		String wattsInfo = EngineeringNotation.toSIUnit(power, "W", 2);
		String voltsInfo = EngineeringNotation.toSIUnit(volts, "V", 2);
		String ohmsInfo = EngineeringNotation.toSIUnit(resistance, "Ω", 2);
		String message = String.format("%s, %s, %s, %s", voltsInfo, ampsInfo, ohmsInfo, wattsInfo);
		return Component.literal(message);
		
	}
}
