package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.entity.MultiInputProcessorBlockEntity;
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
 * 多输入熔炉式加工台菜单基类（淬剑台 / 汲血台 / 搅碎器 / 碾磨台共用）。
 * <p>
 * 子类通过 {@link #addMachineSlots} 定义各自的机器槽布局（坐标必须与对应 GUI 贴图
 * 槽框左上角一致）；玩家背包起始 Y 可覆写 {@link #playerInventoryY()}（碾磨台加高面板）。
 * 输出槽固定为机器槽最后一个，只能取不能放（产物由方块实体加工产生）。
 */
public abstract class MultiInputProcessorMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_X = 8;

    /** 开始按钮的 buttonId（clickMenuButton 通道，无需自定义网络包） */
    public static final int BUTTON_START = 0;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final int inputCount;

    /** 子类提供：GUI 距离校验用的对应方块 */
    protected abstract Block validBlock();

    /** 子类提供：放置机器槽（输入槽用 addInputSlot，输出槽用 addResultSlot） */
    protected abstract void addMachineSlots(Container container);

    /** 玩家背包起始 Y（默认与其他机器一致 84；加高面板的机器覆写） */
    protected int playerInventoryY() {
        return 84;
    }

    /** 服务端构造（由具体子类转发） */
    protected MultiInputProcessorMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
                                      Container container, ContainerData data, ContainerLevelAccess access) {
        super(menuType, containerId);
        checkContainerSize(container, container.getContainerSize());
        checkContainerDataCount(data, MultiInputProcessorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;
        this.inputCount = container.getContainerSize() - 1;

        addMachineSlots(container);

        int invY = playerInventoryY();
        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, invY + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, invY + 58));
        }

        addDataSlots(data);
    }

    /** 添加一个输入槽（任意物品可放） */
    protected void addInputSlot(Container container, int index, int x, int y) {
        addSlot(new Slot(container, index, x, y));
    }

    /** 添加输出槽（只能取，不能放） */
    protected void addResultSlot(Container container, int index, int x, int y) {
        addSlot(new Slot(container, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    /** 当前加工进度（0~加工总时长） */
    public int getProgress() {
        return data.get(MultiInputProcessorBlockEntity.DATA_PROGRESS);
    }

    /** 当前配方加工总时长（tick） */
    public int getMaxProgress() {
        return Math.max(1, data.get(MultiInputProcessorBlockEntity.DATA_MAX_PROGRESS));
    }

    /** 是否处于运行态（淬剑台开始按钮按下后为 true） */
    public boolean isRunning() {
        return data.get(MultiInputProcessorBlockEntity.DATA_RUNNING) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START && container instanceof MultiInputProcessorBlockEntity be) {
            be.startProcessing();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, validBlock());
    }

    /**
     * Shift+点击：机器槽（含输出产物）→ 玩家背包；玩家背包 → 各输入槽（输出槽不接收）；
     * 背包↔快捷栏互转。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int machineSlots = inputCount + 1;
        int invStart = machineSlots;
        int invEnd = invStart + 27;
        int hotbarEnd = invEnd + 9;

        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack clicked = slot.getItem();
            result = clicked.copy();

            if (index < machineSlots) {
                if (!moveItemStackTo(clicked, invStart, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 输入槽（不含最后一个输出槽）
                if (!moveItemStackTo(clicked, 0, inputCount, false)) {
                    if (index < invEnd) {
                        if (!moveItemStackTo(clicked, invEnd, hotbarEnd, true)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(clicked, invStart, invEnd, false)) {
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
            broadcastChanges();
        }
        return result;
    }
}
