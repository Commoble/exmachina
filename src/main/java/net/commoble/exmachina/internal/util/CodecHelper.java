package net.commoble.exmachina.internal.util;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

public class CodecHelper
{
	/**
	 * Helper to make a dispatch codec for a custom forge registry of codecs.
	 * @param <T> Type of the element to be loaded from json
	 * @param registryKey ResourceKey for a Registry of sub-codecs.
	 * @param typeCodec Function to retrieve the codec for a given json-loaded element
	 * @return dispatch codec to load instances of T
	 */
	@SuppressWarnings("unchecked")
	public static <T> Codec<T> dispatch(ResourceKey<Registry<MapCodec<? extends T>>> registryKey, Function<? super T, ? extends MapCodec<? extends T>> typeCodec)
	{
		return Codec.lazyInitialized(() -> 
			((Registry<MapCodec<? extends T>>)BuiltInRegistries.REGISTRY.get(registryKey))
			.byNameCodec()
			.dispatch(typeCodec, mapCodec -> mapCodec));
	}
}
