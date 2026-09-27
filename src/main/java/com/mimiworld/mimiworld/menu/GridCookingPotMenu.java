package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.entity.GridCookingPotBlockEntity;
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
import net.minecraftforge.common.ForgeHooks;

/**
 * NxN 网格煮锅菜单基类（煮锅 / 大煮锅 / 超大煮锅 / 巨无霸大大大煮锅共用）。
 * <p>
 * 槽位几何由构造参数给出（与各锅自绘 GUI 贴图逐像素对齐）：
 * <ul>
 *   <li>NxN 输入网格：(gridX+col*pitch, gridY+row*pitch)，索引 0~N*N-1（行优先）；</li>
 *   <li>右上单独输入槽：(extraX,extraY)，索引 N*N；</li>
 *   <li>燃料槽：(fuelX,fuelY)，索引 N*N+1，仅收可燃烧物品；</li>
 *   <li>输出槽：(extraX,resultY)，索引 N*N+2，只取不放；</li>
 *   <li>玩家背包 (8,playerInvY) 起 3x9，快捷栏 playerInvY+58。</li>
 * </ul>
 */
public abstract class GridCookingPotMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_X = 8;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    private final int machineSlots;
    private final int extraSlot;
    private final int fuelSlot;
    private final int resultSlot;
    private final int invSlotStart;
    private final int invSlotEnd;
    private final int hotbarSlotEnd;

    /** 子类提供：GUI 距离校验用的对应方块 */
    protected abstract Block validBlock();

    protected GridCookingPotMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
                                 Container container, ContainerData data, ContainerLevelAccess access,
                                 int gridSize, int gridX, int gridY, int pitch,
                                 int extraX, int extraY, int resultY,
                                 int fuelX, int fuelY, int playerInvY) {
        super(menuType, containerId);
        this.extraSlot = gridSize * gridSize;
        this.fuelSlot = extraSlot + 1;
        this.resultSlot = fuelSlot + 1;
        this.machineSlots = resultSlot + 1;
        checkContainerSize(container, machineSlots);
        checkContainerDataCount(data, GridCookingPotBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;
        int invStart = machineSlots;
        this.invSlotStart = invStart;
        this.invSlotEnd = invStart + 27;
        this.hotbarSlotEnd = invSlotEnd + 9;

        // NxN 输入网格（任意物品可放，能否烹煮由 BE 配方表决定）
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                addSlot(new Slot(container, row * gridSize + col,
                        gridX + col * pitch, gridY + row * pitch));
            }
        }
        // 右上单独输入槽
        addSlot(new Slot(container, extraSlot, extraX, extraY));
        // 燃料槽：只收可燃烧物品
        addSlot(new Slot(container, fuelSlot, fuelX, fuelY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });
        // 输出槽：只取不放
        addSlot(new Slot(container, resultSlot, extraX, resultY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, playerInvY + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_X + col * 18, playerInvY + 58));
        }

        this.addDataSlots(data);
    }

    // -------------------- 屏幕渲染读取 --------------------

    public int getLitTime() {
        return data.get(GridCookingPotBlockEntity.DATA_LIT_TIME);
    }

    public int getLitDuration() {
        return data.get(GridCookingPotBlockEntity.DATA_LIT_DURATION);
    }

    public int getProgress() {
        return data.get(GridCookingPotBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        int max = data.get(GridCookingPotBlockEntity.DATA_MAX_PROGRESS);
        return max > 0 ? max : GridCookingPotBlockEntity.DEFAULT_COOK_TICKS;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, validBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack clicked = slot.getItem();
        result = clicked.copy();

        if (index < machineSlots) {
            // 机器槽（含输出）→ 玩家背包/快捷栏
            if (!moveItemStackTo(clicked, invSlotStart, hotbarSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(clicked, result);
        } else {
            // 玩家背包 → 机器：可燃烧物品优先进燃料槽，其余进网格槽与右上输入槽
            if (ForgeHooks.getBurnTime(clicked, null) > 0) {
                if (!moveItemStackTo(clicked, fuelSlot, fuelSlot + 1, false)
                        && !moveItemStackTo(clicked, 0, resultSlot, false)) {
                    if (!moveToInvOrHotbar(index, clicked)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(clicked, 0, resultSlot, false)) {
                if (!moveToInvOrHotbar(index, clicked)) {
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
        return result;
    }

    /** 机器槽放不下时，在玩家背包与快捷栏之间回退移动 */
    private boolean moveToInvOrHotbar(int index, ItemStack clicked) {
        if (index < invSlotEnd) {
            return moveItemStackTo(clicked, invSlotEnd, hotbarSlotEnd, true);
        }
        return moveItemStackTo(clicked, invSlotStart, invSlotEnd, false);
    }
}
