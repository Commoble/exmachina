package net.commoble.exmachina.api;

import java.util.Map;
import java.util.function.ToIntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * <p>A SignalSource is a signal graph node assignable to Blocks, which</p>
 * <ul>
 * <li>Can be connected to by the graph</li>
 * <li>Will not connect to further graph nodes</li>
 * <li>Can provide signal power to the graph</li>
 * </ul>
 * <p>SignalSources can be assigned to blocks using the exmachina:signal_source block data map:</p>
 * 
 * <pre>
 * data/exmachina/data_maps/block/signal_source.json
 * [
 *   "namespace:blockid": {
 *     "type": "namespace:receiver_type_id",
 *     // additional fields
 *   }
 * ]
 * </pre>
 */
public interface SignalSource
{
	/** Master dispatch codec for SignalSources */
	public static final Codec<SignalSource> CODEC = CodecHelper.dispatch(ExMachinaRegistries.SIGNAL_SOURCE_TYPE, SignalSource::codec);

	/** {@return sub-codec for this source type registered via {@link ExMachinaRegistries#SIGNAL_SOURCE_TYPE}} */
	public abstract MapCodec<? extends SignalSource> codec();
	
	/**
	 * Describes how much redstone signal power is provided on each channel relevant to this SignalSource for the given context
	 * @param level Level where the signal graph exists
	 * @param supplierPos BlockPos of this SignalSource's block
	 * @param supplierState BlockState of this SignalSource's block
	 * @param supplierSide Direction internal to this SignalSource's position (e.g. down -> the bottom of the supplierPos)
	 * @param connectedFace Face (BlockPos + Direction) of the node attempting to connect to this SignalSource
	 * @return Map of Channel to LevelReader-> int function which provides signal power in range [0,15] on the given channel(s)
	 */
	public abstract Map<Channel, ToIntFunction<LevelReader>> getSupplierEndpoints(BlockGetter level, BlockPos supplierPos, BlockState supplierState, Direction supplierSide, Face connectedFace);
}
