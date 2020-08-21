package com.github.commoble.exmachina.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.block.Block;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class CircuitElementDataManager extends JsonReloadListener
{	
	public static final Gson GSON = new Gson();
	
	public Map<Block, List<CircuitElement>> data = new HashMap<>();
	// use a subfolder so we're less likely to conflict with other mods
	// i.e. this loads jsons at resources/data/modid/exmachina/circuit_elements/name.json
	public static final String FOLDER = "exmachina/circuit_elements";
	
	public CircuitElementDataManager()
	{
		super(GSON, FOLDER);
	}

	@Override
	// the superclass parses all jsons for all modids in the above data folder
	// and compiles them into the given JsonElement map below
	// no merging is done here, so jsons with the same modid:name identifier are overwritten by datapack priority
	protected void apply(Map<ResourceLocation, JsonElement> jsons, IResourceManager resourceManager, IProfiler profiler)
	{
		Map<Block, List<CircuitElement>> newMap = new HashMap<>();
		for (java.util.Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet())
		{
			// gson the json element into an intermediary class
			RawCircuitElement raw = GSON.fromJson(entry.getValue(), RawCircuitElement.class);
			String rawBlockID = raw.block;
			if (rawBlockID != null)
			{
				// and use it to create the finalized element
				ResourceLocation blockID = new ResourceLocation(rawBlockID);
				Block block = ForgeRegistries.BLOCKS.getValue(blockID);
				if (block != null)
				{
					// one block type can have multiple CircuitElements associated with it, assemble a list
					// if this is the first time we're adding an element to this block, assign a new list to the block as well
					List<CircuitElement> list = newMap.computeIfAbsent(block, x -> new ArrayList<>());
					list.add(new CircuitElement(raw, block, pluginRegistry));
				}
			}
		}
	}
	
	
}
