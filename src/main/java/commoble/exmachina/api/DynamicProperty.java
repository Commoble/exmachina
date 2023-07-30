package commoble.exmachina.api;

import com.mojang.serialization.Codec;

import commoble.exmachina.api.content.NoneDynamicProperty;
import commoble.exmachina.util.CodecHelper;
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
 * Subcodecs of DynamicProperty can be registered to {@link ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE}.
<pre>
{
	"type": "modid:registered_type_id",
	// the rest of the subtype fields
}
</pre>
 */
public interface DynamicProperty
{
	public static final Codec<DynamicProperty> CODEC = CodecHelper.dispatch(ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE, DynamicProperty::codec);
	
	/**
	 * @param level LevelReader to read level data with.
	 * @param pos
	 * @param state
	 * @return double value of this property given the provided context.
	 */
	public double getValue(LevelReader level, BlockPos pos, BlockState state);
	
	/**
	 * {@return Codec registered to {@link ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE}.}
	 */
	public Codec<? extends DynamicProperty> codec();
	
	/**
	 * {@return boolean indicating whether this property exists.
	 * If false, dynamic values will not be checked for the associated block.
	 * Consider using {@link NoneDynamicProperty.INSTANCE} instead of overriding this.
	 */
	default boolean isPresent()
	{
		return true;
	}
}
