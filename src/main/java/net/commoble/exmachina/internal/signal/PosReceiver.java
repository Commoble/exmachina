package net.commoble.exmachina.internal.signal;

import net.commoble.exmachina.api.SignalReceiver;
import net.minecraft.core.BlockPos;

/**
 * SignalReceiver at a given blockpos
 * @param pos BlockPos where the SignalReceiver is
 * @param receiver SignalReceiver at that position
 */
public record PosReceiver(BlockPos pos, SignalReceiver receiver)
{

}
