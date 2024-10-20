package net.commoble.exmachina.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.Util;
import net.minecraft.world.item.DyeColor;

/**
 * <p>Channels are used by the signal graph to keep signals separate from each other.</p>
 * 
 * <p>There are seventeen channels: one for each dye color, plus the redstone channel.</p>
 * 
 * <p>The redstone channel can connect to any channel, while the color channels can only connect
 * to the same channel or to redstone.</p>
 */
public sealed interface Channel permits Channel.Redstone, Channel.Single
{
	/** Codec which parses a string at a channel, can be "redstone" or any {@link DyeColor}, e.g. "white", "orange" */
	public static final Codec<Channel> CODEC = Codec.STRING.comapFlatMap(Channel::parse, Channel::toString);
	
	/**
	 * {@return Set of Channels this channel can connect to}
	 */
	public abstract Set<Channel> getConnectableChannels();
	
	/** {@return the Redstone channel} */
	public static Channel redstone()
	{
		return Redstone.INSTANCE;
	}
	
	/**
	 * Retrieves the color channel for a given DyeColor.
	 * @param color DyeColor of a channel to retrieve
	 * @return the Channel for the given DyeColor
	 */
	public static Channel single(DyeColor color)
	{
		return Single.SIXTEEN_COLORS_BY_COLOR[color.ordinal()];
	}
	
	/** Set of the sixteen color-channels **/
	public static final Set<Channel> SIXTEEN_COLORS = Set.of(Single.SIXTEEN_COLORS_BY_COLOR);
	
	/** Set of all channels including the sixteen color channels and redstone */
	public static final Set<Channel> ALL = Set.of(Util.make(new Channel[17], channels -> {
		for (DyeColor color : DyeColor.values())
		{
			channels[color.ordinal()] = Single.SIXTEEN_COLORS_BY_COLOR[color.ordinal()];
		}
		channels[16] = Redstone.INSTANCE;
	}));
	
	/**
	 * List of each of the color channels' connectable sets, indexed by color ordinal
	 * e.g. [{redstone,white}, {redstone,orange}, etc]
	 */
	public static final List<Set<Channel>> EXPANDED_SINGLES = Util.make(() -> {
		List<Set<Channel>> channels = new ArrayList<>(); 
		for (int i=0; i<16; i++)
		{
			channels.add(Set.of(Redstone.INSTANCE, Single.SIXTEEN_COLORS_BY_COLOR[i]));
		}
		return channels;
	});
	
	/**
	 * Attempts to parse a Channel as a String
	 * @param s String representation of a channel, e.g. "redstone", "white", "orange"
	 * @return Success result if string is parsable as a channel, error result otherwise
	 */
	public static DataResult<Channel> parse(String s)
	{
		if ("redstone".equals(s))
		{
			return DataResult.success(redstone());
		}
		@Nullable DyeColor color = DyeColor.byName(s, null);
		if (color != null)
		{
			return DataResult.success(single(color));
		}
		return DataResult.error(() -> String.format("Invalid signal channel: %s", s));
	}
	
	/**
	 * The Redstone Channel
	 */
	public static enum Redstone implements Channel
	{
		/** The Redstone Channel Instance */
		INSTANCE;
		
		public Set<Channel> getConnectableChannels()
		{
			return ALL;
		}

		@Override
		public String toString()
		{
			return "redstone";
		}
	}
	
	/**
	 * Single channels represent one of the sixteen-color digital channels
	 * @param color DyeColor of this channel
	 */
	public static record Single(DyeColor color) implements Channel
	{
		private static final Channel[] SIXTEEN_COLORS_BY_COLOR = Util.make(new Channel[16], channels -> {
			for (DyeColor color : DyeColor.values())
			{
				channels[color.ordinal()] = new Single(color);
			}
		});
		
		public Set<Channel> getConnectableChannels()
		{
			return EXPANDED_SINGLES.get(color.ordinal());
		}

		@Override
		public String toString()
		{
			return color.getSerializedName();
		}
	}
}
