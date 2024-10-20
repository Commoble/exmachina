package net.commoble.exmachina.api;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <p>A SignalReceiver is a signal graph node assignable to Blocks, which</p>
 * <ul>
 * <li>Can be connected to by the graph</li>
 * <li>Will not connect to further graph nodes</li>
 * <li>Can be notified of signal power updates</li>
 * </ul>
 * <p>Unlike {@link SignalTransmitter}s, multiple SignalReceivers can exist at the same node position.
 * 
 * <p>SignalReceivers can be assigned to blocks via the exmachina:signal_receiver block datamap:</p>
 * <pre>
 * data/exmachina/data_maps/block/signal_receiver.json
 * [
 *   "namespace:blockid": {
 *     "type": "namespace:receiver_type_id",
 *     // additional fields
 *   }
 * ]
 * </pre>
 */
public interface SignalReceiver
{
	/** Master dispatch codec for SignalReceivers **/
	public static final Codec<SignalReceiver> CODEC = CodecHelper.dispatch(ExMachinaRegistries.SIGNAL_RECEIVER_TYPE, SignalReceiver::codec);

	/** {@return sub-codec for this receiver type registered via {@link ExMachinaRegistries#SIGNAL_RECEIVER_TYPE}} */
	public abstract MapCodec<? extends SignalReceiver> codec();
	
	/**
	 * Provides Receiver functions to the grapher at a given position
	 * @param level Level where the signal graph exists
	 * @param receiverPos BlockPos where this receiver's block exists
	 * @param receiverState BlockState of this receiver's block
	 * @param receiverSide Direction of the face of the receiverpos where this receiver exists, e.g. down -> the bottom of receiverPos
	 * @param connectedFace Face (pos+direction) of the graph node which is attempting to connect to this receiver
	 * @param channel Channel this receiver is attempting to be reached on
	 * @return Receiver function to invoke after the signal graph updates (or null if no receiver node exists for the given context)
	 */
	public abstract @Nullable Receiver getReceiverEndpoint(BlockGetter level, BlockPos receiverPos, BlockState receiverState, Direction receiverSide, Face connectedFace, Channel channel);
	
	/**
	 * Retrieves all Receiver instances returnable by {@link getReceiverEndpoint}
	 * @param level Level where the signal graph exists
	 * @param receiverPos BlockPos where this receiver's block exists
	 * @param receiverState BlockState of this receiver's block
	 * @param channel Channel this receiver is attempting to be reached on
	 * @return Collection of all receiver functions that can conceivably be returned by {@link getReceiverEndpoint}
	 */
	public abstract Collection<Receiver> getAllReceivers(BlockGetter level, BlockPos receiverPos, BlockState receiverState, Channel channel);
	
	/**
	 * This method is invoked on a SignalReceiver when a graph update occurs where a SignalReceiver block is
	 * but not all of its receivers were connected to a graph.
	 * 
	 * @param level Level where the signal graph exists
	 * @param pos BlockPos where this receiver's block exists
	 * @param receivers Collection of Receiver instances that are providable by the block at the given position,
	 * but were not part of any graph after a graph update occurs. This usually indicates that the receivers should be
	 * reset to signal power 0.
	 */
	public default void resetUnusedReceivers(LevelAccessor level, BlockPos pos, Collection<Receiver> receivers)
	{
		for (Receiver receiver : receivers)
		{
			receiver.accept(level, 0);
		}
	}
}
