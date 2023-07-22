package commoble.exmachina.data.codec;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.exmachina.api.ConnectorFactory;
import commoble.exmachina.content.BuiltinFunctions;
import net.minecraft.util.ResourceLocation;

public class VariantCodecHelper
{
	
	public static Codec<ConnectorFactory> makeAllDirectionsCodec()
	{
		return makeSimpleVariantCodec(new ResourceLocation("exmachina:all_directions"), block -> BuiltinFunctions::getAllDirectionsConnectionSet);
	}
	
	public static Codec<ConnectorFactory> makeNoDirectionsCodec()
	{
		return makeSimpleVariantCodec(new ResourceLocation("exmachina:no_directions"), block -> (world,pos,state) -> ImmutableSet.of());
	}
	
	public static <T> Codec<T> makeSimpleVariantCodec(ResourceLocation typeID, T factory)
	{
		return RecordCodecBuilder.create(instance ->
			getTypeFieldedCodecBuilder(instance, typeID)
			.apply(instance, id -> factory));
	}
	
	public static <T> Codec<T> makeNoopVariantCodec(T factory)
	{
		return RecordCodecBuilder.create(instance -> instance.point(factory));
	}
	
//	public static <T> P1<Mu<T>, ResourceLocation> getTypeFieldedCodecBuilder(RecordCodecBuilder.Instance<T> instance, ResourceLocation typeID)
//	{
//		return instance.group(ResourceLocation.CODEC.fieldOf("type").forGetter(x -> typeID));
//	}
	
//	public static <T> Codec<T> makeVariantCodecWithExtraData(ResourceLocation typeID, Function<RecordCodecBuilder.Instance<T>, App<Mu<T>,T>> dataBuilder)
//	{
//		return RecordCodecBuilder.create(instance -> 
//			VariantCodecHelper.getTypeFieldedCodecBuilder(instance, typeID)
//			.and(dataBuilder.apply(instance))
//			.apply(instance, (type, x) -> x)
//		);
//	}
}
