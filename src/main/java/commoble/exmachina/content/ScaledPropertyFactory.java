package commoble.exmachina.content;

import com.mojang.serialization.Codec;

import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.data.codec.VariantCodecHelper;
import net.minecraft.block.Block;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;

public class ScaledPropertyFactory implements StaticPropertyFactory
{
	private final String propertyName; public String getPropertyName() { return this.propertyName; }
	private final double scale; public double getScale() { return this.scale; }
	
	public ScaledPropertyFactory(String propertyName, double scale)
	{
		this.propertyName = propertyName;
		this.scale = scale;
	}

	@Override
	public StaticProperty apply(Block block)
	{
		Property<?> property = block.getStateContainer().getProperty(this.propertyName);
		if (property instanceof BooleanProperty)
		{
			return state -> state.get((BooleanProperty)property) ? this.scale : 0D;
		}
		else if (property instanceof IntegerProperty)
		{
			return state -> state.get((IntegerProperty)property) * this.scale;
		}
		else
		{
			return state -> 0D;
		}
	}

	public static final Codec<ScaledPropertyFactory> CODEC = VariantCodecHelper.makeVariantCodecWithExtraData(
		new ResourceLocation("exmachina:property"),
		instance -> instance.group(
			Codec.STRING.fieldOf("property").forGetter(ScaledPropertyFactory::getPropertyName),
			Codec.DOUBLE.fieldOf("scale").forGetter(ScaledPropertyFactory::getScale)
		).apply(instance, ScaledPropertyFactory::new));
}
