package net.commoble.exmachina.api;

import net.minecraft.world.level.LevelAccessor;

/**
 * Function which is invoked for {@link SignalReceiver}s in a signal graph after a signal graph update occurs.
 */
public interface Receiver
{
	/**
	 * Listener method invoked when a signal graph update which includes the SignalReceiver node.
	 * @param level Level in which the signal graph updated
	 * @param power The new signal power level assigned to the receiver node in the range [0,15]
	 */
	public abstract void accept(LevelAccessor level, int power);
	
	/**
	 * {@return NodeShape of this receiver}
	 */
	public abstract NodeShape nodeShape();
}
