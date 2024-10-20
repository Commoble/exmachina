package net.commoble.exmachina.api;

import org.jetbrains.annotations.NotNull;

import net.commoble.exmachina.api.Connector.BlockConnector;
import net.commoble.exmachina.api.StaticProperty.BakedStaticProperty;
import net.commoble.exmachina.api.content.NoneDynamicProperty;

/**
 * All of the components provided by a {@link CircuitComponent}, cached and validated for a Block.
 * @param connector BlockConnector for the block
 * @param staticLoad BakedStaticProperty for the block resistance
 * @param staticSource BakedStaticProperty for the block voltage
 * @param dynamicLoad DynamicProperty for the block resistance
 * @param dynamicSource DynamicProperty for the block voltage
 */
public record BlockComponent(
	@NotNull BlockConnector connector,
	@NotNull BakedStaticProperty staticLoad,
	@NotNull BakedStaticProperty staticSource,
	@NotNull DynamicProperty dynamicLoad,
	@NotNull DynamicProperty dynamicSource)
{
	/** BlockComponent for blocks which do not have assigned CircuitComponents or are otherwise invalid */
	public static final BlockComponent EMPTY = new BlockComponent(
		BlockConnector.EMPTY,
		BakedStaticProperty.EMPTY,
		BakedStaticProperty.EMPTY,
		NoneDynamicProperty.INSTANCE,
		NoneDynamicProperty.INSTANCE);
	
	/**
	 * {@return true if this is a real component (not empty/invalid)}
	 */
	public boolean isPresent()
	{
		return this != EMPTY;
	}
}
