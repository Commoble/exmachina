package net.commoble.exmachina.api.content;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.Parity;
import net.minecraft.core.Direction;

/**
 * Relative node definition used by {@link VariantsMechanicalComponent} and {@link MultipartMechanicalComponent}
 * @param shape NodeShape of this node
 * @param torque double value of active torque provided by this node in Newton-Meters.
 * Torque is signed using a right-hand rule, where the fingers of the hand point in the direction of rotation;
 * if the right thumb points toward the positive end of the axis of rotation (up/south/east), torque is positive;
 * if the right thumb points toward the negative end of the axis (down/north/west), torque is negative
 * @param positiveCounterTorque double value of passive torque (e.g. from friction) to subtract when total active torque greater than 0 (right-hand's thumb is pointing toward positive end of axis)
 * @param negativeCounterTorque double value of passive torque to add when total active torque less than 0 (right thumb points toward negative axis)
 * @param inertia double value of inertia of this node (in kg-meters^2)
 * @param connections List of RawConnection objects indicating adjacent nodes this node can connect to, if those targets are present
 */ 
public record RawNode(
	NodeShape shape,
	double torque,
	double positiveCounterTorque,
	double negativeCounterTorque,
	double inertia,
	List<RawConnection> connections)
{
	/**
	 * <pre>
	 * {
	 *   "shape": e.g. "cube", "north", {"face": "north", "side": "down"},
	 *   "torque": 100, // a positive torque indicates right-hand thumb points in + direction
	 *   "positive_counter_torque": 10, // must be positive, subtracted from torque when total active torque is positive
	 *   "negative_counter_torque": 10, // must be positive, added to torque when total active torque is negative
	 *   "inertia": 0.01, // must be positive
	 *   "connections": [
	 *   	// see RawConnection definition
	 *   ]
	 * }
	 * </pre>
	 */
	public static final Codec<RawNode> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			NodeShape.CODEC.fieldOf("shape").forGetter(RawNode::shape),
			Codec.DOUBLE.optionalFieldOf("torque",0D).forGetter(RawNode::torque),
			Codec.DOUBLE.optionalFieldOf("positive_counter_torque",0D).forGetter(RawNode::positiveCounterTorque),
			Codec.DOUBLE.optionalFieldOf("negative_counter_torque",0D).forGetter(RawNode::negativeCounterTorque),
			Codec.DOUBLE.fieldOf("inertia").forGetter(RawNode::inertia),
			RawConnection.CODEC.listOf().fieldOf("connections").forGetter(RawNode::connections)
		).apply(builder, RawNode::new));

	/**
	 * Relative connection from a RawNode to a neighbor node
	 * @param direction Direction to neighbor; if not present, indicates the target node is in the same block as the targeting node
	 * @param neighborShape NodeShape of target node to attempt to connect to
	 * @param parity Parity of connection; if negative, inverts direction of rotation (think two parallel gears meshed together)
	 * @param teeth number of teeth this node has when connecting to target node; if both nodes connect with teeth > 0, causes a gearshift based on teeth ratio
	 */
	public static record RawConnection(
		Optional<Direction> direction,
		NodeShape neighborShape,
		Parity parity,
		int teeth)
	{
		
		/**
		 * <pre>
		 * {
		 *   "direction": "south", // optional field, defaults to none
		 *   "neighbor_shape": e.g. "cube", "north", {"face": "north", "side": "down"},
		 *   "parity": "negative", // optional field, defaults to positive
		 *   "teeth": 40 // optional field, defaults to 0
		 * }
		 * </pre>
		 */
		public static final Codec<RawConnection> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				Direction.CODEC.optionalFieldOf("direction").forGetter(RawConnection::direction),
				NodeShape.CODEC.fieldOf("neighbor_shape").forGetter(RawConnection::neighborShape),
				Parity.CODEC.optionalFieldOf("parity", Parity.POSITIVE).forGetter(RawConnection::parity),
				Codec.INT.optionalFieldOf("teeth", 0).forGetter(RawConnection::teeth)
			).apply(builder, RawConnection::new));
	}
}
