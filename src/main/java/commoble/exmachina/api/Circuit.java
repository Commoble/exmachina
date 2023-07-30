package commoble.exmachina.api;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import commoble.exmachina.circuit.EmptyCircuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface Circuit
{
	/**
	 * {@return Empty/invalid circuit}
	 */
	public static Circuit empty() { return EmptyCircuit.INSTANCE; }
	
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
	
	/**
	 * {@return Whether this circuit has any valid components.}
	 */
	public boolean isPresent();
	
	/**
	 * Returns the set of blockpositions this circuit occupies and the states that existed there
	 * when the circuit was built
	 * @return
	 */
	public Map<BlockPos, Pair<BlockState, StateComponent>> components();
}
