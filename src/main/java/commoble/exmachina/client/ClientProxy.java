package commoble.exmachina.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import commoble.exmachina.ExMachina;
import commoble.exmachina.wire_post.PostsInChunk;
import commoble.exmachina.wire_post.SlackInterpolator;
import commoble.exmachina.wire_post.WireBreakPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkEvent;

public class ClientProxy
{
	private static Map<ChunkPos, Set<BlockPos>> clientPostsInChunk = new HashMap<>();

	public static void clear()
	{
		clientPostsInChunk = new HashMap<>();
	}
	
	public static void updatePostsInChunk(ChunkPos pos, Set<BlockPos> posts)
	{
		@SuppressWarnings("resource")
		Level level = Minecraft.getInstance().level;
		if (level == null)
			return;
		
		LevelChunk chunk = level.getChunk(pos.x, pos.z);
		if (chunk == null)
			return;
		
		chunk.getCapability(PostsInChunk.CAPABILITY).ifPresent(cap -> cap.setPositions(Set.copyOf(posts)));
	}
	
	@NotNull
	public static Set<BlockPos> getPostsInChunk(ChunkPos pos)
	{
		return clientPostsInChunk.getOrDefault(pos, Set.of());
	}
	
	// called from mod constructor if on physical client
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientProxy::onClientSetup);
		modBus.addListener(ClientProxy::onRegisterRenderers);
	}
	
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		event.enqueueWork(ClientProxy::afterClientSetup);
	}
	
	private static void afterClientSetup()
	{
		MenuScreens.register(ExMachina.TOASTER_MENU.get(), SimpleScreen.factory(new ResourceLocation("textures/gui/container/shulker_box.png")));
	}
	
	private static void onRegisterRenderers(RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(ExMachina.WIRE_POST_BLOCKENTITY.get(), WirePostRenderer::new);
	}
	
	public static void onWireBreakPacket(NetworkEvent.Context context, WireBreakPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		
		if (world != null)
		{
			Vec3[] points = SlackInterpolator.getInterpolatedPoints(packet.start, packet.end);
			ParticleEngine manager = mc.particleEngine;
			BlockState state = Blocks.REDSTONE_WIRE.defaultBlockState();
			
			for (Vec3 point : points)
			{
				manager.add(
					new TerrainParticle(world, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D, state)
						.setPower(0.2F).scale(0.6F));
			}
		}
	}
}
