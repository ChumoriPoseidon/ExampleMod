package com.example.examplemod.container;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExampleProcessorScreen extends ContainerScreen<ExampleProcessorContainer> {

	private static final ResourceLocation PROCESSOR_GUI_TEXTURES = new ResourceLocation(ExampleMod.MODID, "textures/gui/container/block_example_processor.png");

	public ExampleProcessorScreen(ExampleProcessorContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(PROCESSOR_GUI_TEXTURES);
		int i = this.guiLeft;
		int j = this.guiTop;
		this.blit(i, j, 0, 0, this.xSize, this.ySize);
		if(((ExampleProcessorContainer)this.container).isBurning()) {
			int k = ((ExampleProcessorContainer)this.container).getBurnLeftScaled();
			this.blit(i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		int l = ((ExampleProcessorContainer)this.container).getCookProgressionScaled();
		this.blit(i + 79, j + 34, 176, 14, l + 1, 16);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String displayTitle = this.title.getFormattedText();
		this.font.drawString(displayTitle, (float)(this.xSize / 2 - this.font.getStringWidth(displayTitle) / 2), 6.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(this.ySize - 96 + 2), 4210752);
	}
}
