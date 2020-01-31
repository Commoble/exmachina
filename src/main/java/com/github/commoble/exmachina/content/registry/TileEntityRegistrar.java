package com.github.commoble.exmachina.content.registry;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.RegistryNames;
import com.github.commoble.exmachina.content.ResourceLocations;
import com.github.commoble.exmachina.content.util.RegistryHelper;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExMachinaMod.MODID)
public class TileEntityRegistrar
{
	@ObjectHolder(RegistryNames.WIRE_PLINTH)
	public static final TileEntityType<WirePlinthTileEntity> wire_plinth = null;
	
	public static void registerTileEntities(IForgeRegistry<TileEntityType<?>> registry)
	{
		RegistryHelper.register(registry, ResourceLocations.WIRE_PLINTH,
			TileEntityType.Builder.create(WirePlinthTileEntity::new, BlockRegistrar.wire_plinth).build(null));
	}
}
