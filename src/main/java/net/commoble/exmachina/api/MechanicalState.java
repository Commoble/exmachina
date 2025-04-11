package net.commoble.exmachina.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

/**
 * Representation of a mechanical update provided to a node in the mechanical graph
 * @param power double value of the power being consumed by this node in Watts (Joules per second). Always non-negative.
 * Equal to the square of the node's (angular velocity)^2 * inertia
 * @param angularVelocity double value of the anvular velocity at this node, in radians per second.
 * This value is signed with a right-hand rule;
 * if positive, the node is rotating in the direction a right hand's fingers are pointing while the
 * thumb points to the positive direction of the axis of rotation (up, south, east).
 * If negative, the node points to the negative direction of the axis (down, north, west).
 */
public record MechanicalState(double power, double angularVelocity) 
{
	/** MechanicalState constant for zero power and velocity **/
	public static final MechanicalState ZERO = new MechanicalState(0D,0D);
	
	/**
	 * <pre>
	 * {
	 *   "power": 10.0,
	 *   "angular_velocity": -1.0
	 * }
	 * </pre>
	 */
	public static final Codec<MechanicalState> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Codec.DOUBLE.fieldOf("power").forGetter(MechanicalState::power),
			Codec.DOUBLE.fieldOf("angular_velocity").forGetter(MechanicalState::angularVelocity)
		).apply(builder, MechanicalState::new));
	
	/** codec but stream **/
	public static final StreamCodec<ByteBuf,MechanicalState> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.DOUBLE, MechanicalState::power,
		ByteBufCodecs.DOUBLE, MechanicalState::angularVelocity,
		MechanicalState::new);
	
	/**
	 * Return a modulated game time suitable for determining machine rotation.
	 * The intent of this is for machines to have a consistent rotation on both client and server,
	 * while keeping the maximum tick count reasonably small to avoid visual artifacts from rounding errors in high double values.
	 * 
	 * This is configurable in config/exmachina-server.toml
	 * @param level Level a machine exists in
	 * @return integer number of ticks elapsed in current machine cycle
	 */
	public static int getMachineTicks(Level level)
	{
		return (int)(level.getGameTime() % ExMachina.SERVER_CONFIG.machineCycleTicks().getAsInt());
	}
}
