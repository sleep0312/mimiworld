package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.entity.DualInputProcessorBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 双输入定时加工台菜单基类（电离台 / 沉淀台共用）：
 * 左列两个输入槽（上 A / 下 B，均接受任意物品）、右侧输出槽（只能取），
 * 下方为玩家背包。槽位坐标与 ionization_table.png / precipitation_table.png
 * 中槽框左上角严格一致（两张贴图槽位布局完全相同）。
 */
public abstract class DualInputProcessorMenu extends AbstractContainerMenu {

    // 机器槽（16x16 凹槽左上角像素坐标；贴图上 18x18 槽框精灵位于 (x-1,y-1)）
    public static final int SLOT_INPUT_A_X = 61;
    public static final int SLOT_INPUT_A_Y = 20;
    public static final int SLOT_INPUT_B_X = 61;
    public static final int SLOT_INPUT_B_Y = 46;
    public static final int SLOT_OUTPUT_X = 102;
    public static final int SLOT_OUTPUT_Y = 33;

    // 玩家背包槽
    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int MACHINE_SLOTS = 3;
    private static final int INV_SLOT_START = MACHINE_SLOTS;          // 3
    private static final int INV_SLOT_END = INV_SLOT_START + 27;      // 30（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;      // 39（不含）

    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** 子类提供：GUI 距离校验用的对应方块 */
    protected abstract Block validBlock();

    /** 服务端构造（由具体子类 5 参构造转发）：由 BlockEntity#createMenu 间接调用 */
    protected DualInputProcessorMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
                                     Container container, ContainerData data, ContainerLevelAccess access) {
        super(menuType, containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, DualInputProcessorBlockEntity.DATA_COUNT);
        this.access = access;
        this.data = data;

        // 输入槽 A/B：接受任意物品（工作台语义，是否参与加工由 BE 配方表决定）
        this.addSlot(new Slot(container, DualInputProcessorBlockEntity.SLOT_INPUT_A,
                SLOT_INPUT_A_X, SLOT_INPUT_A_Y));
        this.addSlot(new Slot(container, DualInputProcessorBlockEntity.SLOT_INPUT_B,
                SLOT_INPUT_B_X, SLOT_INPUT_B_Y));
        // 输出槽：只能取，不能放
        this.addSlot(new Slot(container, DualInputProcessorBlockEntity.SLOT_OUTPUT,
                SLOT_OUTPUT_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_START_X + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    /** 当前加工进度（0~加工总时长），供 Screen 绘制三角形递减动画 */
    public int getProgress() {
        return data.get(DualInputProcessorBlockEntity.DATA_PROGRESS);
    }

    /** 当前配方加工总时长（tick） */
    public int getMaxProgress() {
        return Math.max(1, data.get(DualInputProcessorBlockEntity.DATA_MAX_PROGRESS));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, validBlock());
    }

    /**
     * Shift+点击移动物品：
     * 机器槽 → 玩家背包；玩家背包 → 两个输入槽（输出槽不接收）；背包↔快捷栏互转。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack clicked = slot.getItem();
            result = clicked.copy();

            if (index < MACHINE_SLOTS) {
                // 机器槽（含输出槽产物）→ 玩家背包/快捷栏
                if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 尝试两个输入槽（0..2 不含输出槽）
                if (!moveItemStackTo(clicked, 0, MACHINE_SLOTS - 1, false)) {
                    // 背包↔快捷栏
                    if (index < INV_SLOT_END) {
                        if (!moveItemStackTo(clicked, INV_SLOT_END, HOTBAR_SLOT_END, true)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(clicked, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (clicked.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (clicked.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, clicked);
            this.broadcastChanges();
        }
        return result;
    }
}
