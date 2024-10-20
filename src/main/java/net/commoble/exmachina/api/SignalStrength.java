package net.commoble.exmachina.api;

/**
 * Indicator of strong (conducting) vs weak (nonconducting) redstone power
 */
public enum SignalStrength
{
	/** Weak (non-conducting) redstone power */
	WEAK,
	/** Strong (conducting) redstone power */
	STRONG;

	/**
	 * Combines another SignalStrength with this one and returns the stronger of the two.
	 * @param that SignalStrength to compare to
	 * @return the stronger of these two signal strengths
	 */
	public SignalStrength max(SignalStrength that)
	{
		return this == STRONG || that == STRONG
			? STRONG
			: WEAK;
	}
}
