package com.github.commoble.exmachina.content;

import java.util.Arrays;
import java.util.function.Supplier;

import com.github.commoble.exmachina.ExMachina;
import com.github.commoble.exmachina.content.wire_post.WirePostTileEntity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegistrar
{
	public static final DeferredRegister<TileEntityType<?>> TYPES = ExMachina.createDeferredRegister(ForgeRegistries.TILE_ENTITIES);
	
	@SafeVarargs
	public static final <T extends TileEntity> RegistryObject<TileEntityType<T>>
		registerType(String name, Supplier<T> factory, Supplier<Block>... blockGetters)
	{
		return TYPES.register(name, () ->
			TileEntityType.Builder.create(factory, Arrays.stream(blockGetters).map(Supplier::get).toArray(Block[]::new)).build(null));
	}
	
	public static final RegistryObject<TileEntityType<WirePostTileEntity>> WIRE_POST = registerType(Names.WIRE_POST, WirePostTileEntity::new, BlockRegistrar.WIRE_POST);
}
