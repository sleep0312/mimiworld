package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.ForgingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 装备锻造台 GUI 基类（铸剑台 / 铸弓台 / 铸甲台共用）。
 * <p>
 * 界面与原版工作台完全一致：直接使用原版贴图
 * {@code minecraft:textures/gui/container/crafting_table.png}（256x256，左上角
 * 176x166 为有效画面），3x3 网格、箭头、结果槽全部烘在原版贴图内，本 Screen 只做
 * 整图 blit + 代码渲染标题（标题为各锻造台自己的名字，如"铸剑台"）。
 */
public abstract class ForgingTableScreen<M extends ForgingTableMenu> extends AbstractContainerScreen<M> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;
    private static final int TITLE_COLOR = 0x404040;

    protected ForgingTableScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 原版工作台贴图：槽位、箭头全部烘在图内
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0,
                IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
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
