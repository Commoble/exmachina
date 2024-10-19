/**
The MIT License (MIT)

Copyright (c) Joseph "Commoble" Bettendorff 2020

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
**/
package commoble.exmachina.util;

/**
 * Util for making very large or small values more readable
 * @author Joseph Bettendorff aka Commoble
 *
 */
public class EngineeringNotation
{
	/**
	 * Given a value, returns a string representation of that value in an SI unit
	 * where the non-fractional value of the unit >= 1 and < 1000
	 * i.e. 1200 Watts -> 1.2 kiloWatts
	 * 20560000 Ohms -> 20.56 megaOhms
	 * 0.0042 amps -> 4.2 milliAmps
	 * @param value	The value in baseUnits
	 * @param baseUnits "Ohms", "meters", "seconds", etc
	 * @param significantDigits how many digits to round after (1234, meters, 3 -> 1.23 kilometers)
	 * @return
	 */
	public static String toSIUnit(double value, String baseUnits, int significantDigits)
	{
		int magnitude = 0;
		String sign = value < 0 ? "-" : "";
		value = Math.abs(value);
		while (value < 1D && magnitude > -9)
		{
			magnitude--;
			value *= 1000;
		}
		while (value >= 1000D && magnitude < 9)
		{
			magnitude++;
			value *= 0.001;
		}
		if (magnitude >= 9)
		{
			return sign + "∞" + baseUnits;
		}
		else
		{
			if (magnitude <= -9) value = 0D;	// if magnitude is really small return e.g. "0V"
			String scalePrefix = getScalePrefix(magnitude);
			String formatString = "%." + significantDigits + "f ";
			return sign + String.format(formatString, value) + scalePrefix + baseUnits;
		}
	}
	
	/** as above but with 3 significant digits as the default **/
	public static String toSIUnit(double value, String baseUnits)
	{
		return toSIUnit(value, baseUnits, 4);
	}
	
	public static String getScalePrefix(int magnitude)
	{
		switch(magnitude)
		{
			case 9:
				return "";
			case 8:
				return "Y";
			case 7:
				return "Z";
			case 6:
				return "E";
			case 5:
				return "P";
			case 4:
				return "T";
			case 3:
				return "G";
			case 2:
				return "M";
			case 1:
				return "k";
			case 0:
				return "";
			case -1:
				return "m";
			case -2:
				return "μ";
			case -3:
				return "n";
			case -4:
				return "p";
			case -5:
				return "f";
			case -6:
				return "a";
			case -7:
				return "z";
			case -8:
				return "y";
			case -9:
				return "";
			default:
				return "";
		}
	}
}