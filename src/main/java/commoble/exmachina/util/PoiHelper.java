package commoble.exmachina.util;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;

public class PoiHelper
{
	// Each chunk section has a Map of PoiType -> Set<PoiRecord>.
	// It would be very useful to get all the poirecords for a given poitype.
	// However, the vanilla code only allows iterating over the map keys, returning poirecords
	// for each type that matches a given predicate! WHY
	// So we ignore that and just yoink the records out of the map directly.
	public static Set<PoiRecord> getInChunk(Holder<PoiType> type, ServerLevel serverLevel, ChunkPos chunkPos)
	{
		PoiManager poiManager = serverLevel.getPoiManager();
		Set<PoiRecord> results = new HashSet<>();
		int minSection = serverLevel.getMinSection();
		int maxSection = serverLevel.getMaxSection();
		for (int i=minSection; i<maxSection; i++)
		{
			// public net.minecraft.world.level.chunk.storage.SectionStorage m_63823_(J)Ljava/util/Optional; # getOrLoad
			poiManager.getOrLoad(SectionPos.of(chunkPos, i).asLong()).ifPresent(poiSection -> {
				// public net.minecraft.world.entity.ai.village.poi.PoiSection f_27262_ # byType
				Set<PoiRecord> records = poiSection.byType.get(type);
				if (records != null)
				{
					results.addAll(records);
				}
			});
		}
		return results;
	}
}
