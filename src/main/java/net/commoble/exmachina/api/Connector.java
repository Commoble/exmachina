package net.commoble.exmachina.api;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.internal.util.CodecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connectors are properties assigned to block instances that determine which
 * positions a block at a given position could potentially connect to in a circuit.
 * If blocks at two positions are both capable of connecting to each other's position,
 * then a circuit network can and will extend from one position to the other.
 * 
 * Subcodecs of Connector can be registered to {@link ExMachinaRegistries#CONNECTOR_TYPE}.
<pre>
{
	"type": "modid:registered_type_id",
	// the rest of the subtype fields
}
</pre>
 */
public interface Connector
{
	public static Codec<Connector> CODEC = CodecHelper.dispatch(ExMachinaRegistries.CONNECTOR_TYPE, Connector::codec);

	/**
	 * Bakes a BakedConnector function for the associated block. Caching and validation can be done.
	 * @param block Block this connector is associated with
	 * @return DataResult with success holding a BakedConnector if block is valid, error result otherwise
	 */
	public abstract DataResult<BlockConnector> bake(Block block);
	
	/**
	 * {@return Codec registered to {@link ExMachinaRegistries#CONNECTOR_TYPE}}.
	 */
	public MapCodec<? extends Connector> codec();
	
	/**
	 * Returns whether this is a valid Connector.
	 * If false, will be ignored by the graph builder.
	 * Consider using {@link CircuitComponent#empty()} instead of overriding.
	 * 
	 * @return Whether the component can be part of a circuit.
	 */
	default boolean isPresent()
	{
		return true;
	}

	@FunctionalInterface
	public interface BlockConnector
	{
		public static final BlockConnector EMPTY = state -> StateConnector.EMPTY;
		
		public abstract StateConnector getStateConnector(BlockState state);
		
		default boolean isPresent()
		{
			return this != EMPTY;
		}
	}
	
	@FunctionalInterface
	public interface StateConnector
	{
		public static final StateConnector EMPTY = StateConnector::noneOf;
		
		/**
		 * Retrieves the set of positions a block at the given position can connect to.
		 * @param level Level of the given block
		 * @param pos BlockPos of the given block
		 * @return Set<BlockPos> of the positions that block can connect to.
		 */
		public abstract Set<BlockPos> connectedPositions(LevelReader level, BlockPos pos);
		
		default boolean isPresent()
		{
			return this != EMPTY;
		}
		
		private static Set<BlockPos> noneOf(LevelReader level, BlockPos pos)
		{
			return Set.of();
		}
	}
}
