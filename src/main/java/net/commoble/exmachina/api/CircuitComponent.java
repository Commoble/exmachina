package net.commoble.exmachina.api;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.content.ConstantProperty;
import net.commoble.exmachina.api.content.NoneConnector;
import net.commoble.exmachina.api.content.NoneDynamicProperty;

/**
 * CircuitComponents represent the circuit properties associated with particular Blocks.
 * Each block has at most one CircuitComponent loaded for it, from the exmachina/circuit_components data folder.
 * A circuit component can be loaded for a block by having a circuit component json with the same id as an existing block.
<pre>
{
	"connector": {}, // connector object, defaults to an invalid component if not present
	"static_load": 1.0, // double or static property object, defaults to 0 if not present
	"static_source": 1.0, // double or static property object, defaults to 0 if not present
	"dynamic_load": {}, // dynamic property object, optional
	"dynamic_source": {}, // dynamic property object, optional
}
</pre>
 * Datapacks can "disable" components by overriding a component json with an empty root object {}.
 * 
 * @param connector Connector interface to determine which blocks a block in the world can connect to
 * @param staticLoad StaticProperty designating the load for each of the block's blockstates
 * @param staticSource StaticProperty designating the power source quantity of each of the block's blockstates
 * @param dynamicLoad DynamicProperty designating the dynamic (worldpos-sensitive) load of the block
 * @param dynamicSource DynamicProperty designating the dynamic (worldpos-sensitive) source from the block
 */
public record CircuitComponent(
	@NotNull Connector connector,
	@NotNull StaticProperty staticLoad,
	@NotNull StaticProperty staticSource,
	@NotNull DynamicProperty dynamicLoad,
	@NotNull DynamicProperty dynamicSource)
{
	public static final Codec<CircuitComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Connector.CODEC.optionalFieldOf("connector", NoneConnector.INSTANCE).forGetter(CircuitComponent::connector),
			StaticProperty.CODEC.optionalFieldOf("static_load", ConstantProperty.zero()).forGetter(CircuitComponent::staticLoad),
			StaticProperty.CODEC.optionalFieldOf("static_source", ConstantProperty.zero()).forGetter(CircuitComponent::staticSource),
			DynamicProperty.CODEC.optionalFieldOf("dynamic_load", NoneDynamicProperty.INSTANCE).forGetter(CircuitComponent::dynamicLoad),
			DynamicProperty.CODEC.optionalFieldOf("dynamic_source", NoneDynamicProperty.INSTANCE).forGetter(CircuitComponent::dynamicSource)
		).apply(builder, CircuitComponent::new));
	
	/**
	 * {@return CircuitComponent representing empty/invalid components (equivalent to air blocks/items)}
	 */
	public CircuitComponent empty() { return EMPTY; }
	
	/**
	 * {@return Whether this is a valid component.}
	 */
	public boolean isPresent()
	{
		return this != EMPTY;
	}
	
	private static final CircuitComponent EMPTY = new CircuitComponent(
		NoneConnector.INSTANCE,
		ConstantProperty.zero(),
		ConstantProperty.zero(),
		NoneDynamicProperty.INSTANCE,
		NoneDynamicProperty.INSTANCE);
}
