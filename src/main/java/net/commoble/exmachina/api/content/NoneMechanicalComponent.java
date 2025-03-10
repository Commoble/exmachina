package net.commoble.exmachina.api.content;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.MechanicalNode;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
	public Collection<MechanicalNode> getNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state)
	{
		return List.of();
	}

}
