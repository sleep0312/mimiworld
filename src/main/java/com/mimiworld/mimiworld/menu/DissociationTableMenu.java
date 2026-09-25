package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 离解台菜单：熔炉式三槽布局 + 下方玩家背包。槽位坐标与
 * dissociation_table.png 中槽框左上角严格一致：
 * <ul>
 *   <li>槽 0：上方输入槽（待离解原料）(35,24)；</li>
 *   <li>槽 1：输出槽（只能取）(121,24)；</li>
 *   <li>槽 2：下方输入槽（离解介质）(79,53)。</li>
 * </ul>
 */
public class DissociationTableMenu extends AbstractContainerMenu {

    // 机器槽（16x16 凹槽左上角像素坐标；贴图上 18x18 槽框精灵位于 (x-1,y-1)）
    public static final int SLOT_INPUT_X = 35;
    public static final int SLOT_INPUT_Y = 24;
    public static final int SLOT_OUTPUT_X = 121;
    public static final int SLOT_OUTPUT_Y = 24;
    public static final int SLOT_INPUT_BOTTOM_X = 79;
    public static final int SLOT_INPUT_BOTTOM_Y = 53;

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

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器/数据使用本地空实现（槽位同步照常工作） */
    public DissociationTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(DissociationTableBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(DissociationTableBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 DissociationTableBlockEntity#createMenu 调用 */
    public DissociationTableMenu(int containerId, Inventory playerInventory,
                                 Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.DISSOCIATION_TABLE_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, DissociationTableBlockEntity.DATA_COUNT);
        this.access = access;
        this.data = data;

        // 上方输入槽：接受任意物品（是否参与加工由 BE 配方表决定）
        this.addSlot(new Slot(container, DissociationTableBlockEntity.SLOT_INPUT,
                SLOT_INPUT_X, SLOT_INPUT_Y));
        // 输出槽：只能取，不能放
        this.addSlot(new Slot(container, DissociationTableBlockEntity.SLOT_OUTPUT,
                SLOT_OUTPUT_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        // 下方输入槽
        this.addSlot(new Slot(container, DissociationTableBlockEntity.SLOT_INPUT_BOTTOM,
                SLOT_INPUT_BOTTOM_X, SLOT_INPUT_BOTTOM_Y));

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

    /** 当前加工进度（0~加工总时长），供 Screen 绘制横箭头增长动画 */
    public int getProgress() {
        return data.get(DissociationTableBlockEntity.DATA_PROGRESS);
    }

    /** 当前配方加工总时长（tick） */
    public int getMaxProgress() {
        return Math.max(1, data.get(DissociationTableBlockEntity.DATA_MAX_PROGRESS));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.DISSOCIATION_TABLE.get());
    }

    /**
     * Shift+点击移动物品：
     * 机器槽 → 玩家背包；玩家背包 → 先上输入槽、再下输入槽（输出槽不接收）；背包↔快捷栏互转。
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
                // 玩家背包 → 先上方输入槽（0），满了再下方输入槽（2）
                if (!moveItemStackTo(clicked, 0, 1, false)
                        && !moveItemStackTo(clicked, 2, 3, false)) {
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
