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
 * <p>A SignalTransmitter is a signal graph node assignable to Blocks, which</p>
 * <ul>
 * <li>Can be connected to by the graph</li>
 * <li>Can connect to further graph nodes</li>
 * <li>Can be notified of signal power updates</li>
 * <li>Can read redstone power from non-graph blocks adjacent to the graph</li>
 * <li>Exists at a unique node (BlockPos + Direction + Channel) in any graph</li>
 * </ul>
 * 
 * <p>SignalTransmitters can be assigned to blocks via the exmachina:signal_transmitter block datamap:</p>
 * <pre>
 * data/exmachina/data_maps/block/signal_transmitter.json
 * [
 *   "namespace:blockid": {
 *     "type": "namespace:transmitter_type_id",
 *     // additional fields
 *   }
 * ]
 * </pre>
 */
public interface SignalTransmitter
{
	/** Master dispatch codec for SignalTransmitters */
	public static final Codec<SignalTransmitter> CODEC = CodecHelper.dispatch(ExMachinaRegistries.SIGNAL_TRANSMITTER_TYPE, SignalTransmitter::codec);
	
	/** 
	 * Returns the sub-codec for this transmitter type registered via {@link ExMachinaRegistries#SIGNAL_TRANSMITTER_TYPE}
	 * @return sub-codec 
	 */
	public abstract MapCodec<? extends SignalTransmitter> codec();
	
	/**
	 * Defines the TransmissionNode at a given pos/face/channel
	 * @param levelKey ResourceKey of the blockgetter
	 * @param level BlockGetter where the transmitter is
	 * @param pos BlockPos where this SignalTransmitter is
	 * @param state BlockState of this SignalTransmitter's block
	 * @param channel Channel to get nodes for
	 * @return Map describing the TransmissionNode on a given channel for the given context
	 */
	public abstract Collection<TransmissionNode> getTransmissionNodes(ResourceKey<Level> levelKey, BlockGetter level, BlockPos pos, BlockState state, Channel channel);
}
