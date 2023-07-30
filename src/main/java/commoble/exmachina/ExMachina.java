package commoble.exmachina;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.databuddy.config.ConfigHelper;
import commoble.exmachina.api.CircuitComponent;
import commoble.exmachina.api.CircuitManager;
import commoble.exmachina.api.ExMachinaRegistries;
import commoble.exmachina.api.content.AllDirectionsConnector;
import commoble.exmachina.api.content.BlockStateConnector;
import commoble.exmachina.api.content.BlockStateProperty;
import commoble.exmachina.api.content.ConstantProperty;
import commoble.exmachina.api.content.DirectionsConnector;
import commoble.exmachina.api.content.NoneDynamicProperty;
import commoble.exmachina.api.content.UnionConnector;
import commoble.exmachina.circuit.ComponentBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

@Mod(ExMachina.MODID)
public class ExMachina
{
	public static final String MODID = "exmachina";
	public static final Logger LOGGER = LogManager.getLogger();
	private static ExMachina instance;
	public static ExMachina get() { return instance; }
	
	// the network channel we'll use for sending packets associated with this mod
	public static final String CHANNEL_PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(ExMachina.MODID, "main"),
		() -> CHANNEL_PROTOCOL_VERSION,
		CHANNEL_PROTOCOL_VERSION::equals,
		CHANNEL_PROTOCOL_VERSION::equals);
	
	public final CommonConfig commonConfig;
	
	// forge constructs this during modloading
	public ExMachina()
	{
		instance = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		var connectors = newDefreg(modBus, ExMachinaRegistries.CONNECTOR_TYPE);
		var staticProperties = newDefreg(modBus, ExMachinaRegistries.STATIC_PROPERTY_TYPE);
		var dynamicProperties = newDefreg(modBus, ExMachinaRegistries.DYNAMIC_PROPERTY_TYPE);
		
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
		forgeBus.addListener(this::onServerStarting);
		forgeBus.addListener(this::onServerStopping);
		forgeBus.addListener(this::onNeighborNotify);
		
		this.commonConfig = ConfigHelper.register(ModConfig.Type.SERVER, CommonConfig::create);
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
	
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}
	
	public static <T> DeferredRegister<T> newDefreg(IEventBus modBus, ResourceKey<Registry<T>> key)
	{
		var defreg = DeferredRegister.create(key, MODID);
		defreg.makeRegistry(() -> new RegistryBuilder<T>()
			.hasTags());
		defreg.register(modBus);
		return defreg;
	}
}
