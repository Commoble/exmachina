package net.commoble.exmachina.api;

import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * Representation of relative direction of rotation between mechanical nodes
 */
public enum Parity implements StringRepresentable
{
	/** Indicates no rotation is occurring **/
	ZERO(0, "zero"),
	/**
	 * Indicates a node's rotation relative to its axis is the same as another node's relative to that node's axis;
	 * e.g. two nodes are rotating in the same direction on positive axes, or the same direction on negative axes,
	 * or two nodes are rotating about opposite directions, one on a positive axis and one on a negative axis
	 **/
	POSITIVE(1, "positive"),
	/**
	 * Indicates a node's rotation relative to its axis is the opposite of another node's relative to that node's axis;
	 * e.g. two nodes are rotating in opposite directions about positive axes, or opposite directions on negative axis,
	 * or the same direction, one on a positive axis and one on a negative axis
	 */
	NEGATIVE(-1, "negative");
	
	/**
	 * <pre>
	 * "parity": "negative"
	 * </pre>
	 */
	public static final Codec<Parity> CODEC = StringRepresentable.fromEnum(Parity::values);
	
	private int value;
	private String name;
	
	Parity(int value, String name)
	{
		this.value = value;
		this.name = name;
	}

	/**
	 * {@return Parity which will invert rotation in a way that appears correct for interlocking gears on the two given faces}
	 * @param a Direction one gear faces
	 * @param b Direction another gear faces
	 */
	public static Parity inversion(Direction a, Direction b)
	{
		return a.getAxisDirection() == b.getAxisDirection() ? Parity.NEGATIVE : Parity.POSITIVE;
	}
	
	/**
	 * {@return int sign of the parity (0, +1, or -1)}
	 */
	public int value()
	{
		return this.value;
	}
	
	/**
	 * {@return Parity of this parity multiplied with another parity}
	 * @param that Parity to multiply this parity with
	 */
	public Parity multiply(Parity that)
	{
		int newValue = this.value * that.value;
		return newValue == 1 ? POSITIVE
			: newValue == -1 ? NEGATIVE
			: ZERO;
	}

	@Override
	public String getSerializedName()
	{
		return this.name;
	}
}
