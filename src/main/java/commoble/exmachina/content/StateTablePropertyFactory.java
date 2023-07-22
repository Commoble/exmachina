package commoble.exmachina.content;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.databuddy.codec.MapCodecHelper;
import commoble.exmachina.api.StaticProperty;
import commoble.exmachina.api.StaticPropertyFactory;
import commoble.exmachina.data.codec.VariantCodecHelper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class StateTablePropertyFactory implements StaticPropertyFactory
{
	private final Map<String, Double> variants; public Map<String, Double> getVariants() { return this.variants; }
	
	public StateTablePropertyFactory(Map<String, Double> variants)
	{
		this.variants = variants;
	}
	
	@Override
	public StaticProperty apply(Block block)
	{
		return BuiltinFunctions.getStateTableProperty(block, this.variants);
	}

	private static final Codec<Map<String, Double>> STATE_MAP_CODEC = MapCodecHelper.makeStringKeyedCodec(Codec.DOUBLE);
	public static final Codec<StateTablePropertyFactory> CODEC = makeStateTableCodec();
	private static Codec<StateTablePropertyFactory> makeStateTableCodec()
	{
		ResourceLocation typeID = new ResourceLocation("exmachina:blockstate");
		return RecordCodecBuilder.create(instance ->
			VariantCodecHelper.getTypeFieldedCodecBuilder(instance, typeID).and(
				instance.group(
					STATE_MAP_CODEC.fieldOf("variants").forGetter(StateTablePropertyFactory::getVariants)
				)
				.apply(instance, StateTablePropertyFactory::new)
			).apply(instance, (type,x) -> x)
		);
	}
}
