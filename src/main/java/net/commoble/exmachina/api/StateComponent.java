package net.commoble.exmachina.api;

import org.jetbrains.annotations.NotNull;

import net.commoble.exmachina.api.Connector.StateConnector;
import net.commoble.exmachina.api.content.NoneDynamicProperty;

/**
 * A CircuitComponent, but baked for a particular BlockState
 */
public record StateComponent(
	@NotNull StateConnector connector,
	double staticLoad,
	double staticSource,
	@NotNull DynamicProperty dynamicLoad,
	@NotNull DynamicProperty dynamicSource)
{	
	public static final StateComponent EMPTY = new StateComponent(
		StateConnector.EMPTY,
		0D,
		0D,
		NoneDynamicProperty.INSTANCE,
		NoneDynamicProperty.INSTANCE);
	
	public boolean isPresent()
	{
		return this != EMPTY;
	}
}
