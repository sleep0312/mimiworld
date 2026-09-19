package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.RiceMakerBlockEntity;
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
 * 造米台菜单：左 2 个输入槽（上下排列），右 1 个输出槽；下方为玩家背包。
 * 槽位坐标与 rice_maker.png 中槽框左上角严格一致。
 */
public class RiceMakerMenu extends AbstractContainerMenu {

    // 机器槽（这里是 16x16 凹槽左上角的像素坐标；贴图上 18x18 槽框精灵位于
    // (x-1,y-1)，完整贴图绘制/拼贴规范见 RiceMakerScreen 类头注释）
    public static final int SLOT_INPUT_1_X = 30;
    public static final int SLOT_INPUT_1_Y = 26;
    public static final int SLOT_INPUT_2_X = 30;
    public static final int SLOT_INPUT_2_Y = 62;
    public static final int SLOT_OUTPUT_X = 112;
    public static final int SLOT_OUTPUT_Y = 43;

    // 玩家背包槽
    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int MACHINE_SLOTS = 3;
    private static final int INV_SLOT_START = MACHINE_SLOTS;          // 3
    private static final int INV_SLOT_END = INV_SLOT_START + 27;      // 30（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;      // 39（不含）

    private final Container container;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器/数据使用本地空实现（槽位同步照常工作） */
    public RiceMakerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        // 客户端容器：槽位内容由网络同步；本地进度数据会被 DataSlot 同步覆盖
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOTS),
                new SimpleContainerData(2), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 RiceMakerBlockEntity#createMenu 调用 */
    public RiceMakerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.RICE_MAKER_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.access = access;
        this.data = data;

        // 2 个输入槽
        addSlot(new Slot(container, RiceMakerBlockEntity.SLOT_INPUT_1, SLOT_INPUT_1_X, SLOT_INPUT_1_Y));
        addSlot(new Slot(container, RiceMakerBlockEntity.SLOT_INPUT_2, SLOT_INPUT_2_X, SLOT_INPUT_2_Y));
        // 输出槽：只能取，不能放
        addSlot(new Slot(container, RiceMakerBlockEntity.SLOT_OUTPUT, SLOT_OUTPUT_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_START_X + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }

        addDataSlots(data);
    }

    /** 当前加工进度（0~MAX_PROGRESS），供 Screen 绘制箭头填充 */
    public int getProgress() {
        return data.get(0);
    }

    /** 加工总进度（tick） */
    public int getMaxProgress() {
        return Math.max(1, data.get(1));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RICE_MAKER.get());
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
                // 机器槽（含输出槽）→ 玩家背包/快捷栏
                if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 仅尝试两个输入槽
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
