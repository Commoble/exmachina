package commoble.exmachina;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import commoble.exmachina.client.ClientProxy;
import commoble.exmachina.engine.api.Connector;
import commoble.exmachina.engine.api.ExMachinaRegistries;
import commoble.exmachina.generators.SolarPanelBlock;
import commoble.exmachina.processors.ToasterBlock;
import commoble.exmachina.processors.ToasterBlockEntity;
import commoble.exmachina.processors.ToasterMenu;
import commoble.exmachina.util.PoiHelper;
import commoble.exmachina.wire_post.FakeStateLevel;
import commoble.exmachina.wire_post.PostsInChunk;
import commoble.exmachina.wire_post.SlackInterpolator;
import commoble.exmachina.wire_post.WireBreakPacket;
import commoble.exmachina.wire_post.WirePostBlock;
import commoble.exmachina.wire_post.WirePostBlockEntity;
import commoble.exmachina.wire_post.WirePostConnector;
import commoble.exmachina.wire_post.WireSpoolItem;
import commoble.useitemonblockevent.api.UseItemOnBlockEvent;
import commoble.useitemonblockevent.api.UseItemOnBlockEvent.UsePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExMachina.MODID)
public class ExMachina
{
	public static final String MODID = "exmachina";
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final Supplier<IEventBus> MODBUS = Suppliers.memoize(() -> ((FMLModContainer)(ModList.get().getModContainerById(MODID).get())).getEventBus());
	
	private static final DeferredRegister<Block> BLOCKS = defreg(Registries.BLOCK);
	private static final DeferredRegister<Item> ITEMS = defreg(Registries.ITEM);
	private static final DeferredRegister<CreativeModeTab> TABS = defreg(Registries.CREATIVE_MODE_TAB);
	private static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = defreg(Registries.BLOCK_ENTITY_TYPE);
	private static final DeferredRegister<MenuType<?>> MENUS = defreg(Registries.MENU);
	private static final DeferredRegister<PoiType> POIS = defreg(Registries.POINT_OF_INTEREST_TYPE);
	private static final DeferredRegister<Codec<? extends Connector>> CONNECTORS = defreg(ExMachinaRegistries.CONNECTOR_TYPE);
	
	public static final RegistryObject<WirePostBlock> WIRE_POST_BLOCK = registerBlockItem(BLOCKS, ITEMS, Names.WIRE_POST, () -> new WirePostBlock(blockProps().mapColor(MapColor.RAW_IRON).strength(3F, 6F)));
	public static final RegistryObject<SolarPanelBlock> SOLAR_PANEL_BLOCK = registerBlockItem(BLOCKS, ITEMS, Names.SOLAR_PANEL, () -> new SolarPanelBlock(blockProps().mapColor(MapColor.COLOR_BLUE).strength(0.2F)));
	public static final RegistryObject<ToasterBlock> TOASTER_BLOCK = registerBlockItem(BLOCKS, ITEMS, Names.TOASTER, () -> new ToasterBlock(blockProps().mapColor(MapColor.STONE).strength(3.5F)));

	public static final RegistryObject<MondometerItem> MONDOMETER_ITEM = ITEMS.register(Names.MONDOMETER, () -> new MondometerItem(itemProps()));
	public static final RegistryObject<WireSpoolItem> WIRE_SPOOL_ITEM = ITEMS.register(Names.WIRE_SPOOL, () -> new WireSpoolItem(itemProps().durability(64), ExMachinaBlockTags.ELECTRICAL_WIRE_POSTS));
	
	public static final RegistryObject<CreativeModeTab> TAB = TABS.register(MODID, () -> CreativeModeTab.builder()
		.icon(() -> new ItemStack(WIRE_SPOOL_ITEM.get()))
		.title(Component.translatable("itemGroup.exmachina"))
		.displayItems((params, output) -> output.acceptAll(ITEMS.getEntries().stream().map(rob -> new ItemStack(rob.get())).toList()))
		.build());
	
