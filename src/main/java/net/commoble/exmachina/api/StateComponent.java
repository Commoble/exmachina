package net.commoble.exmachina.api;

import org.jetbrains.annotations.NotNull;

import net.commoble.exmachina.api.Connector.StateConnector;
import net.commoble.exmachina.api.content.NoneDynamicProperty;

/**
 * A CircuitComponent, but baked for a particular BlockState
 * @param connector StateConnector of a particular blockstate
 * @param staticLoad double value of the resistance of the blockstate
 * @param staticSource double value of the voltage of the blockstate
 * @param dynamicLoad DynamicProperty providing resistance for the blockstate
 * @param dynamicSource DynamicSource providing voltage for the blockstate
 */
public record StateComponent(
	@NotNull StateConnector connector,
	double staticLoad,
	double staticSource,
	@NotNull DynamicProperty dynamicLoad,
	@NotNull DynamicProperty dynamicSource)
{	
	/** no-op StateComponent representing a componentless blockstate or invalid component */
	public static final StateComponent EMPTY = new StateComponent(
		StateConnector.EMPTY,
		0D,
		0D,
		NoneDynamicProperty.INSTANCE,
		NoneDynamicProperty.INSTANCE);
	
	/** {@return true if this is a real (non-empty) component} */
	public boolean isPresent()
	{
		return this != EMPTY;
	}
}
