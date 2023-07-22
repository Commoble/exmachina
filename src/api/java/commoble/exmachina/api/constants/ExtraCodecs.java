package commoble.exmachina.api.constants;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public final class ExtraCodecs
{
	public static final Codec<Direction> DIRECTION = IStringSerializable.createCodec(() -> Direction.values(), Direction::byName);
	public static final Codec<List<Direction>> DIRECTION_LIST = DIRECTION.listOf();
	public static final Codec<Direction[]> DIRECTION_ARRAY = DIRECTION_LIST.xmap(list -> list.toArray(new Direction[list.size()]), array -> Arrays.asList(array));
}
