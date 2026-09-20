package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.AdvancedRicePoundingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 高级锤米台 GUI：贴图 256x256，左上角 176x166 为有效画面。
 * <p>
 * 功能/槽位布局与普通锤米台完全一致（贴图同样符合 RiceMakerScreen 类头的
 * 「原版立体感」规范，3 个槽精灵与 furnace.png 源 0 mismatch）：
 * <ul>
 *   <li>输入槽 A (30,26)、输入槽 B (30,62)、输出槽 (112,26)；</li>
 *   <li>横向静态箭头烘在 (62,27)；竖向向上箭头烘在 (51,44)；</li>
 *   <li>与普通版唯一差别：装饰图标位于右上角，为金色锤子（纯装饰，<b>不是槽位</b>）；</li>
 *   <li>贴图无文字，标题/背包名走 renderLabels。</li>
 * </ul>
 */
public class AdvancedRicePoundingTableScreen extends AbstractContainerScreen<AdvancedRicePoundingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/advanced_rice_pounding_table.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;

    private static final int TITLE_COLOR = 0x404040;

    public AdvancedRicePoundingTableScreen(AdvancedRicePoundingTableMenu menu, Inventory inventory, Component title) {
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
        // 静态整图：槽位、两支箭头、金锤装饰全部烘在贴图内
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
