package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.DissociationTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 离解台 GUI：贴图 dissociation_table.png（256x256，左上角 176x166 为有效画面）。
 * <p>
 * 熔炉式布局：左上输入槽 → 横箭头 → 右上输出槽，正下方为第二输入槽（三个向上箭头为装饰）。
 * 槽框/灰色箭头底/上箭头均烘在贴图里，标题由代码渲染；横箭头上方叠加<b>从左向右增长</b>
 * 的白色箭头精灵（位于贴图留白区 UV (176,0)，24x17）表示加工进度，与原版熔炉箭头一致。
 */
public class DissociationTableScreen extends AbstractContainerScreen<DissociationTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/dissociation_table.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    // 横箭头在 GUI 内的位置（灰底与白色精灵共同的 bbox）
    private static final int ARROW_X = 75;
    private static final int ARROW_Y = 26;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    // 纯白箭头精灵在 256x256 贴图留白区中的 UV
    private static final int ARROW_SPRITE_U = 176;
    private static final int ARROW_SPRITE_V = 0;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;
    private static final int TITLE_COLOR = 0x404040;

    public DissociationTableScreen(DissociationTableMenu menu, Inventory inventory, Component title) {
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
        int x = this.leftPos;
        int y = this.topPos;

        // GUI 底图（仅左上角 176x166 有画面，其余透明）
        graphics.blit(TEXTURE, x, y, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

        // 白色箭头：从左向右增长，宽度按进度比例裁切（与原版熔炉进度箭头一致）
        int filled = (int) Math.ceil(this.menu.getProgress() * (double) ARROW_WIDTH / this.menu.getMaxProgress());
        if (filled > 0) {
            graphics.blit(TEXTURE,
                    x + ARROW_X, y + ARROW_Y,
                    ARROW_SPRITE_U, ARROW_SPRITE_V,
                    filled, ARROW_HEIGHT,
                    256, 256);
        }
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
