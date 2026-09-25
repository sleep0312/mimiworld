package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.GrindingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 碾磨台 GUI：贴图 grinding_table.png，面板加高（176x192）。
 * 左右两列输入槽与底部输出槽之间烘有两支灰色向下箭头；白色箭头精灵
 * （留白区 UV (176,0)，8x9）锚定底边、自上而下消减（随进度填充）。
 */
public class GrindingTableScreen extends MultiInputProcessorScreen<GrindingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/grinding_table.png");

    private static final int ARROW_H = 9;
    private static final int ARROW_W = 8;
    private static final int ARROW_Y = 70;
    private static final int ARROW_LEFT_X = 76;
    private static final int ARROW_RIGHT_X = 94;
    private static final int SPRITE_U = 176;
    private static final int SPRITE_V = 0;

    public GrindingTableScreen(GrindingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 192, 104);
    }

    @Override
    protected void renderProgress(GuiGraphics graphics, int x, int y) {
        int filled = (int) Math.ceil(this.menu.getProgress() * (double) ARROW_H / this.menu.getMaxProgress());
        if (filled > 0) {
            int cut = ARROW_H - filled;
            graphics.blit(texture, x + ARROW_LEFT_X, y + ARROW_Y + cut,
                    SPRITE_U, SPRITE_V + cut, ARROW_W, filled, 256, 256);
            graphics.blit(texture, x + ARROW_RIGHT_X, y + ARROW_Y + cut,
                    SPRITE_U, SPRITE_V + cut, ARROW_W, filled, 256, 256);
        }
    }
}
