package commoble.exmachina.content;

import com.mojang.serialization.Codec;

import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.data.codec.VariantCodecHelper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class ConstantPropertyFactory implements StaticPropertyFactory
{
	private final double value; public double getValue() {return this.value;}
	
	public ConstantPropertyFactory(double value)
	{
		this.value = value;
	}

	@Override
	public StaticProperty apply(Block block)
	{
		return state -> this.value;
	}
	
	public static final Codec<ConstantPropertyFactory> CODEC = VariantCodecHelper.makeVariantCodecWithExtraData(
		new ResourceLocation("exmachina:constant"),
		instance -> instance.group(
			Codec.DOUBLE.optionalFieldOf("value",0D).forGetter(ConstantPropertyFactory::getValue)
		)
		.apply(instance, ConstantPropertyFactory::new));
}
