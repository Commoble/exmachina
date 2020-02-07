package com.github.commoble.exmachina.content.client;

import java.util.Set;

import com.github.commoble.exmachina.content.wireplinth.WirePlinthBlock;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
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
	public void render(WirePlinthTileEntity plinth, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn)
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

			matrices.push();

			matrices.translate(0.5D, 0.5D, 0.5D);

			double startX = startPos.getX();
			double startY = startPos.getY();
			double startZ = startPos.getZ();

			double endX = endPos.getX();
			double endY = endPos.getY();
			double endZ = endPos.getZ();
			float dx = (float) (endX - startX);
			float dy = (float) (endY - startY);
			float dz = (float) (endZ - startZ);
			IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
			Matrix4f fourMatrix = matrices.getLast().getPositionMatrix();

			if (startY <= endY)
			{
				for (int k = 0; k < 16; ++k)
				{
					float startLerp = getFractionalLerp(k, 16);
					float endLerp = getFractionalLerp(k + 1, 16);
					float startYLerp = getYLerp(startLerp, startY, endY);
					float endYLerp = getYLerp(endLerp, startY, endY);
					drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, startLerp, startYLerp);
					drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, endLerp, endYLerp);
				}
			}

			matrices.pop();
		}
	}

	public static float getFractionalLerp(int current, int max)
	{
		return (float) current / (float) max;
	}

	public static float getYLerp(float lerp, double startY, double endY)
	{
		return (float) Math.pow(lerp, Math.log(Math.abs(endY - startY) + 3));
	}

	public static void drawNextLineSegment(float x, float y, float z, IVertexBuilder vertexBuilder, Matrix4f fourMatrix, float lerp, float yLerp)
	{
		vertexBuilder.pos(fourMatrix, x * lerp, y * (yLerp), z * lerp).color(0, 0, 0, 255).endVertex();
	}

	public static void spawnParticleFromTo(World world, Vec3d start, Vec3d end)
	{
		double lerp = world.rand.nextDouble();
		double x = MathHelper.lerp(lerp, start.getX(), end.getX());
		double y = MathHelper.lerp(lerp, start.getY(), end.getY());
		double z = MathHelper.lerp(lerp, start.getZ(), end.getZ());

		world.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
	}

	@Override
	public boolean isGlobalRenderer(WirePlinthTileEntity te)
	{
		return true;
	}
}
