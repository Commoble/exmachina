package net.commoble.exmachina.internal.signal;

import net.commoble.exmachina.api.Channel;
import net.commoble.exmachina.api.SignalReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * SignalReceiver at a given blockpos
 * @param levelKey Level key where the SignalReceiver is
 * @param pos BlockPos where the SignalReceiver is
 * @param channel Channel the receiver is receiving on
 * @param receiver SignalReceiver at that position
 */
public record ReceiverPos(ResourceKey<Level> levelKey, BlockPos pos, Channel channel, SignalReceiver receiver)
{

}
