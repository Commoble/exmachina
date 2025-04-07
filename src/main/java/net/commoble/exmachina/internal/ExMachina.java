package net.commoble.exmachina.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;

import net.commoble.exmachina.api.CircuitComponent;
import net.commoble.exmachina.api.CircuitManager;
import net.commoble.exmachina.api.Connector;
import net.commoble.exmachina.api.DynamicProperty;
import net.commoble.exmachina.api.ExMachinaDataMaps;
import net.commoble.exmachina.api.ExMachinaGameEvents;
import net.commoble.exmachina.api.ExMachinaRegistries;
import net.commoble.exmachina.api.ExMachinaTags;
import net.commoble.exmachina.api.MechanicalComponent;
import net.commoble.exmachina.api.MechanicalNodeStates;
import net.commoble.exmachina.api.MechanicalState;
import net.commoble.exmachina.api.NodeShape;
import net.commoble.exmachina.api.SignalComponent;
import net.commoble.exmachina.api.StaticProperty;
import net.commoble.exmachina.api.content.AllDirectionsConnector;
import net.commoble.exmachina.api.content.BlockStateConnector;
import net.commoble.exmachina.api.content.BlockStateProperty;
import net.commoble.exmachina.api.content.ConstantProperty;
import net.commoble.exmachina.api.content.CubeSignalComponent;
import net.commoble.exmachina.api.content.DefaultSignalComponent;
import net.commoble.exmachina.api.content.DirectionsConnector;
import net.commoble.exmachina.api.content.FloorSignalComponent;
import net.commoble.exmachina.api.content.MultipartMechanicalComponent;
import net.commoble.exmachina.api.content.NoneDynamicProperty;
import net.commoble.exmachina.api.content.NoneMechanicalComponent;
import net.commoble.exmachina.api.content.NoneTransmitter;
import net.commoble.exmachina.api.content.UnionConnector;
import net.commoble.exmachina.api.content.VariantsMechanicalComponent;
import net.commoble.exmachina.api.content.WallFloorCeilingSignalComponent;
import net.commoble.exmachina.internal.mechanical.MechanicalComponentBaker;
import net.commoble.exmachina.internal.mechanical.MechanicalGraphBuffer;
import net.commoble.exmachina.internal.power.ComponentBaker;
import net.commoble.exmachina.internal.signal.SignalGraphBuffer;
import net.commoble.exmachina.internal.util.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Mod class for Ex Machina where things are registered and events are subscribed.
 * You may be looking for:
 * <ul>
 * <li>{@link ExMachinaRegistries}, which has registry keys for Ex Machina's registries</li>
 * <li>{@link ExMachinaTags}, which has Ex Machina's tag keys</li>
 * <li>{@link ExMachinaDataMaps}, which has Ex Machina's data map types</li>
 * <li>{@link ExMachinaGameEvents#scheduleSignalGraphUpdate} to invoke a signal graph update</li>
 * <li>The net.commoble.exmachina.api package, which has api interfaces and records
 * <li>The net.commoble.exmachina.api.content package, which has the classes of Ex Machina's registered objects and their resource keys</li>
 * </ul>
 */
@Mod(ExMachina.MODID)
public class ExMachina
{
	/** Mod ID **/
	public static final String MODID = "exmachina";
	
	/** config/exmachina-common.toml **/
	public static final CommonConfig COMMON_CONFIG = ConfigHelper.register(MODID, ModConfig.Type.SERVER, CommonConfig::create);
	
	/**
	 * mod constructor
	 * @param modBus mod bus
	 */
	public ExMachina(IEventBus modBus)
	{
		IEventBus gameBus = NeoForge.EVENT_BUS;
		
		var connectors = newRegistry(ExMachinaRegistries.CONNECTOR_TYPE);
		var staticProperties = newRegistry(ExMachinaRegistries.STATIC_PROPERTY_TYPE);
		var dynamicProperties = newRegistry(ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE);
		var transmitters =  newRegistry(ExMachinaRegistries.SIGNAL_COMPONENT_TYPE);
		var mechanicalComponents = newRegistry(ExMachinaRegistries.MECHANICAL_COMPONENT_TYPE);
		var gameEvents = defreg(Registries.GAME_EVENT);
		var attachmentTypes = defreg(NeoForgeRegistries.Keys.ATTACHMENT_TYPES);
		
		BiConsumer<ResourceKey<MapCodec<? extends Connector>>, MapCodec<? extends Connector>> registerConnector = (key,codec) -> connectors.register(key.location().getPath(), () -> codec);
		BiConsumer<ResourceKey<MapCodec<? extends StaticProperty>>, MapCodec<? extends StaticProperty>> registerStaticProperty = (key,codec) -> staticProperties.register(key.location().getPath(), () -> codec);
		BiConsumer<ResourceKey<MapCodec<? extends DynamicProperty>>, MapCodec<? extends DynamicProperty>> registerDynamicProperty = (key,codec) -> dynamicProperties.register(key.location().getPath(), () -> codec);
		
		BiConsumer<ResourceKey<MapCodec<? extends SignalComponent>>, MapCodec<? extends SignalComponent>> registerTransmitter = (key,codec) -> transmitters.register(key.location().getPath(), () -> codec);
		BiConsumer<ResourceKey<MapCodec<? extends MechanicalComponent>>, MapCodec<? extends MechanicalComponent>> registerMechanicalComponent = (key,codec) -> mechanicalComponents.register(key.location().getPath(), () -> codec);
		
		registerConnector.accept(AllDirectionsConnector.KEY, AllDirectionsConnector.CODEC);
		registerConnector.accept(DirectionsConnector.KEY, DirectionsConnector.CODEC);
		registerConnector.accept(BlockStateConnector.KEY, BlockStateConnector.CODEC);
		registerConnector.accept(UnionConnector.KEY, UnionConnector.CODEC);
		registerStaticProperty.accept(ConstantProperty.KEY, ConstantProperty.CODEC);
		registerStaticProperty.accept(BlockStateProperty.KEY, BlockStateProperty.CODEC);
		registerDynamicProperty.accept(NoneDynamicProperty.KEY, NoneDynamicProperty.CODEC);
		registerTransmitter.accept(NoneTransmitter.KEY, NoneTransmitter.CODEC);
		registerTransmitter.accept(DefaultSignalComponent.KEY, DefaultSignalComponent.CODEC);
		registerTransmitter.accept(CubeSignalComponent.KEY, CubeSignalComponent.CODEC);
		registerTransmitter.accept(FloorSignalComponent.KEY, FloorSignalComponent.CODEC);
		registerTransmitter.accept(WallFloorCeilingSignalComponent.KEY, WallFloorCeilingSignalComponent.CODEC);
		registerMechanicalComponent.accept(NoneMechanicalComponent.KEY, NoneMechanicalComponent.CODEC);
		registerMechanicalComponent.accept(VariantsMechanicalComponent.KEY, VariantsMechanicalComponent.CODEC);
		registerMechanicalComponent.accept(MultipartMechanicalComponent.KEY, MultipartMechanicalComponent.CODEC);
		
		gameEvents.register(ExMachinaGameEvents.SIGNAL_GRAPH_UPDATE_KEY.location().getPath(), () -> new GameEvent(0));
		attachmentTypes.register(MechanicalNodeStates.KEY.location().getPath(), () -> AttachmentType.<Map<NodeShape,MechanicalState>>builder(() -> new HashMap<>())
			.serialize(MechanicalNodeStates.CODEC)
			.build());
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onRegisterDataPackRegistries);
		modBus.addListener(this::onRegisterDataMapTypes);
		
		// subscribe events to forge bus -- server init and in-game events
		gameBus.addListener(this::onServerStarting);
		gameBus.addListener(this::onServerStopping);
		gameBus.addListener(this::onNeighborNotify);
		gameBus.addListener(this::onVanillaGameEvent);
		gameBus.addListener(this::onEndOfServerTickEvent);
	}
	
	private void onRegisterDataPackRegistries(DataPackRegistryEvent.NewRegistry event)
	{
		event.dataPackRegistry(ExMachinaRegistries.CIRCUIT_COMPONENT, CircuitComponent.CODEC);
		event.dataPackRegistry(ExMachinaRegistries.MECHANICAL_COMPONENT, MechanicalComponent.CODEC);
	}
	
	private void onRegisterDataMapTypes(RegisterDataMapTypesEvent event)
	{
		event.register(ExMachinaDataMaps.SIGNAL_COMPONENT);
	}
	
	private void onServerStarting(ServerStartingEvent event)
	{
		RegistryAccess registries = event.getServer().registryAccess();
		ComponentBaker.get().preBake(registries);
		MechanicalComponentBaker.INSTANCE.preBake(registries);
	}
	
	private void onServerStopping(ServerStoppingEvent event)
	{
		ComponentBaker.get().clear();
		MechanicalComponentBaker.INSTANCE.clear();
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
			MechanicalGraphBuffer.get(serverLevel.getServer()).enqueue(serverLevel.dimension(), pos);
			BlockState state = event.getState();
			if (!state.is(ExMachinaTags.Blocks.NO_AUTOMATIC_MECHANICAL_UPDATES))
			{
				MechanicalGraphBuffer.get(serverLevel.getServer()).enqueue(serverLevel.dimension(), pos);
				for (Direction directionToNeighbor : Direction.values())
				{
					BlockPos neighborPos = pos.relative(directionToNeighbor);
					BlockState neighborState = serverLevel.getBlockState(neighborPos);
					if (!neighborState.is(ExMachinaTags.Blocks.NO_AUTOMATIC_MECHANICAL_UPDATES))
					{
						MechanicalGraphBuffer.get(serverLevel.getServer()).enqueue(serverLevel.dimension(), neighborPos);
					}
				}
			}	
			
		}
	}
	
	private void onVanillaGameEvent(VanillaGameEvent event)
	{
		// called when a block update occurs at a given position (including when a blockstate change occurs at that position)
		// if the blockstate changed, the event's given state is the new blockstate
		LevelAccessor level = event.getLevel();
		
		if (level instanceof ServerLevel serverLevel)
		{
			var holder = event.getVanillaEvent();
			if (holder.is(ExMachinaGameEvents.SIGNAL_GRAPH_UPDATE_KEY))
			{
				SignalGraphBuffer.get(serverLevel.getServer()).enqueue(serverLevel.dimension(), BlockPos.containing(event.getEventPosition()));				
			}
			else if (holder.is(ExMachinaGameEvents.MECHANICAL_GRAPH_UPDATE_KEY))
			{
				BlockPos pos = BlockPos.containing(event.getEventPosition());
				MechanicalGraphBuffer.get(serverLevel.getServer()).enqueue(serverLevel.dimension(), pos);	
			}
		}
	}
	
	private void onEndOfServerTickEvent(ServerTickEvent.Post event)
	{
		MinecraftServer server = event.getServer();
		MechanicalGraphBuffer.get(server).tick(server);
		SignalGraphBuffer.get(server).tick(server);
	}
	
	/**
	 * {@return ResourceLocation of exmachina:${name}}
	 * @param name ResourceLocation path
	 */
	public static ResourceLocation id(String name)
	{
		return ResourceLocation.fromNamespaceAndPath(ExMachina.MODID, name);
	}
	
	private static <T> DeferredRegister<T> newRegistry(ResourceKey<Registry<T>> key)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		var defreg = DeferredRegister.create(key, ExMachina.MODID);
		defreg.makeRegistry(builder -> {});
		defreg.register(modBus);
		return defreg;
	}
	
	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> key)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		var defreg = DeferredRegister.create(key, MODID);
		defreg.register(modBus);
		return defreg;
	}
}
