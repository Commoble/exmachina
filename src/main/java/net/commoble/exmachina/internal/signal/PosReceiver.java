package net.commoble.exmachina.internal.signal;

import net.commoble.exmachina.api.SignalReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * SignalReceiver at a given blockpos
 * @param levelKey Level key where the SignalReceiver is
 * @param pos BlockPos where the SignalReceiver is
 * @param receiver SignalReceiver at that position
 */
public record PosReceiver(ResourceKey<Level> levelKey, BlockPos pos, SignalReceiver receiver)
{

}
