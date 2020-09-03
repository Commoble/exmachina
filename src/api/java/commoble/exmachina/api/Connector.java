package commoble.exmachina.api;

import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * Connectors are properties assigned to block instances that determine which
 * positions a block at a given position could potentially connect to in a circuit.
 * If blocks at two positions are both capable of connecting to each other's position,
 * then a circuit network can and will extend from one position to the other.
 */
@FunctionalInterface
public interface Connector
{
	public Set<BlockPos> apply(IWorld world, BlockPos pos, BlockState state);
}
