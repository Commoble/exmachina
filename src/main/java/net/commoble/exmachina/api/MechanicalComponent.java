package net.commoble.exmachina.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Block;
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
	/**
	 * <pre>
	 * {
	 *   "type": "namespace:subcodec",
	 *   // additional fields specified by subcodec
	 * }
	 * </pre>
	 */
	public static final Codec<MechanicalComponent> CODEC = CodecHelper.dispatch(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE, MechanicalComponent::codec);
	
	/**
	 * {@return registerd MapCodec for this subclass}
	 */
	public abstract MapCodec<? extends MechanicalComponent> codec();
	
	/**
	 * Validates and bakes a MechanicalBlockComponent for this component's associated block.
	 * Invoked on server start.
	 * @param block Block which this MechanicalComponent shares an id with
	 * @param registries RegistryAccess containing the datapack registries, if needed
	 * @return DataResult containing the baked and validated block component, or an error result if validation failed
	 */
	public abstract DataResult<MechanicalBlockComponent> bake(Block block, RegistryAccess registries);
}
