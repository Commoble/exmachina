package net.commoble.exmachina.api;

import net.commoble.exmachina.internal.ExMachina;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * The signal graph update event is used to inform the signal grapher that a graph update should occur.
 * Only the position of this game event is used, blockstate/entity are ignored and should be assumed to be null.
 */
public final class ExMachinaGameEvents
{
	private ExMachinaGameEvents() {}
	
	/** minecraft:game_event / exmachina:signal_graph_update */
	public static final ResourceKey<GameEvent> SIGNAL_GRAPH_UPDATE_KEY = ResourceKey.create(Registries.GAME_EVENT, ExMachina.id("signal_graph_update"));
	/** minecraft:game_event / exmachina:mechanical_graph_update */
	public static final ResourceKey<GameEvent> MECHANICAL_GRAPH_UPDATE_KEY = ResourceKey.create(Registries.GAME_EVENT, ExMachina.id("mechanical_graph_update"));

	/**
	 * Schedules a signal graph update to occur. Graph updates will occur at the end of the tick.
	 * This only has an effect on server levels, invoking this on client levels has no effect.
	 *  
	 * @param level LevelAccessor to schedule the graph update in
	 * @param pos BlockPos to schedule the graph update at
	 */
	public static void scheduleSignalGraphUpdate(LevelAccessor level, BlockPos pos)
	{
		level.gameEvent(SIGNAL_GRAPH_UPDATE_KEY, pos, GameEvent.Context.of(null, null));
	}
	
	/**
	 * Schedules a mechanical graph update to occur. Graph updates will occur at the end of the tick.
	 * This only has an effect on server levels, invoking this on client levels has no effect.
	 *  
	 * @param level LevelAccessor to schedule the graph update in
	 * @param pos BlockPos to schedule the graph update at
	 */
	public static void scheduleMechanicalGraphUpdate(LevelAccessor level, BlockPos pos)
	{
		level.gameEvent(MECHANICAL_GRAPH_UPDATE_KEY, pos, GameEvent.Context.of(null, null));
	}
}
