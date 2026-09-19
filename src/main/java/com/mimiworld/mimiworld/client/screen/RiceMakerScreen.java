package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.RiceMakerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 造米台 GUI：贴图 256x256，左上角 176x166 为有效画面；
 * 槽框/箭头底图均在贴图内，标题与进度箭头填充由代码渲染。
 *
 * ============================================================================
 * 【GUI 贴图绘制规范 —— 本项目所有机器方块（搅碎器/搅拌器/烹饪锅等）照此复用】
 * ----------------------------------------------------------------------------
 * 目标：与原版 100% 一致的立体感。核心原则是「不要手画斜面」，直接从原版
 * furnace.png 逐像素拷贝精灵拼贴。手画的近似色槽位会变成凸起黑块，没有原版
 * 的凹陷感，用户已验证：逐像素拷贝原版素材的版本才合格。
 *
 * 1. 素材来源（原版 1.20.1 jar 内，256x256）：
 *    ~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar
 *    entry: assets/minecraft/textures/gui/container/furnace.png
 *    用 PowerShell + System.Drawing 提取并 DrawImage 拼贴（InterpolationMode=
 *    NearestNeighbor，禁用缩放平滑）。
 *
 * 2. 画布规则：
 *    - PNG 必须 256x256（2 的幂），GUI 画面放左上角，其余区域全透明留白；
 *      留白区可藏动态裁切用的精灵（如亮色进度箭头）。
 *    - 面板画面 176x166（= 熔炉面板尺寸），imageWidth/imageHeight 必须严格
 *      等于该值。
 *    - 贴图上【不画任何文字】（标题/背包名全部 renderLabels 代码渲染）；
 *      【不画物品图标】；进度条只画底图，填充由代码动态裁切。
 *
 * 3. 拼贴步骤：
 *    a. 整张拷贝 furnace.png 的 (0,0)-(175,165) 作为面板（自带原版斜面边框：
 *       外沿 #000000、左上高光 #FFFFFF、右下暗影 #555555，面板填充 #C6C6C6）；
 *    b. 用 #C6C6C6 填充矩形 (3,3,170,75) 擦掉熔炉原有的输入槽/燃料槽/火焰/箭头，
 *       注意保留 3px 外边框，且不要动 y>=83 的玩家背包 36 个槽（直接复用原位）；
 *    c. 机器槽用原版 18x18 槽精灵 1:1 盖印，源矩形取 furnace.png 的 (7,83,18,18)
 *       （即玩家背包槽的外沿框，与熔炉输入槽完全同款）。
 *
 * 4. 槽位几何（最容易错的一像素）：
 *    Menu 里 addSlot(new Slot(...)) 的坐标是凹槽 16x16 的【左上角】；
 *    贴图上 18x18 槽框精灵的左上角在 (slotX-1, slotY-1)。
 *    原版槽结构（18x18）：顶/左各 1px #373737 暗影边 → 中间 16x16 #8B8B8B 凹陷
 *    井面 → 右/下各 1px #FFFFFF 高光边（左下角像素例外为 #8B8B8B，直接拷贝
 *    即可不用记）。物品图标由游戏渲染在凹槽内 (slotX+1,slotY+1)，悬停高亮也
 *    正好覆盖 16x16 凹槽，所以精灵必须按 (x-1,y-1) 放置才能严丝合缝。
 *
 * 5. 进度箭头（本类的实际用法）：
 *    - 底图（烘在面板里）：furnace.png (79,35) 24x17，是凹陷 #8B8B8B 箭头，
 *      不是黑色；盖到面板 (62,44)。
 *    - 亮色填充精灵：furnace.png (176,14) 24x17（画面外留白区的白色箭头，
 *      自带少量 #555555 阴影像素和 #C6C6C6 背景，原样拷贝即可，blit 时背景
 *      灰与面板灰融合不可见），放到本贴图留白区 (0,200)；
 *      renderBg 中按进度横向裁切宽度 = ceil(progress*24/maxProgress)。
 *
 * 6. 验收：生成后逐像素对比新贴图目标矩形与 furnace 源矩形，必须 0 mismatch
 *    （GetPixel 双重 for 循环逐 ARGB 比较）。256x256 尺寸也要断言。
 *    改完贴图进游戏按 F3+T 重载资源即可，无需重启。
 * ============================================================================
 */
public class RiceMakerScreen extends AbstractContainerScreen<RiceMakerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/rice_maker.png");

    // 与贴图一致的画面尺寸（严格 176x166）
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    // 箭头在 GUI 内的位置/尺寸，与贴图中的底图严格对齐
    private static final int ARROW_X = 62;
    private static final int ARROW_Y = 44;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    // 完整亮色箭头精灵在 256x256 贴图中的 UV（位于 GUI 画面之外的留白区）
    private static final int ARROW_FILL_U = 0;
    private static final int ARROW_FILL_V = 200;

    // 标题位置（贴图上无任何文字，全部代码渲染）
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 72;

    private static final int TITLE_COLOR = 0x404040;

    public RiceMakerScreen(RiceMakerMenu menu, Inventory inventory, Component title) {
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

        // 进度箭头：按加工进度横向裁切亮色箭头精灵
        int fillWidth = (int) Math.ceil(this.menu.getProgress() * (double) ARROW_WIDTH / this.menu.getMaxProgress());
        if (fillWidth > 0) {
            graphics.blit(TEXTURE,
                    x + ARROW_X, y + ARROW_Y,
                    ARROW_FILL_U, ARROW_FILL_V,
                    fillWidth, ARROW_HEIGHT,
                    256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TITLE_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
