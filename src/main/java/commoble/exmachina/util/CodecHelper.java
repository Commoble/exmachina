package commoble.exmachina.util;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.GameData;

public class CodecHelper
{
	/**
	 * Helper to make a dispatch codec for a custom forge registry of codecs.
	 * @param <T> Type of the element to be loaded from json
	 * @param registryKey ResourceKey for a Registry of sub-codecs. Must be a custom forge registry with hasTags() enabled.
	 * @param typeCodec Function<T,Codec> to retrieve the codec for a given json-loaded element
	 * @return dispatch codec to load instances of T
	 */
	public static <T> Codec<T> dispatch(ResourceKey<Registry<Codec<? extends T>>> registryKey, Function<T, Codec<? extends T>> typeCodec)
	{
		return ExtraCodecs.lazyInitializedCodec(() ->
			GameData.getWrapper(registryKey, Lifecycle.stable())
				.byNameCodec()
				.dispatch(typeCodec, Function.identity()));
	}
}
