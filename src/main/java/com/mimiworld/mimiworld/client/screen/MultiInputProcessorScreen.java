package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.MultiInputProcessorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 多输入熔炉式加工台 GUI 基类（淬剑台 / 汲血台 / 搅碎器 / 碾磨台共用）。
 * <p>
 * 贴图固定 256x256，左上角 176 x {@code imageHeight} 为有效画面（标准机器 166，
 * 碾磨台加高到 192）；槽框/装饰/灰色进度底图烘在贴图里，白色进度精灵放在贴图留白区。
 * 标题代码渲染；进度动画由子类 {@link #renderProgress} 绘制。
 */
public abstract class MultiInputProcessorScreen<M extends MultiInputProcessorMenu>
        extends AbstractContainerScreen<M> {

    protected static final int IMAGE_WIDTH = 176;
    protected static final int TITLE_X = 8;
    protected static final int TITLE_Y = 6;
    protected static final int TITLE_COLOR = 0x404040;

    protected final ResourceLocation texture;
    protected final int imageH;

    protected MultiInputProcessorScreen(M menu, Inventory inventory, Component title,
                                        ResourceLocation texture, int imageHeight, int inventoryLabelY) {
        super(menu, inventory, title);
        this.texture = texture;
        this.imageH = imageHeight;
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = imageHeight;
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = inventoryLabelY;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = 8;
    }

    /** 子类绘制进度动画（机器区装饰/灰底已烘在贴图内） */
    protected abstract void renderProgress(GuiGraphics graphics, int x, int y);

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, this.leftPos, this.topPos, 0, 0,
                IMAGE_WIDTH, imageH, 256, 256);
        renderProgress(graphics, this.leftPos, this.topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, TITLE_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
