package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.MultiInputProcessorMenu;
import com.mimiworld.mimiworld.menu.SwordQuenchingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 淬剑台 GUI：贴图 sword_quenching_table.png。
 * <ul>
 *   <li>上排两个输入槽汇入中间槽的括号线条、中间槽到输出槽的三支灰色下箭头均烘在贴图内；</li>
 *   <li>右侧"开始"按钮烘在 (122,41) 42x20；加工运行时叠加白色高亮精灵
 *       （留白区 UV (176,8)）；</li>
 *   <li>三支下箭头中间一支叠加白色精灵（留白区 UV (176,0)，6x6），锚定底边随进度填充；</li>
 *   <li>点击按钮走原版 clickMenuButton 通道（{@code gameMode.handleInventoryButtonClick}），
 *       服务端 {@link MultiInputProcessorMenu#clickMenuButton} 启动一轮加工。</li>
 * </ul>
 */
public class SwordQuenchingTableScreen extends MultiInputProcessorScreen<SwordQuenchingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/sword_quenching_table.png");

    // 开始按钮（GUI 内坐标，与贴图一致）
    private static final int BTN_X = 122;
    private static final int BTN_Y = 41;
    private static final int BTN_W = 42;
    private static final int BTN_H = 20;
    private static final int BTN_SPRITE_U = 176;
    private static final int BTN_SPRITE_V = 8;

    // 中间下箭头进度精灵
    private static final int ARROW_X = 86;
    private static final int ARROW_Y = 54;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 6;
    private static final int ARROW_SPRITE_U = 176;
    private static final int ARROW_SPRITE_V = 0;

    public SwordQuenchingTableScreen(SwordQuenchingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 166, 72);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
                && mouseX >= this.leftPos + BTN_X && mouseX < this.leftPos + BTN_X + BTN_W
                && mouseY >= this.topPos + BTN_Y && mouseY < this.topPos + BTN_Y + BTN_H) {
            // 服务端菜单 clickMenuButton 收到 BUTTON_START 后启动一轮加工
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
                        MultiInputProcessorMenu.BUTTON_START);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderProgress(GuiGraphics graphics, int x, int y) {
        // 运行中：按钮白色高亮
        if (this.menu.isRunning()) {
            graphics.blit(texture, x + BTN_X, y + BTN_Y,
                    BTN_SPRITE_U, BTN_SPRITE_V, BTN_W, BTN_H, 256, 256);
        }
        // 中间下箭头：锚定底边自上而下填充
        int filled = (int) Math.ceil(this.menu.getProgress() * (double) ARROW_H / this.menu.getMaxProgress());
        if (filled > 0) {
            int cut = ARROW_H - filled;
            graphics.blit(texture, x + ARROW_X, y + ARROW_Y + cut,
                    ARROW_SPRITE_U, ARROW_SPRITE_V + cut, ARROW_W, filled, 256, 256);
        }
    }
}
