package com.github.commoble.exmachina.content.client;

import com.github.commoble.exmachina.ExMachinaMod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid=ExMachinaMod.MODID, value= {Dist.CLIENT}, bus=Bus.FORGE)
public class ClientForgeEventHandler
{
	@SubscribeEvent
	public static void onPostRenderPlayer(RenderPlayerEvent.Post event)
	{
//		MatrixStack matrices = event.getMatrixStack();
//		PlayerEntity player = event.getPlayer();
//		IRenderTypeBuffer buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
//		for (Hand hand : Hand.values())
//		{
//			ItemStack stack = player.getHeldItem(hand);
//			if (stack.getItem() instanceof WireSpoolItem)
//			{
//				CompoundNBT nbt = stack.getChildTag(WireSpoolItem.LAST_PLINTH_POS);
//				if (nbt != null)
//				{
//					Vec3d endPos = new Vec3d(NBTUtil.readBlockPos(nbt)).add(0.5d, 0.5d, 0.5d);
//					double playerX = player.getPosX();
//					double playerY = player.getPosY();
//					double playerZ = player.getPosZ();
//					double renderLerp = event.getPartialRenderTick();
//					double renderX = MathHelper.lerp(renderLerp, player.prevPosX, playerX);
//					double renderY = MathHelper.lerp(renderLerp, player.prevPosY, playerY);
//					double renderZ = MathHelper.lerp(renderLerp, player.prevPosZ, playerZ);
//					Vec3d startPos = new Vec3d(renderX, renderY+1D, renderZ);
//					
//					boolean translateSwap = false;
//					if (startPos.getY() > endPos.getY())
//					{
//						Vec3d swap = startPos;
//						startPos = endPos;
//						endPos = swap;
//						translateSwap = true;
//					}
//					
//					matrices.push();
//
//					matrices.translate(0D, 1D, 0D);
//					double startX = startPos.getX();
//					double startY = startPos.getY();
//					double startZ = startPos.getZ();
//
//					double endX = endPos.getX();
//					double endY = endPos.getY();
//					double endZ = endPos.getZ();
//					float dx = (float) (endX - startX);
//					float dy = (float) (endY - startY);
//					float dz = (float) (endZ - startZ);
//					if (translateSwap)
//					{
//						matrices.translate(-dx, -dy, -dz);
//					}
//					IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
//					Matrix4f fourMatrix = matrices.getLast().getPositionMatrix();
//					
//					if (startY <= endY)
//					{
//						for (int k = 0; k < 16; ++k)
//						{
//							float startLerp = WirePlinthRenderer.getFractionalLerp(k, 16);
//							float endLerp = WirePlinthRenderer.getFractionalLerp(k + 1, 16);
//							float startYLerp = WirePlinthRenderer.getYLerp(startLerp, startY, endY);
//							float endYLerp = WirePlinthRenderer.getYLerp(endLerp, startY, endY);
//							WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, startLerp, startYLerp);
//							WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, endLerp, endYLerp);
//						}
//					}
//					
//					matrices.pop();
//				}
//			}
//		}
		
	}
	
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event)
	{
//		Minecraft mc = Minecraft.getInstance();
//		MatrixStack matrices = event.getMatrixStack();
//		IRenderTypeBuffer buffer = mc.getRenderTypeBuffers().getBufferSource();
//		WorldRenderer renderer = event.getContext();
//		PlayerEntity player = mc.player;
//		EntityRendererManager renderManager = mc.getRenderManager();
//		float partialTicks = event.getPartialTicks();
//		int handSide = player.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
//        ItemStack itemstack = player.getHeldItemMainhand();
//        if (!(itemstack.getItem() instanceof net.minecraft.item.FishingRodItem)) {
//           handSide = -handSide;
//        }
//
//        float swingProgress = player.getSwingProgress(partialTicks);
//        float swingRotation = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
//        float renderSwingRotation = MathHelper.lerp(partialTicks, player.prevRenderYawOffset, player.renderYawOffset) * ((float)Math.PI / 180F);
//        double swingY = (double)MathHelper.sin(renderSwingRotation);
//        double swingX = (double)MathHelper.cos(renderSwingRotation);
//        double handOffset = (double)handSide * 0.35D;
//        double d3 = 0.8D;
//        double startX;
//        double startY;
//        double startZ;
//        float eyeHeight;
//        if ((renderManager.options == null || renderManager.options.thirdPersonView <= 0)) {
//           double fov = renderManager.options.fov;
//           fov = fov / 100.0D;
//           Vec3d handVector = new Vec3d((double)handSide * -0.36D * fov, -0.045D * fov, 0.4D);
//           handVector = handVector.rotatePitch(-MathHelper.lerp(partialTicks, player.prevRotationPitch, player.rotationPitch) * ((float)Math.PI / 180F));
//           handVector = handVector.rotateYaw(-MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw) * ((float)Math.PI / 180F));
//           handVector = handVector.rotateYaw(swingRotation * 0.5F);
//           handVector = handVector.rotatePitch(-swingRotation * 0.7F);
//           startX = MathHelper.lerp((double)partialTicks, player.prevPosX, player.getPosX()) + handVector.x;
//           startY = MathHelper.lerp((double)partialTicks, player.prevPosY, player.getPosY()) + handVector.y;
//           startZ = MathHelper.lerp((double)partialTicks, player.prevPosZ, player.getPosZ()) + handVector.z;
//           eyeHeight = player.getEyeHeight();
//        } else {
//           startX = MathHelper.lerp((double)partialTicks, player.prevPosX, player.getPosX()) - swingX * handOffset - swingY * 0.8D;
//           startY = player.prevPosY + (double)player.getEyeHeight() + (player.getPosY() - player.prevPosY) * (double)partialTicks - 0.45D;
//           startZ = MathHelper.lerp((double)partialTicks, player.prevPosZ, player.getPosZ()) - swingY * handOffset + swingX * 0.8D;
//           eyeHeight = player.isCrouching() ? -0.1875F : 0.0F;
//        }
//
//        double endX = MathHelper.lerp((double)partialTicks, player.prevPosX, player.getPosX());
//        double endY = MathHelper.lerp((double)partialTicks, player.prevPosY, player.getPosY()) + 0.25D;
//        double endZ = MathHelper.lerp((double)partialTicks, player.prevPosZ, player.getPosZ());
//        float dx = (float)(startX - endX);
//        float dy = (float)(startY - endY) + eyeHeight;
//        float dz = (float)(startZ - endZ);
//
//		IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
//		Matrix4f fourMatrix = matrices.getLast().getPositionMatrix();
//		
//		matrices.push();
//		
//		if (startY <= endY)
//		{
//			for (int k = 0; k < 16; ++k)
//			{
//				float startLerp = WirePlinthRenderer.getFractionalLerp(k, 16);
//				float endLerp = WirePlinthRenderer.getFractionalLerp(k + 1, 16);
//				float startYLerp = WirePlinthRenderer.getYLerp(startLerp, startY, endY);
//				float endYLerp = WirePlinthRenderer.getYLerp(endLerp, startY, endY);
//				WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, startLerp, startYLerp);
//				WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, endLerp, endYLerp);
//			}
//		}
//		
//		matrices.pop();
	}
}
