package net.commoble.exmachina.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.commoble.exmachina.api.CircuitComponent;
import net.commoble.exmachina.api.CircuitManager;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.content.AllDirectionsConnector;
import net.commoble.exmachina.api.content.BlockStateConnector;
import net.commoble.exmachina.api.content.BlockStateProperty;
import net.commoble.exmachina.api.content.ConstantProperty;
import net.commoble.exmachina.api.content.DirectionsConnector;
import net.commoble.exmachina.api.content.NoneDynamicProperty;
import net.commoble.exmachina.api.content.UnionConnector;
import net.commoble.exmachina.internal.circuit.ComponentBaker;
import net.commoble.exmachina.internal.util.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExMachina.MODID)
public class ExMachina
{
	public static final String MODID = "exmachina";
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final CommonConfig COMMON_CONFIG = ConfigHelper.register(MODID, ModConfig.Type.SERVER, CommonConfig::create);
	
	// forge constructs this during modloading
	public ExMachina(IEventBus modBus)
	{
		IEventBus gameBus = NeoForge.EVENT_BUS;
		
		var connectors = defreg(modBus, ExMachinaRegistries.CONNECTOR_TYPE);
		var staticProperties = defreg(modBus, ExMachinaRegistries.STATIC_PROPERTY_TYPE);
		var dynamicProperties = defreg(modBus, ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE);
		
		connectors.register(Names.ALL_DIRECTIONS, () -> AllDirectionsConnector.CODEC);
		connectors.register(Names.DIRECTIONS, () -> DirectionsConnector.CODEC);
		connectors.register(Names.BLOCKSTATE, () -> BlockStateConnector.CODEC);
		connectors.register(Names.UNION, () -> UnionConnector.CODEC);
		staticProperties.register(Names.CONSTANT, () -> ConstantProperty.CODEC);
		staticProperties.register(Names.BLOCKSTATE, () -> BlockStateProperty.CODEC);
		dynamicProperties.register(Names.NONE, () -> NoneDynamicProperty.CODEC);
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onRegisterDataPackRegistries);
		
		// subscribe events to forge bus -- server init and in-game events
		gameBus.addListener(this::onServerStarting);
		gameBus.addListener(this::onServerStopping);
		gameBus.addListener(this::onNeighborNotify);
	}
	
	private void onRegisterDataPackRegistries(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(ExMachinaRegistries.CIRCUIT_COMPONENT, CircuitComponent.CODEC);
	}
	
	private void onServerStarting(ServerStartingEvent event)
	{
		ComponentBaker.get().preBake(event.getServer().registryAccess());
	}
	
	private void onServerStopping(ServerStoppingEvent event)
	{
		ComponentBaker.get().clear();
	}
	
	private void onNeighborNotify(NeighborNotifyEvent event)
	{
		// called when a block update occurs at a given position (including when a blockstate change occurs at that position)
		// if the blockstate changed, the event's given state is the new blockstate
		LevelAccessor level = event.getLevel();
		
		if (level instanceof ServerLevel serverLevel)
		{
			BlockState newState = event.getState();
			BlockPos pos = event.getPos();
			CircuitManager.get(serverLevel).onBlockUpdate(newState, pos);
		}
	}
	
	public static ResourceLocation id(String name)
	{
		return ResourceLocation.fromNamespaceAndPath(ExMachina.MODID, name);
	}
	
	public static <T> DeferredRegister<T> defreg(IEventBus modBus, ResourceKey<Registry<T>> key)
	{
		var defreg = DeferredRegister.create(key, ExMachina.MODID);
		defreg.makeRegistry(builder -> {});
		defreg.register(modBus);
		return defreg;
	}
}
