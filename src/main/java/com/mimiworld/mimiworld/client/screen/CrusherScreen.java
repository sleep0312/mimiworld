package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.CrusherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 搅碎器 GUI：贴图 crusher.png。中央上方为装饰性捣碎锤像素图（无槽位），
 * 灰色横箭头指向右下输出槽；白色箭头精灵（留白区 UV (176,0)，32x10）随进度
 * 从左向右增长。
 */
public class CrusherScreen extends MultiInputProcessorScreen<CrusherMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/crusher.png");

    private static final int ARROW_X = 62;
    private static final int ARROW_Y = 53;
    private static final int ARROW_W = 32;
    private static final int ARROW_H = 10;
    private static final int SPRITE_U = 176;
    private static final int SPRITE_V = 0;

    public CrusherScreen(CrusherMenu menu, Inventory inventory, Component title) {
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
