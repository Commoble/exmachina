package commoble.exmachina.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CircuitManagerCapability
{
	/**
	 * The capability instance for retrieving circuits from (forge fills in the field).
	 * Note that INSTANCE.getDefaultInstance returns a permanently empty circuit manager,
	 * use world.getCapability(CircuitManagerCapability.INSTANCE) to get a circuit manager
	 * capable of managing circuits
	 */
	@CapabilityInject(CircuitManager.class)
	public static final Capability<CircuitManager> INSTANCE = null;
}
