package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.SmallCookingPotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 小煮锅 GUI：原版熔炉布局（176x166）。
 * 贴图内已烘灰色火焰底（(56,36) 14x14）与灰色箭头底（(80,35) 24x17）；
 * 点亮的橙色火焰精灵在留白区 UV(176,0)，白色进度箭头精灵在 UV(176,14)。
 */
public class SmallCookingPotScreen extends AbstractContainerScreen<SmallCookingPotMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mimiworld", "textures/gui/small_cooking_pot.png");

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;
    private static final int TITLE_COLOR = 0x404040;

    private static final int FLAME_X = 56;
    private static final int FLAME_Y = 36;
    private static final int FLAME_SIZE = 14;
    private static final int ARROW_X = 80;
    private static final int ARROW_Y = 35;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    // 留白区精灵 UV
    private static final int SPRITE_FLAME_U = 176;
    private static final int SPRITE_FLAME_V = 0;
    private static final int SPRITE_ARROW_U = 176;
    private static final int SPRITE_ARROW_V = 14;

    public SmallCookingPotScreen(SmallCookingPotMenu menu, Inventory inventory, Component title) {
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
        graphics.blit(TEXTURE, x, y, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

        // 点亮火焰（从底部向上增长）
        int litDuration = this.menu.getLitDuration();
        if (this.menu.getLitTime() > 0 && litDuration > 0) {
            int litHeight = FLAME_SIZE * this.menu.getLitTime() / litDuration;
            if (litHeight > 0) {
                graphics.blit(TEXTURE,
                        x + FLAME_X, y + FLAME_Y + FLAME_SIZE - litHeight,
                        SPRITE_FLAME_U, SPRITE_FLAME_V + FLAME_SIZE - litHeight,
                        FLAME_SIZE, litHeight, 256, 256);
            }
        }

        // 烹煮进度箭头（从左向右增长）
        int maxProgress = this.menu.getMaxProgress();
        int progress = this.menu.getProgress();
        if (progress > 0) {
            int arrowWidth = ARROW_WIDTH * progress / maxProgress;
            if (arrowWidth > 0) {
                graphics.blit(TEXTURE,
                        x + ARROW_X, y + ARROW_Y,
                        SPRITE_ARROW_U, SPRITE_ARROW_V,
                        arrowWidth, ARROW_HEIGHT, 256, 256);
            }
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
