package net.commoble.exmachina.api;

import java.util.Collection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
/**
 * <p>A MechanicalComponent is a mechanical graph component assignable to Blocks, which</p>
 * <ul>
 * <li>Can be connected to by the graph</li>
 * <li>Can connect to further graph nodes</li>
 * <li>Can be notified of mechanical power updates</li>
 * <li>Exists at a unique node (level + BlockPos + NodeShape) in any graph</li>
 * </ul>
 * 
 * <p>MechanicalComponents can be assigned to blocks via the exmachina:mechanical_component block datamap:</p>
 * <pre>
 * data/exmachina/data_maps/block/mechanical_component.json
 * [
 *   "namespace:blockid": {
 *     "type": "namespace:mechanical_component_type_id", // referring to a registered mechanical_component_type (a MechanicalComponent codec)
 *     // additional fields
 *   }
 * ]
 * </pre>
 */
public interface MechanicalComponent
{
	public static final Codec<MechanicalComponent> CODEC = CodecHelper.dispatch(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE, MechanicalComponent::codec);
	
	public abstract MapCodec<? extends MechanicalComponent> codec();
	
	/**
	 * Provides the MechanicalNodes at a given position
	 * @param levelKey ResourceKey identifying the level where this component is
	 * @param level BlockGetter where this component is
	 * @param pos BlockPos where this component is
	 * @param state BlockState which owns this component
	 * @return Collection of MechanicalNodes at the given position
	 */
	public abstract Collection<MechanicalNode> getNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state);
}
