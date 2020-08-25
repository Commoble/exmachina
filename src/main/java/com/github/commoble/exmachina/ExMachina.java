package com.github.commoble.exmachina;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.commoble.exmachina.api.CircuitManager;
import com.github.commoble.exmachina.api.CircuitManagerCapability;
import com.github.commoble.exmachina.circuit.NoStorageForCapability;
import com.github.commoble.exmachina.circuit.WorldCircuitManager;
import com.github.commoble.exmachina.client.ClientEvents;
import com.github.commoble.exmachina.content.BlockRegistrar;
import com.github.commoble.exmachina.content.ItemRegistrar;
import com.github.commoble.exmachina.content.Names;
import com.github.commoble.exmachina.content.TileEntityRegistrar;
import com.github.commoble.exmachina.content.wire_post.IPostsInChunk;
import com.github.commoble.exmachina.content.wire_post.PostsInChunk;
import com.github.commoble.exmachina.content.wire_post.PostsInChunkCapability;
import com.github.commoble.exmachina.content.wire_post.WireBreakPacket;
import com.github.commoble.exmachina.content.wire_post.WirePostTileEntity;
import com.github.commoble.exmachina.data.CircuitElementDataManager;
import com.github.commoble.exmachina.plugins.CircuitBehaviourRegistry;
import com.github.commoble.exmachina.plugins.PluginLoader;
import com.github.commoble.exmachina.util.ConfigHelper;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
		if (INSTANCE == null) INSTANCE = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		// subscribe events to mod bus -- registries and other init events, mostly
		// subscribe deferred registers so they register our stuff for us
		DeferredRegister<?>[] registers =
		{
			BlockRegistrar.BLOCKS,
			ItemRegistrar.ITEMS,
			TileEntityRegistrar.TYPES
		};
	
		for (DeferredRegister<?> register : registers)
		{
			register.register(modBus);
		}
		
		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onCommonSetup);
		
		// subscribe events to forge bus -- server init and in-game events
		forgeBus.addListener(this::onAddReloadListeners);
		forgeBus.addGenericListener(Chunk.class, this::onAttachChunkCapabilities);
		forgeBus.addGenericListener(World.class, this::onAttachWorldCapabilities);
		forgeBus.addListener(EventPriority.LOW, this::checkBlockingWiresOnEntityPlaceBlock);
		forgeBus.addListener(this::onNeighborNotify);
		
		// subscribe to client events separately so they don't break servers
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEvents.subscribeClientEvents(modBus, forgeBus);
		}
		
		this.serverConfig = ConfigHelper.register(Type.SERVER, ServerConfig::new);
	}
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		// register packets
		int packetID = 0;
		CHANNEL.registerMessage(packetID++,
			WireBreakPacket.class,
			WireBreakPacket::write,
			WireBreakPacket::read,
			WireBreakPacket::handle);
		
		// register capabilities
		CapabilityManager.INSTANCE.register(IPostsInChunk.class, new PostsInChunkCapability.Storage(), PostsInChunk::new);
		CapabilityManager.INSTANCE.register(CircuitManager.class, new NoStorageForCapability<>(), () -> null);
		
		// init API plugins
		this.circuitBehaviourRegistry = PluginLoader.loadPlugins();
	}
	
	private void onAddReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(this.circuitElementDataManager);
	}
	
	private void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event)
	{
		PostsInChunk postsInChunk = new PostsInChunk();
		event.addCapability(getModRL(Names.POSTS_IN_CHUNK), postsInChunk);
		event.addListener(() -> postsInChunk.holder.invalidate());
		
	}
	
	private void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event)
	{
		WorldCircuitManager manager = new WorldCircuitManager(event.getObject());
		event.addCapability(getModRL(Names.CIRCUIT_MANAGER), manager);
		event.addListener(manager::onCapabilityInvalidated);
	}
	
	private void checkBlockingWiresOnEntityPlaceBlock(BlockEvent.EntityPlaceEvent event)
	{
		BlockPos pos = event.getPos();
		IWorld iworld = event.getWorld();
		BlockState state = event.getState();
		if (iworld instanceof World && !iworld.isRemote())
		{
			World world = (World)iworld;
			
			Set<ChunkPos> chunkPositions = PostsInChunk.getRelevantChunkPositionsNearPos(pos);
			
			for (ChunkPos chunkPos : chunkPositions)
			{
				if (world.isBlockLoaded(chunkPos.asBlockPos()))
				{
					Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
					chunk.getCapability(PostsInChunkCapability.INSTANCE).ifPresent(posts ->
					{
						Set<BlockPos> checkedPostPositions = new HashSet<BlockPos>();
						for (BlockPos postPos : posts.getPositions())
						{
							TileEntity te = world.getTileEntity(postPos);
							if (te instanceof WirePostTileEntity)
							{
								Vector3d hit = ((WirePostTileEntity)te).doesBlockStateIntersectConnection(pos, state, checkedPostPositions);
								if (hit != null)
								{
									event.setCanceled(true);
									Entity entity = event.getEntity();
									if (entity instanceof ServerPlayerEntity)
									{
										ServerPlayerEntity serverPlayer = (ServerPlayerEntity)entity;
										serverPlayer.connection.sendPacket(new SEntityEquipmentPacket(serverPlayer.getEntityId(), Lists.newArrayList(Pair.of(EquipmentSlotType.MAINHAND, serverPlayer.getHeldItem(Hand.MAIN_HAND)))));
										((ServerWorld)world).spawnParticle(serverPlayer, RedstoneParticleData.REDSTONE_DUST, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
										serverPlayer.connection.sendPacket(new SPlaySoundEffectPacket(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, hit.x, hit.y, hit.z, 0.5F, 2F));
									}
									return;
								}
								else
								{
									checkedPostPositions.add(postPos);
								}
							}
						}
					});
				}
			}
		}
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
	
	public static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createDeferredRegister(IForgeRegistry<T> registry)
	{
		return DeferredRegister.create(registry, ExMachina.MODID);
	}
}