	public static final RegistryObject<BlockEntityType<WirePostBlockEntity>> WIRE_POST_BLOCKENTITY = BLOCKENTITIES.register(Names.WIRE_POST, () -> beType(WirePostBlockEntity::new, WIRE_POST_BLOCK.get()));
	public static final RegistryObject<BlockEntityType<ToasterBlockEntity>> TOASTER_BLOCKENTITY = BLOCKENTITIES.register(Names.TOASTER, () -> beType(ToasterBlockEntity::new, TOASTER_BLOCK.get()));
	
	public static final RegistryObject<MenuType<ToasterMenu>> TOASTER_MENU = MENUS.register(Names.TOASTER, () -> new MenuType<>(ToasterMenu::clientMenu, FeatureFlags.VANILLA_SET));
	
	public static final RegistryObject<PoiType> SOLAR_PANEL_POI = POIS.register(Names.SOLAR_PANEL, () -> new PoiType(Set.copyOf(SOLAR_PANEL_BLOCK.get().getStateDefinition().getPossibleStates()), 0, 1));
	
	public static final ServerConfig SERVER_CONFIG = config(Type.SERVER, ServerConfig::create);
	public static final CommonConfig COMMON_CONFIG = config(Type.COMMON, CommonConfig::create);
	
	// the network channel we'll use for sending packets associated with this mod
	public static final String CHANNEL_PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(ExMachina.MODID, "main"),
		() -> CHANNEL_PROTOCOL_VERSION,
		CHANNEL_PROTOCOL_VERSION::equals,
		CHANNEL_PROTOCOL_VERSION::equals);
	
	// forge constructs this during modloading
	public ExMachina()
	{		
		CONNECTORS.register(Names.WIRE_POST, () -> WirePostConnector.CODEC);
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		// subscribe the rest of the mod event listeners
		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onRegisterCapabilities);
		
		// subscribe events to forge bus -- server init and in-game events
		forgeBus.addGenericListener(LevelChunk.class, this::onAttachChunkCapabilities);
		forgeBus.addListener(this::onUseItemOnBlock);
		forgeBus.addListener(this::onLevelTick);
		
		// register packets
		int packetID = 0;
		CHANNEL.registerMessage(packetID++,
			WireBreakPacket.class,
			WireBreakPacket::write,
			WireBreakPacket::read,
			WireBreakPacket::handle);
		
		// subscribe to client events separately so they don't break servers
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientProxy.subscribeClientEvents(modBus, forgeBus);
		}
	}

	private void onCommonSetup(FMLCommonSetupEvent event)
	{
	}
	
	private void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(PostsInChunk.class);
	}
	
	private void onAttachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event)
	{
		PostsInChunk cap = new PostsInChunk(event.getObject());
		event.addCapability(getModRL(Names.POSTS_IN_CHUNK), cap);
		event.addListener(cap::onCapabilityInvalidated);
	}
	
	@SuppressWarnings("deprecation")
	private void onUseItemOnBlock(UseItemOnBlockEvent event)
	{
		UseOnContext useContext = event.getUseOnContext();
		ItemStack stack = useContext.getItemInHand();
		if (event.getUsePhase() == UsePhase.POST_BLOCK && stack.getItem() instanceof BlockItem blockItem)
		{
			Level level = useContext.getLevel();
			BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
			BlockPos placePos = placeContext.getClickedPos(); // getClickedPos is a misnomer, this is the position the block is placed at
			BlockState placementState = blockItem.getPlacementState(placeContext);
			if (placementState == null)
			{
				return; // placement state is null when the block couldn't be placed there anyway
			}
			Set<ChunkPos> chunkPositions = PostsInChunk.getRelevantChunkPositionsNearPos(placePos);
			
			for (ChunkPos chunkPos : chunkPositions)
			{
				if (level.hasChunkAt(chunkPos.getWorldPosition()))
				{
					LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
					chunk.getCapability(PostsInChunk.CAPABILITY).ifPresent(posts ->
					{
						Set<BlockPos> checkedPostPositions = new HashSet<BlockPos>();
						for (BlockPos postPos : posts.getPositions())
						{
							BlockEntity be = level.getBlockEntity(postPos);
							if (be instanceof WirePostBlockEntity wire)
							{
								Vec3 hit = SlackInterpolator.doesBlockStateIntersectAnyWireOfPost(new FakeStateLevel(level, placePos, placementState), postPos, placePos, placementState, wire.getRemoteConnectionBoxes(), checkedPostPositions);
								if (hit != null)
								{
									Player player = placeContext.getPlayer();
									if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel)
									{
										serverPlayer.connection.send(new ClientboundSetEquipmentPacket(serverPlayer.getId(), List.of(Pair.of(EquipmentSlot.MAINHAND, serverPlayer.getItemInHand(InteractionHand.MAIN_HAND)))));
										serverLevel.sendParticles(serverPlayer, DustParticleOptions.REDSTONE, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
									}
									else if (level.isClientSide)
									{
										level.addParticle(DustParticleOptions.REDSTONE, hit.x, hit.y, hit.z, 0.05D, 0.05D, 0.05D);
									}
									
									if (player != null)
									{
										player.playNotifySound(SoundEvents.WANDERING_TRADER_HURT, SoundSource.BLOCKS, 0.5F, 2F);
									}
									event.cancelWithResult(InteractionResult.SUCCESS);
									return;
								}
								else
								{
									checkedPostPositions.add(postPos.immutable());
								}
							}
						}
					});
				}
			}
		}
	}
	
	private void onLevelTick(LevelTickEvent event)
	{
		// update solar panels
		// tick at end of the tick so light can have been updated
		// only update on server worlds
		// don't do this on the debug world as the debug world doesn't tick chunks and blockstates shouldn't update there anyway
		if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel && !serverLevel.isDebug() && serverLevel.getGameTime() % COMMON_CONFIG.solarPanelUpdateInterval().get() == 0)
		{
			var chunks = serverLevel.getChunkSource();
			Iterable<ChunkHolder> loadedChunks = chunks.chunkMap.getChunks();
			for (ChunkHolder chunkHolder : loadedChunks)
			{
				var chunk = chunkHolder.getTickingChunk(); // returns null if not ready yet
				if (chunk != null)
				{
					Set<PoiRecord> pois = PoiHelper.getInChunk(SOLAR_PANEL_POI.getHolder().get(), serverLevel, chunk.getPos());
					for (PoiRecord poi : pois)
					{
						SolarPanelBlock.tickSolarPanel(serverLevel, poi.getPos());
					}
				}
			}
		}
	}
	
	public static ResourceLocation getModRL(String name)
	{
		return new ResourceLocation(MODID, name);
	}
	
	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> registryKey)
	{
		var defreg = DeferredRegister.create(registryKey, ExMachina.MODID);
		defreg.register(MODBUS.get());
		return defreg;
	}

	private static <BLOCK extends Block> RegistryObject<BLOCK> registerBlockItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory)
	{
		return registerBlockItem(blocks, items, name, blockFactory, block -> new BlockItem(block, itemProps()));
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> RegistryObject<BLOCK> registerBlockItem(
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		String name,
		Supplier<BLOCK> blockFactory,
		Function<BLOCK,ITEM> itemFactory)
	{
		var blockRob = blocks.register(name, blockFactory);
		items.register(name, () -> itemFactory.apply(blockRob.get()));
		return blockRob;
	}
	
	private static BlockBehaviour.Properties blockProps()
	{
		return BlockBehaviour.Properties.of();
	}
	
	private static Item.Properties itemProps()
	{
		return new Item.Properties();
	}
	
	private static <T extends BlockEntity> BlockEntityType<T> beType(BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks)
	{
		return BlockEntityType.Builder.of(factory, blocks).build(null);
	}
	
	public static <T> T config(
		final ModConfig.Type configType,
		final Function<ForgeConfigSpec.Builder, T> configFactory)
	{
		final org.apache.commons.lang3.tuple.Pair<T, ForgeConfigSpec> entry = new ForgeConfigSpec.Builder()
			.configure(configFactory);
		final T config = entry.getLeft();
		final ForgeConfigSpec spec = entry.getRight();
		ModContainer modContainer = ModList.get().getModContainerById(MODID).get();
		modContainer.addConfig(new ModConfig(configType, spec, modContainer));
		
		return config;
	}
}
