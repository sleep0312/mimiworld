package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.RicePressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 压饭机 GUI：贴图 256x256，左上角 176x166 为有效画面。
 * <p>
 * 贴图 rice_press.png 按 RiceMakerScreen 类头固化的「原版立体感」规范生成：
 * furnace.png 176x166 面板 + (3,3,170,75) 擦机器区 + 10 个原版 18x18 槽精灵
 * （源 (7,83)，置于槽坐标 (x-1,y-1)）。本机器为工作台式即时合成，无进度/液体
 * 动态元素，Screen 只做整图 blit + 代码渲染标题：
 * <ul>
 *   <li>9 个输入槽横排：槽坐标 x=9+col*18（col 0..8）、y=18；</li>
 *   <li>竖向下箭头：10x14 纯 #8B8B8B（与凹陷箭头同灰）烘在 (83,38)，
 *       位于输入排与输出槽之间，中线对齐；</li>
 *   <li>输出槽 (81,54)；玩家背包区直接复用 furnace 原槽位 (8,84)/(8,142)；</li>
 *   <li>贴图无文字，标题/背包名走 renderLabels。</li>
 * </ul>
 */
public class RicePressScreen extends AbstractContainerScreen<RicePressMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/rice_press.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;

    private static final int TITLE_COLOR = 0x404040;

    public RicePressScreen(RicePressMenu menu, Inventory inventory, Component title) {
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
        // 静态整图：9 输入槽、下箭头、输出槽全部烘在贴图内
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
