package com.github.commoble.exmachina.client.gui;

import com.github.commoble.exmachina.common.ExMachinaMod;

import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

public class GuiElectricFurnace extends GuiFurnace
{
	private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation(ExMachinaMod.MODID, "textures/gui/container/electricfurnace.png");

	public GuiElectricFurnace(InventoryPlayer playerInv, IInventory furnaceInv)
	{
		super(playerInv, furnaceInv);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 * Overriding this so our own texture can be used
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(FURNACE_GUI_TEXTURES);
		int i = this.guiLeft;
		int j = this.guiTop;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		if (TileEntityFurnace.isBurning(this.tileFurnace))
		{
			int k = this.getBurnLeftScaled(13);
			this.drawTexturedModalRect(i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}

		int l = this.getCookProgressScaled(24);
		this.drawTexturedModalRect(i + 79, j + 34, 176, 14, l + 1, 16);
	}

	@Override
	public int getCookProgressScaled(int pixels)
	{
		int i = this.tileFurnace.getField(2);
		int j = this.tileFurnace.getField(3);
		return j != 0 && i != 0 ? i * pixels / j : 0;
	}

	@Override
	public int getBurnLeftScaled(int pixels)
	{
		int i = this.tileFurnace.getField(1);
		if (i == 0)
		{
			i = 200;
		}

		return this.tileFurnace.getField(0) * pixels / i;
	}
}
