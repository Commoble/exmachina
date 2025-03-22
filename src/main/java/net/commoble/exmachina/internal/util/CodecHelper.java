package net.commoble.exmachina.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** codec utils */
public final class CodecHelper
{
	private CodecHelper() {}
	
	public static final StreamCodec<ByteBuf, BlockState> BLOCKSTATE_STREAM_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
	
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
		return Codec.lazyInitialized(() -> {
			// eclipsec and javac need to agree on the generics, so this might look strange
			Registry<?> uncastRegistry = BuiltInRegistries.REGISTRY.getValue(registryKey.location());
			Registry<MapCodec<? extends T>> registry = (Registry<MapCodec<? extends T>>) uncastRegistry;
			return registry.byNameCodec().dispatch(typeCodec, Function.identity());
		});
	}
	
	/**
	 * Creates a codec which encodes a Map as a list of pairs, averting unboundedMap's requirement that key codecs are string-serializable
	 * @param <K> Type of the map keys
	 * @param <V> Type of the map values
	 * @param keyCodec Key serializer
	 * @param valueCodec Value serializer
	 * @return Codec which encodes a Map as a list of pairs
	 * @implNote Maps/Lists created are mutable.
	 */
	public static <K,V> Codec<Map<K,V>> pairListMap(Codec<K> keyCodec, Codec<V> valueCodec)
	{
		return Codec.mapPair(keyCodec.fieldOf("key"), valueCodec.fieldOf("value"))
			.codec()
			.listOf()
			.xmap(list -> {
				Map<K,V> map = new HashMap<>();
				for (var entry : list)
				{
					map.put(entry.getFirst(), entry.getSecond());
				}
				return map;
			}, map -> {
				List<Pair<K,V>> list = new ArrayList<>();
				for (var entry : map.entrySet())
				{
					list.add(Pair.of(entry.getKey(), entry.getValue()));
				}
				return list;
			});
	}
	
	/**
	 * Creates a list codec that serializes as a single object when size == 1
	 * @param <T> Type of the thing to serialize
	 * @param codec Codec for the singular object
	 * @return Codec which serializes a list of the things, unless the list only has one thing in it, in which cases it serializes the single value
	 */
	public static <T> Codec<List<T>> singleOrPluralCodec(Codec<T> codec)
	{
		return Codec.either(codec.listOf(), codec).xmap(
				either -> either.map(Function.identity(), List::of), // map list/singleton to list
				list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)); // map list to list/singleton
	}
}
