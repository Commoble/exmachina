package net.commoble.exmachina.internal.util;

import org.apache.commons.lang3.math.Fraction;

/**
 * Extra math utils
 */
public final class Maths
{
	private Maths() {}
	
	/**
	 * Attempts to multiply fractions, returning a fallback in case of overflow exception
	 * @param a Fraction to multiply by the other fraction
	 * @param b Fraction to multiply the other fraction by
	 * @param fallback Fraction to return if the resulting fraction's numerator or denominator would exceed Integer.MAX_VALUE
	 * @return Fraction returned by a.multiplyBy(b) if possible, or the fallback if an overflow would occur
	 */
	public static Fraction safeMultiplyFraction(Fraction a, Fraction b, Fraction fallback)
	{
		try
		{
			return a.multiplyBy(b);
		}
		catch(ArithmeticException e)
		{
			return fallback;
		}
	}
}
