package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.DualInputProcessorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 双输入定时加工台 GUI 基类（电离台 / 沉淀台共用）。
 * <p>
 * 贴图 256x256，左上角 176x166 为有效画面（贴图绘制规范见 RiceMakerScreen 类头注释）；
 * 槽框/横箭头/灰色三角形底图均烘在贴图里，标题与白色三角形动画由代码渲染。
 * <p>
 * <b>三角形动画（本类核心）：</b>两输入槽之间的三角形（电离台倒三角 ▼、沉淀台正三角 ▲）
 * 是加工进度指示，语义对标原版熔炉的火焰：未加工时显示完整白色三角形，随加工推进
 * 白色慢慢变少，加工完成瞬间白色消失（露出贴图上的灰色底图）并在下一周期重新填满。
 * <p>
 * 裁切数学与熔炉火焰逐字对应（火焰锚定燃料槽、高度从满到 0）：三角形<b>锚定底边</b>
 * （靠近下方输入槽的那条边）固定不动，从顶部自由端开始一行行消失 —— 取白色精灵
 * 下部 remaining 行 blit（源/目标都偏移 cut 行）。白色精灵位于贴图留白区 UV (0,200)，
 * 尺寸 16x8（与面板内灰三角逐像素一致，仅形状是纯白；bbox 内非三角像素全透明，
 * 不会盖住旁边的横箭头）。
 */
public abstract class DualInputProcessorScreen<M extends DualInputProcessorMenu>
        extends AbstractContainerScreen<M> {

    protected final ResourceLocation texture;

    // 与贴图一致的画面尺寸（严格 176x166）
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    // 三角形在 GUI 内的位置/尺寸（灰三角底图与白色精灵共同的 bbox）
    private static final int TRI_X = 61;
    private static final int TRI_Y = 37;
    private static final int TRI_WIDTH = 16;
    private static final int TRI_HEIGHT = 8;
    // 纯白三角形精灵在 256x256 贴图留白区中的 UV
    private static final int TRI_SPRITE_U = 0;
    private static final int TRI_SPRITE_V = 200;

    // 标题位置（贴图上无任何文字，全部代码渲染）
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;
    private static final int TITLE_COLOR = 0x404040;

    protected DualInputProcessorScreen(M menu, Inventory inventory,
                                       Component title, ResourceLocation texture) {
        super(menu, inventory, title);
        this.texture = texture;
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
        graphics.blit(texture, x, y, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

        // 白色三角形：未加工 remaining=8 全白；随加工推进从顶部自由端逐行消失；完成时不绘制
        int remaining = TRI_HEIGHT
                - (int) Math.ceil(this.menu.getProgress() * (double) TRI_HEIGHT / this.menu.getMaxProgress());
        if (remaining > 0) {
            int cut = TRI_HEIGHT - remaining;
            graphics.blit(texture,
                    x + TRI_X, y + TRI_Y + cut,
                    TRI_SPRITE_U, TRI_SPRITE_V + cut,
                    TRI_WIDTH, remaining,
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
