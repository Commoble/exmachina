package net.commoble.exmachina.api.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.StaticProperty;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * StaticProperty representing a constant value for all blockstates.
<pre>
{
	"type": "exmachina:constant",
	"value": 5.0
}
</pre>
 * @param value double value for the block property
 */
public record ConstantProperty(double value) implements StaticProperty
{
	/** exmachina:constant_property_type / exmachina:constant */
	public static final ResourceKey<MapCodec<? extends StaticProperty>> KEY = ResourceKey.create(ExMachinaRegistries.STATIC_PROPERTY_TYPE, ExMachina.id("constant"));
	
	/**
<pre>
{
	"type": "exmachina:constant",
	"value": 5.0
}
</pre>
	 */
	public static final MapCodec<ConstantProperty> CODEC = Codec.DOUBLE.fieldOf("value").xmap(ConstantProperty::of, ConstantProperty::value);

	private static final ConstantProperty ZERO = new ConstantProperty(0D);
	
	/**
	 * {@return ConstantProperty with value 0}
	 */
	public static ConstantProperty zero()
	{
		return ZERO;
	}

	/**
	 * {@return ConstantProperty with the given value}
	 * @param value double value of a ConstantProperty to return
	 */
	public static ConstantProperty of(double value)
	{
		return value == 0D ? ZERO : new ConstantProperty(value);
	}
	
	@Override
	public DataResult<BakedStaticProperty> bake(Block block)
	{
		return DataResult.success(this::getValue);
	}
	
	private double getValue(BlockState state)
	{
		return this.value;
	}
	
	@Override
	public boolean isPresent()
	{
		return this.value != 0D;
	}

	@Override
	public MapCodec<? extends StaticProperty> codec()
	{
		return CODEC;
	}
}
