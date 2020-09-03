package commoble.exmachina.util;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class DirectionHelper
{
	public static final Direction D = Direction.DOWN;
	public static final Direction U = Direction.UP;
	public static final Direction N = Direction.NORTH;
	public static final Direction S = Direction.SOUTH;
	public static final Direction W = Direction.WEST;
	public static final Direction E = Direction.EAST;
	
	public static final Direction[] SAMES ={D,U,N,S,W,E};
	public static final Direction[] OPPOSITES = {U,D,S,N,E,W};
	public static final Direction[] ROTATE_X_DNUS = {N,S,U,D,W,E};
	public static final Direction[] ROTATE_X_DSUN = {S,N,D,U,W,E};
	public static final Direction[] ROTATE_Y_NESW = {D,U,E,W,N,S};
	public static final Direction[] ROTATE_Y_NWSE = {D,U,W,E,S,N};
	public static final Direction[] ROTATE_Z_DWUE = {W,E,N,S,U,D};
	public static final Direction[] ROTATE_Z_DEUW = {E,W,N,S,D,U};
	
	public static final Axis[][] ORTHAGONAL_AXES =
	{
		{Axis.Y,Axis.X},
		{Axis.X,Axis.Z},
		{Axis.Y,Axis.Z}
	};
	
	public static final Direction[][] ORTHAGONAL_ROTATION_TABLE =
	{
		{N,E,S,W},
		{S,E,N,W},
		{U,E,D,W},
		{D,E,U,W},
		{D,S,U,N},
		{U,S,D,N}
	};
	
	/** Indices are direction indices: [from][to][toRotate] **/
	public static final Direction[][][] ROTATION_TABLE =
	{
		// from = down
		{
			SAMES, // to = down
			OPPOSITES, // to = up
			ROTATE_X_DNUS, // down to north
			ROTATE_X_DSUN, // down to south
			ROTATE_Z_DWUE, // down to west
			ROTATE_Z_DEUW // down to east
		},
		// from = up
		{
			OPPOSITES, // up to down
			SAMES, // up to up
			ROTATE_X_DSUN, // up to north
			ROTATE_X_DNUS, // up to south
			ROTATE_Z_DEUW, // up to west
			ROTATE_Z_DWUE, // up to east
		},
		// from = north
		{
			ROTATE_X_DSUN, // north to down
			ROTATE_X_DNUS, // north to up
			SAMES,
			OPPOSITES,
			ROTATE_Y_NWSE, // north to west
			ROTATE_Y_NESW // north to east
		},
		// from = south
		{
			ROTATE_X_DNUS, // south to down
			ROTATE_X_DSUN, // south to up
			OPPOSITES,
			SAMES,
			ROTATE_Y_NESW,
			ROTATE_Y_NWSE
		},
		// from = west
		{
			ROTATE_Z_DEUW, // west to down
			ROTATE_Z_DWUE, // west to up
			ROTATE_Y_NESW, // west to north
			ROTATE_Y_NWSE, // west to south
			SAMES,
			OPPOSITES
		},
		// from = east
		{
			ROTATE_Z_DWUE,
			ROTATE_Z_DEUW,
			ROTATE_Y_NWSE,
			ROTATE_Y_NESW,
			OPPOSITES,
			SAMES
		}
	};
	
	/**
	 * Given two directions and a third, applies the rotation-of-the-first-direction-to-the-second
	 * to the third direction
	 * @param from Starting direction of the reference operation
	 * @param to Final direction of the reference operation
	 * @param toRotate Direction to apply the reference operation to
	 * @return The direction we get after we apply the reference rotation to toRotate
	 */
	public static Direction getRotatedDirection(Direction from, Direction to, Direction toRotate)
	{
		return ROTATION_TABLE[from.ordinal()][to.ordinal()][toRotate.ordinal()];
	}
}
