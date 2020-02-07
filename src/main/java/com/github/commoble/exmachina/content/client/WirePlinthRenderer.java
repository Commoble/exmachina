package com.github.commoble.exmachina.content.client;

import java.util.Set;

import com.github.commoble.exmachina.content.item.WireSpoolItem;
import com.github.commoble.exmachina.content.wireplinth.WirePlinthTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
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

	// heavily based on fishing rod line renderer
	@Override
	public void render(WirePlinthTileEntity plinth, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn)
	{
		BlockPos plinthPos = plinth.getPos();
		Vec3d plinthVector = new Vec3d(plinthPos).add(0.5D, 0.5D, 0.5D);
		Set<BlockPos> connections = plinth.getConnections();
		World world = plinth.getWorld();
		IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
		for (BlockPos connectionPos : connections)
		{
			this.renderConnection(plinth, world, partialTicks, matrices, vertexBuilder, plinthVector, new Vec3d(connectionPos).add(0.5D, 0.5D, 0.5D), 0F);
		}

		PlayerEntity player = Minecraft.getInstance().player;
		for (Hand hand : Hand.values())
		{
			ItemStack stack = player.getHeldItem(hand);
			if (stack.getItem() instanceof WireSpoolItem)
			{
				CompoundNBT nbt = stack.getChildTag(WireSpoolItem.LAST_PLINTH_POS);
				if (nbt != null)
				{
					EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
					BlockPos positionOfCurrentPlinthOfPlayer = NBTUtil.readBlockPos(nbt);
					Vec3d vectorOfCurrentPlinthOfPlayer = new Vec3d(positionOfCurrentPlinthOfPlayer).add(0.5d, 0.5d, 0.5d);
					int handSideID = -(hand == Hand.MAIN_HAND ? -1 : 1) * (player.getPrimaryHand() == HandSide.RIGHT ? 1 : -1);

					float swingProgress = player.getSwingProgress(partialTicks);
					float swingZ = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
					float playerAngle = MathHelper.lerp(partialTicks, player.prevRenderYawOffset, player.renderYawOffset) * ((float) Math.PI / 180F);
					double playerAngleX = MathHelper.sin(playerAngle);
					double playerAngleZ = MathHelper.cos(playerAngle);
					double handOffset = handSideID * 0.35D;
					double d3 = 0.8D;
					double handX;
					double handY;
					double handZ;
					float eyeHeight;
					if ((renderManager.options == null || renderManager.options.thirdPersonView <= 0))
					{
						double fov = renderManager.options.fov;
						fov = fov / 100.0D;
						Vec3d handVector = new Vec3d(handSideID * -0.36D * fov, -0.045D * fov, 0.4D);
						handVector = handVector.rotatePitch(-MathHelper.lerp(partialTicks, player.prevRotationPitch, player.rotationPitch) * ((float) Math.PI / 180F));
						handVector = handVector.rotateYaw(-MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw) * ((float) Math.PI / 180F));
						handVector = handVector.rotateYaw(swingZ * 0.5F);
						handVector = handVector.rotatePitch(-swingZ * 0.7F);
						handX = MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) + handVector.x;
						handY = MathHelper.lerp(partialTicks, player.prevPosY, player.getPosY()) + handVector.y;
						handZ = MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) + handVector.z;
						eyeHeight = player.getEyeHeight();
					}
					else
					{
						handX = MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - playerAngleZ * handOffset - playerAngleX * 0.8D;
						handY = player.prevPosY + player.getEyeHeight() + (player.getPosY() - player.prevPosY) * partialTicks - 0.45D;
						handZ = MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - playerAngleX * handOffset + playerAngleZ * 0.8D;
						eyeHeight = player.isCrouching() ? -0.1875F : 0.0F;
					}
					Vec3d renderPlayerVec = new Vec3d(handX, handY + eyeHeight, handZ);
					if (positionOfCurrentPlinthOfPlayer.equals(plinthPos))
					{
						this.renderConnection(plinth, world, partialTicks, matrices, vertexBuilder, vectorOfCurrentPlinthOfPlayer, renderPlayerVec, eyeHeight);
					}
				}
			}
		}
	}

	private void renderConnection(WirePlinthTileEntity plinth, World world, float partialTicks,
		MatrixStack matrices, IVertexBuilder vertexBuilder, Vec3d startPos, Vec3d endPos, float eyeHeight)
	{
		matrices.push();

		boolean translateSwap = false;
		if (startPos.getY() > endPos.getY())
		{
			Vec3d swap = startPos;
			startPos = endPos;
			endPos = swap;
			translateSwap = true;
		}

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
		if (translateSwap)
		{
			matrices.translate(-dx, -dy, -dz);
		}
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
