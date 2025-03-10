package net.commoble.exmachina.api;

import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.world.level.LevelAccessor;

public record MechanicalNode(
	NodeShape shape,
	int teeth,
	double torque,
	double positiveCounterTorque,
	double negativeCounterTorque,
	double inertia,
	Map<MechanicalGraphKey, Parity> connectableNodes,
	BiConsumer<LevelAccessor, MechanicalUpdate> graphListener
	)
{
}
