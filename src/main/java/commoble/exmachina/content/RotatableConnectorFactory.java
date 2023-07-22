package commoble.exmachina.content;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.exmachina.api.Connector;
import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.api.constants.ExtraCodecs;
import commoble.exmachina.data.codec.VariantCodecHelper;
import net.minecraft.block.Block;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class RotatableConnectorFactory implements ConnectorFactory
{
	private final Direction[] nominalDirections; public Direction[] getNominalDirections() { return this.nominalDirections; }
	private final Optional<String> directionProperty; public Optional<String> getDirectionProperty() { return this.directionProperty; }
	private final Direction unrotatedDirection; public Direction getUnrotatedDirection() { return this.unrotatedDirection; }
	
	public RotatableConnectorFactory(Direction[] values, Optional<String> directionProperty, Direction unrotatedDirection)
	{
		this.nominalDirections = values;
		this.directionProperty = directionProperty;
		this.unrotatedDirection = unrotatedDirection;
	}
	
	@Override
	public Connector apply(Block block)
	{
		return this.directionProperty.map(name -> this.getRotatedConnector(name, block)).orElse(this.getUnrotatedConnector());
	}
	
	private Connector getUnrotatedConnector()
	{
		return (world,pos,state) -> BuiltinFunctions.getAdjacentPositions(pos, this.nominalDirections);
	}
	
	private Connector getRotatedConnector(String propertyName, Block block)
	{
		Property<?> property = block.getStateContainer().getProperty(propertyName);
		if (property == null || !(property instanceof DirectionProperty))
		{
			return this.getUnrotatedConnector();
		}
		DirectionProperty directionProperty = (DirectionProperty)property;
		Direction defaultStateFacing = this.unrotatedDirection == null ? block.getDefaultState().get(directionProperty) : this.unrotatedDirection;
		return (world,pos,state) -> BuiltinFunctions.getRotatedPositions(pos, state, directionProperty, defaultStateFacing, this.nominalDirections);
	}
	
	public static final Codec<RotatableConnectorFactory> CODEC = makeRotatableDirectionsCodec();
	private static Codec<RotatableConnectorFactory> makeRotatableDirectionsCodec()
	{
		ResourceLocation typeID = new ResourceLocation("exmachina:directions");
		return RecordCodecBuilder.create(instance -> 
			VariantCodecHelper.getTypeFieldedCodecBuilder(instance, typeID).and(
				instance.group(
					ExtraCodecs.DIRECTION_ARRAY.optionalFieldOf("values", new Direction[0]).forGetter(RotatableConnectorFactory::getNominalDirections),
					Codec.STRING.optionalFieldOf("direction_property").forGetter(RotatableConnectorFactory::getDirectionProperty),
					ExtraCodecs.DIRECTION.optionalFieldOf("unrotated_direction", Direction.NORTH).forGetter(RotatableConnectorFactory::getUnrotatedDirection)
				).apply(instance, RotatableConnectorFactory::new)
			).apply(instance, (type,x) -> x)
		);
	}
	
}