package commoble.exmachina.api;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface CircuitComponent
{
	/**
	 * Returns the independant resistance of a component block given a world context.
	 * @param world
	 * @param pos
	 * @param state
	 * @return The load of a component block in ohms (voltage per current, or power / current^2, or joule-seconds per square charge, etc)
	 */
	public double getLoad(IWorld world, BlockState state, BlockPos pos);
	
	/**
	 * Returns the independant source voltage of a component block given a world context.
	 * To find the voltage across a purely resistive component, call getLoad instead to get its resistance,
	 * and query the current from a circuit (volts = current * resistance)
	 * If a component has both load and voltage defined, they are independant of each other in the circuit model.
	 * Power queries from circuits return the sum of the power supplied to and drawn from such components.
	 * The separate power supplied by and drawn from such componenents can be determined by querying the current
	 * from a circuit the component belongs to and doing the math separately.
	 * Note that this returns the nominal voltage of an element -- the actual voltage the element is supplying
	 * to a given circuit can be found by multiplying this value by the circuit's efficiency value
	 * @param world
	 * @param pos
	 * @param state
	 * @return The voltage of a current block in volts (energy per charge, or current * resistance, or power / current, etc)
	 */
	public double getSource(IWorld world, BlockState state, BlockPos pos);
	
	/**
	 * Returns a function that can be used to determine which blocks the block at a given position can connect to.
	 * @return
	 */
	@Nonnull
	public Connector getConnector();
}
