package commoble.exmachina;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.databuddy.plugin.PluginLoader;
import commoble.exmachina.api.AutoPlugin;
import commoble.exmachina.api.CircuitManager;
import commoble.exmachina.api.CircuitManagerCapability;
import commoble.exmachina.api.Plugin;
import commoble.exmachina.circuit.NoStorageForCapability;
import commoble.exmachina.circuit.WorldCircuitManager;
import commoble.exmachina.data.CircuitElementDataManager;
import commoble.exmachina.plugins.CircuitBehaviourRegistry;
import commoble.exmachina.util.ConfigHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

// tell forge to make an instance of this class
@Mod(ExMachina.MODID)
public class ExMachina
{
	public static final String MODID = "exmachina";
	public static final Logger LOGGER = LogManager.getLogger();
	public static ExMachina INSTANCE;
	
	// the network channel we'll use for sending packets associated with this mod
	public static final String CHANNEL_PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(ExMachina.MODID, "main"),
		() -> CHANNEL_PROTOCOL_VERSION,
		CHANNEL_PROTOCOL_VERSION::equals,
		CHANNEL_PROTOCOL_VERSION::equals);
	
	public final ServerConfig serverConfig;
	public CircuitBehaviourRegistry circuitBehaviourRegistry = new CircuitBehaviourRegistry();
	public CircuitElementDataManager circuitElementDataManager = new CircuitElementDataManager();
	
	// forge constructs this during modloading
	public ExMachina()
	{
		INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onCommonSetup);
		
		// subscribe events to forge bus -- server init and in-game events
		forgeBus.addListener(this::onAddReloadListeners);
		forgeBus.addGenericListener(World.class, this::onAttachWorldCapabilities);
		forgeBus.addListener(this::onNeighborNotify);
		
		this.serverConfig = ConfigHelper.register(Type.SERVER, ServerConfig::new);
	}
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		// register capabilities
		CapabilityManager.INSTANCE.register(CircuitManager.class, new NoStorageForCapability<>(), () -> null);
		
		// init API plugins
		this.circuitBehaviourRegistry = PluginLoader.loadPlugins(AutoPlugin.class, Plugin.class, new CircuitBehaviourRegistry(), LOGGER, MODID);
	}
	
	private void onAddReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(this.circuitElementDataManager);
	}
	
	private void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event)
	{
		WorldCircuitManager manager = new WorldCircuitManager(event.getObject());
		event.addCapability(getModRL("circuit_manager"), manager);
		event.addListener(manager::onCapabilityInvalidated);
	}
	
	private void onNeighborNotify(NeighborNotifyEvent event)
	{
		// called when a block update occurs at a given position (including when a blockstate change occurs at that position)
		// if the blockstate changed, the event's given state is the new blockstate
		IWorld iworld = event.getWorld();
		
		if (iworld instanceof World)
		{
			@SuppressWarnings("resource")
			World world = (World)iworld;
			BlockState newState = event.getState();
			BlockPos pos = event.getPos();
			world.getCapability(CircuitManagerCapability.INSTANCE).ifPresent(manager -> manager.onBlockUpdate(newState, pos));
		}
	}
	
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}
}
