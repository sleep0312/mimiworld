package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.BloodlettingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 汲血台 GUI：贴图 bloodletting_table.png，左列两输入、两支灰色右箭头、右侧输出。
 * 上方白色箭头精灵（留白区 UV (176,0)，30x8）随进度从左向右增长。
 */
public class BloodlettingTableScreen extends MultiInputProcessorScreen<BloodlettingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/bloodletting_table.png");

    private static final int ARROW_X = 69;
    private static final int ARROW_Y = 22;
    private static final int ARROW_W = 30;
    private static final int ARROW_H = 8;
    private static final int SPRITE_U = 176;
    private static final int SPRITE_V = 0;

    public BloodlettingTableScreen(BloodlettingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 166, 72);
    }

    @Override
    protected void renderProgress(GuiGraphics graphics, int x, int y) {
        int filled = (int) Math.ceil(this.menu.getProgress() * (double) ARROW_W / this.menu.getMaxProgress());
        if (filled > 0) {
            graphics.blit(texture, x + ARROW_X, y + ARROW_Y,
                    SPRITE_U, SPRITE_V, filled, ARROW_H, 256, 256);
        }
    }
}
