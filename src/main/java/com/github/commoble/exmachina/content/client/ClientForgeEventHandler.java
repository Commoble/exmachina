package com.github.commoble.exmachina.content.client;

import com.github.commoble.exmachina.ExMachinaMod;
import com.github.commoble.exmachina.content.item.WireSpoolItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
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
		MatrixStack matrices = event.getMatrixStack();
		PlayerEntity player = event.getPlayer();
		IRenderTypeBuffer buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		for (Hand hand : Hand.values())
		{
			ItemStack stack = player.getHeldItem(hand);
			if (stack.getItem() instanceof WireSpoolItem)
			{
				CompoundNBT nbt = stack.getChildTag(WireSpoolItem.LAST_PLINTH_POS);
				if (nbt != null)
				{
					Vec3d endPos = new Vec3d(NBTUtil.readBlockPos(nbt)).add(0.5d, 0.5d, 0.5d);
					double playerX = player.getPosX();
					double playerY = player.getPosY();
					double playerZ = player.getPosZ();
					double renderLerp = event.getPartialRenderTick();
					double renderX = MathHelper.lerp(renderLerp, player.prevPosX, playerX);
					double renderY = MathHelper.lerp(renderLerp, player.prevPosY, playerY);
					double renderZ = MathHelper.lerp(renderLerp, player.prevPosZ, playerZ);
					Vec3d startPos = new Vec3d(renderX, renderY+0.7D, renderZ);
					
					boolean translateSwap = false;
					if (startPos.getY() > endPos.getY())
					{
						Vec3d swap = startPos;
						startPos = endPos;
						endPos = swap;
						translateSwap = true;
					}
					
					matrices.push();

					matrices.translate(0D, 0.7D, 0D);
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
					IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.lines());
					Matrix4f fourMatrix = matrices.getLast().getPositionMatrix();
					
					if (startY <= endY)
					{
						for (int k = 0; k < 16; ++k)
						{
							float startLerp = WirePlinthRenderer.getFractionalLerp(k, 16);
							float endLerp = WirePlinthRenderer.getFractionalLerp(k + 1, 16);
							float startYLerp = WirePlinthRenderer.getYLerp(startLerp, startY, endY);
							float endYLerp = WirePlinthRenderer.getYLerp(endLerp, startY, endY);
							WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, startLerp, startYLerp);
							WirePlinthRenderer.drawNextLineSegment(dx, dy, dz, vertexBuilder, fourMatrix, endLerp, endYLerp);
						}
					}
					
					matrices.pop();
				}
			}
		}
		
	}
}
