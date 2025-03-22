package net.commoble.exmachina.api.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.MechanicalBlockComponent;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

/**
 * MechanicalComponent which provides no power and connects to no graph.
 * 
<pre>
{
	"type": "exmachina:none"
}
</pre>
 */
public enum NoneMechanicalComponent implements MechanicalComponent
{
	INSTANCE;
	
	/** exmachina:mechanical_component_type / exmachina:none */
	public static final ResourceKey<MapCodec<? extends MechanicalComponent>> KEY = ResourceKey.create(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE, ExMachina.id("none"));
	
	/** <pre>{"type": "exmachina:none"}</pre> */
	public static final MapCodec<? extends MechanicalComponent> CODEC = MapCodec.unit(INSTANCE);
	
	@Override
	public MapCodec<? extends MechanicalComponent> codec()
	{
		return CODEC;
	}

	@Override
	public DataResult<MechanicalBlockComponent> bake(Block block, RegistryAccess registries)
	{
		return DataResult.success(MechanicalBlockComponent.EMPTY);
	}
}
