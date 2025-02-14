package net.commoble.exmachina.api;

import java.util.Collection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;


/**
 * <p>A SignalComponent is a signal graph component assignable to Blocks, which</p>
 * <ul>
 * <li>Can be connected to by the graph</li>
 * <li>Can connect to further graph nodes</li>
 * <li>Can be notified of signal power updates</li>
 * <li>Can read redstone power from non-graph blocks adjacent to the graph</li>
 * <li>Exists at a unique node (BlockPos + Direction + NodeShape + Channel) in any graph</li>
 * </ul>
 * 
 * <p>SignalComponents can be assigned to blocks via the exmachina:signal_component block datamap:</p>
 * <pre>
 * data/exmachina/data_maps/block/signal_component.json
 * [
 *   "namespace:blockid": {
 *     "type": "namespace:signal_component_type_id", // referring to a registered signal_component_type (a SignalComponent codec)
 *     // additional fields
 *   }
 * ]
 * </pre>
 */
public interface SignalComponent
{
	/** Master dispatch codec for SignalComponents */
	public static final Codec<SignalComponent> CODEC = CodecHelper.dispatch(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE, SignalComponent::codec);
	
	/** 
	 * Returns the sub-codec for this component type registered via {@link ExMachinaRegistries#SIGNAL_COMPONENT_TYPE}
	 * @return sub-codec 
	 */
	public abstract MapCodec<? extends SignalComponent> codec();
	
	/**
	 * Defines the TransmissionNode at a given pos/face/channel
	 * @param levelKey ResourceKey of the blockgetter
	 * @param level BlockGetter where the SignalComponent block is
	 * @param pos BlockPos where this SignalComponent block is
	 * @param state BlockState of this SignalComponent block
	 * @param channel Channel to get nodes for
	 * @return Collection of TransmissionNodes for the provided context. Any transmission nodes at the same level+pos+channel must have a unique NodeShape.
	 */
	public abstract Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel);
	
	/**
	 * If true, neighborChanged will be invoked on this block after a graph update occurs (default false)
	 * @param level LevelReader where the block is
	 * @param state BlockState of the block
	 * @param pos BlockPos where the block is 
	 * @return boolean indicating that the block should receive neighbor updates after a graph update if true
	 */
	public default boolean updateSelfFromNeighborsAfterGraphUpdate(LevelReader level, BlockState state, BlockPos pos) {
		return false;
	}
}
