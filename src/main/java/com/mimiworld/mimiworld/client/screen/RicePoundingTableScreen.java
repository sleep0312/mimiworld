package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.RicePoundingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 锤米台 GUI：贴图 256x256，左上角 176x166 为有效画面。
 * <p>
 * 贴图 rice_pounding_table.png 按模组机器 GUI 的「原版立体感」规范生成：
 * furnace.png 176x166 面板 + (3,3,170,75) 擦机器区 + 3 个原版 18x18 槽精灵
 * （源 (7,83)，置于槽坐标 (x-1,y-1)）。本机器为工作台式即时
 * 合成，没有进度/液体等动态元素，因此本 Screen 只做整图 blit + 代码渲染标题：
 * <ul>
 *   <li>输入槽 A (30,26)、输入槽 B (30,62)、输出槽 (112,26)；</li>
 *   <li>横向静态箭头：furnace (79,35) 24x17 烘在 (62,27)，指向输出槽；</li>
 *   <li>竖向向上箭头：10x16 纯 #8B8B8B（与凹陷箭头同灰）烘在 (51,44)，
 *       位于两个输入槽之间，表示下槽原料向上汇入；</li>
 *   <li>右下角 (112,55) 16x16 木槌像素图纯装饰，<b>不是槽位</b>（Menu 中无对应 Slot，
 *       物品图标不会渲染其上）；</li>
 *   <li>贴图无文字，标题/背包名走 renderLabels。</li>
 * </ul>
 */
public class RicePoundingTableScreen extends AbstractContainerScreen<RicePoundingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/rice_pounding_table.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;

    private static final int TITLE_COLOR = 0x404040;

    public RicePoundingTableScreen(RicePoundingTableMenu menu, Inventory inventory, Component title) {
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
        // 静态整图：槽位、两支箭头、木槌图标全部烘在贴图内
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
