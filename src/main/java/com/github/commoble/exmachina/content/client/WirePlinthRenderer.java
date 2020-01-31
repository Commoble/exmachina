package com.github.commoble.exmachina.content.client;

import java.util.Set;

import com.github.commoble.exmachina.content.wireplinth.WirePlinthBlock;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WirePlinthRenderer extends TileEntityRenderer<WirePlinthTileEntity>
{

	public WirePlinthRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(WirePlinthTileEntity plinth, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		BlockPos startPos = plinth.getPos();
		Set<BlockPos> connections = plinth.getConnections();
		World world = plinth.getWorld();
		for (BlockPos endPos : connections)
		{
			// ignore the attached block, only render to remote blocks
			BlockState state = plinth.getBlockState();
			if (state.has(WirePlinthBlock.DIRECTION_OF_ATTACHMENT) && endPos.equals(startPos.offset(state.get(WirePlinthBlock.DIRECTION_OF_ATTACHMENT))))
			{
				continue;
			}
			
			renderFromTo(world, new Vec3d(startPos).add(0.5D, 0.5D, 0.5D), new Vec3d(endPos).add(0.5D,0.5D,0.5D));
		}
	}
	
	public static void renderFromTo(World world, Vec3d start, Vec3d end)
	{
		double lerp = world.rand.nextDouble();
		double x = MathHelper.lerp(lerp, start.getX(), end.getX());
		double y = MathHelper.lerp(lerp, start.getY(), end.getY());
		double z = MathHelper.lerp(lerp, start.getZ(), end.getZ());
		
		world.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
	}
}
