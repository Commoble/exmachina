package commoble.exmachina.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SimpleScreen<MENU extends AbstractContainerMenu> extends AbstractContainerScreen<MENU>
{
	protected final ResourceLocation backgroundTexture;
	
	public static <MENU extends AbstractContainerMenu> ScreenConstructor<MENU, SimpleScreen<MENU>> factory(ResourceLocation backgroundTexture)
	{
		return (menu, playerInventory, component) -> new SimpleScreen<>(menu, playerInventory, component, backgroundTexture, 176, 166);
	}

	public static <MENU extends AbstractContainerMenu> ScreenConstructor<MENU, SimpleScreen<MENU>> factory(ResourceLocation backgroundTexture, int imageWidth, int imageHeight)
	{
		return (menu, playerInventory, component) -> new SimpleScreen<>(menu, playerInventory, component, backgroundTexture, imageWidth, imageHeight);
	}
	
	public SimpleScreen(MENU menu, Inventory playerInventory, Component component, ResourceLocation backgroundTexture, int imageWidth, int imageHeight)
	{
		super(menu, playerInventory, component);
		this.backgroundTexture = backgroundTexture;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(this.backgroundTexture, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
}
