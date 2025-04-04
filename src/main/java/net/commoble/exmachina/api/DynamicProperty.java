package net.commoble.exmachina.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.content.NoneDynamicProperty;
import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dynamic properties are assigned to blocks as part of their circuit element definition.
 * They allow the existence of blocks whose power characteristics change somewhat frequently
 * and in a manner unrelated to their blockstates (e.g. a block entity whose power output
 * depends on its inventory).
 * 
 * If the value of a dynamic property of a block in the world changes, it must notify the
 * circuit manager capability of a dynamic update.
 * 
 * Subcodecs of DynamicProperty can be registered to {@link ExMachinaRegistries#DYNAMIC_PROPERTY_TYPE}.
<pre>
{
	"type": "modid:registered_type_id",
	// the rest of the subtype fields
}
</pre>
 */
public interface DynamicProperty
{
	/** Master dispatch codec for DynamicProperty(s); subcodecs can be registered to {@link ExMachinaRegistries#DYNAMIC_PROPERTY_TYPE} */
	public static final Codec<DynamicProperty> CODEC = CodecHelper.dispatch(ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE, DynamicProperty::codec);
	
	/**
	 * {@return double value of this property given the provided context.}
	 * @param level LevelReader to read level data with.
	 * @param pos BlockPos where the value is being read at
	 * @param state BlockState at the position where the value is being read at
	 */
	public double getValue(LevelReader level, BlockPos pos, BlockState state);
	
	/**
	 * {@return Codec registered to {@link ExMachinaRegistries#DYNAMIC_PROPERTY_TYPE}.}
	 */
	public MapCodec<? extends DynamicProperty> codec();
	
	/**
	 * {@return boolean indicating whether this property exists.
	 * If false, dynamic values will not be checked for the associated block.
	 * Consider using {@link NoneDynamicProperty#INSTANCE} instead of overriding this.}
	 */
	default boolean isPresent()
	{
		return true;
	}
}
