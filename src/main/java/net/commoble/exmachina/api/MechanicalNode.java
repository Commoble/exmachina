package net.commoble.exmachina.api;

import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.world.level.LevelAccessor;

/**
 * Represents a node in the mechanical graph. Multiple nodes can exist in the same BlockPos.
 * @param shape NodeShape of this node. Must be unique per position in a given level.
 * @param torque double value (in Newton-meters) of the "active torque" provided by this node (such as from a windmill or motor).
 * Torque values are signed with a right-hand rule;
 * if positive, the node is rotating in the direction a right hand's fingers are pointing while the
 * thumb points to the positive direction of the axis of rotation (up, south, east).
 * If negative, the node points to the negative direction of the axis (down, north, west).
 * @param positiveCounterTorque double value of passive/load torque to subtract from system's total active torque while total active torque is positive.
 * This represents e.g. friction or maybe rocks being crushed by a grinder.
 * @param negativeCounterTorque double value of passive/load torque to add to total active torque while total active torque is negative.
 * @param inertia double value of node's inertia (in kilogram-meters^2). Higher inertia slows down the rotation of the entire system
 * and causes more power to be weighted to this node.
 * @param connectableNodes Map of the nodes this node will attempt to form connections to, and the properties of that connection.
 * Nodes must mutually connect to each other for a connection to form.
 * @param graphListener BiConsumer to invoke when a mechanical update occurs (e.g. to store power/velocity in an associated blockentity)
 */
public record MechanicalNode(
	NodeShape shape,
	double torque,
	double positiveCounterTorque,
	double negativeCounterTorque,
	double inertia,
	Map<MechanicalGraphKey, MechanicalConnection> connectableNodes,
	BiConsumer<LevelAccessor, MechanicalState> graphListener
	)
{
}
