package commoble.exmachina.api;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface Circuit
{
	/**
	 * Returns the power in energy/second this Circuit is supplying to a given position.
	 * This is positive for power consumers and negative for power sources.
	 * If a position is both consuming and supplying power, the returned value
	 * is positive if it is consuming more power than it is supplying.
	 * @param pos
	 * @return The power supplied to a position, or 0 if the circuit is
	 * invalid or does not exist at the given position
	 */
	public double getPowerSuppliedTo(BlockPos pos);
	
	/**
	 * Returns the current running through the circuit (in charge/second).
	 * Voltage across a block can be determined via voltage = power / current
	 * @return The current through the circuit, or 0 if the circuit is invalid.
	 */
	public double getCurrent();
	
	/**
	 * Mark a circuit as needing to read data from its dynamic components.
	 * If your component block has a dynamic source or load, this should be called
	 * when the value updates to notify the circuit that it has to redo some math.
	 */
	public void markNeedingDynamicUpdate();
	
//	/**
//	 * Returns whether the circuit is valid.
//	 * A circuit becomes invalidated when its component blocks are added to, removed,
//	 * or undergo a change in blockstate.
//	 * An invalid circuit will be returned by the circuit manager if no circuit exists at the queried position.
//	 * Invalid circuits return 0 power and current in the relevant methods.
//	 * @return
//	 */
//	public boolean isValid();
//	
//	/**
//	 * Mark the circuit as invalid. It will no longer query blocks for power
//	 * and a new circuit will attempt to be built the next time the circuit manager
//	 * is queried for a position this circuit formerly occupied.
//	 * @return
//	 */
//	public Circuit invalidate();
	
	/**
	 * Returns the set of blockpositions this circuit occupies and the states that existed there
	 * when the circuit was built
	 * @return
	 */
	public Map<BlockPos, ? extends Pair<BlockState, ? extends CircuitComponent>> getComponentCache();
}
