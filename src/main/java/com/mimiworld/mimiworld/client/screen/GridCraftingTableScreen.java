package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCraftingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * NxN 网格工作台式 GUI 基类（杵界台 2x2 / 凝锭台 5x5 等共用）。
 * <p>
 * 使用各机器自绘贴图（256x256，左上角 imageWidth x imageHeight 为有效画面），
 * 网格槽、箭头、结果槽全部烘在贴图内（即时合成无进度条），本 Screen 只做整图
 * blit + 代码渲染标题与背包名。
 */
public abstract class GridCraftingTableScreen<M extends GridCraftingTableMenu>
        extends AbstractContainerScreen<M> {

    private static final int IMAGE_WIDTH = 176;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int TITLE_COLOR = 0x404040;

    private final ResourceLocation texture;
    private final int imageH;
    private final int invLabelY;

    protected GridCraftingTableScreen(M menu, Inventory inventory, Component title,
                                      String texturePath, int imageHeight, int inventoryLabelY) {
        super(menu, inventory, title);
        this.texture = new ResourceLocation("mimiworld", "textures/gui/" + texturePath);
        this.imageH = imageHeight;
        this.invLabelY = inventoryLabelY;
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = imageHeight;
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = inventoryLabelY;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = invLabelY;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 槽位、箭头全部烘在各机器自绘贴图内（即时合成无需进度精灵）
        graphics.blit(texture, this.leftPos, this.topPos, 0, 0,
                IMAGE_WIDTH, imageH, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int machineY) {
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
