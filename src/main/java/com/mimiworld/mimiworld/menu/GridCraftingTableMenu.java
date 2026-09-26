package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.entity.GridCraftingTableBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * NxN 网格工作台式机器菜单基类（杵界台 2x2 / 凝锭台 5x5 等共用）。
 * <p>
 * 槽位几何由构造参数给出（与各机器自绘 GUI 贴图逐像素对齐）：
 * <ul>
 *   <li>NxN 合成网格：槽 (gridX+col*pitchX, gridY+row*pitchY)，槽位索引 0~N*N-1（行优先）；</li>
 *   <li>结果槽：(resultX,resultY)，槽位索引 N*N，只取不放，取走时各非空网格槽消耗 1 个；</li>
 *   <li>玩家背包 (8,playerInvY) 起 3x9，快捷栏 playerInvY+58。</li>
 * </ul>
 */
public abstract class GridCraftingTableMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_START_X = 8;

    /** shift+合成一次最多处理的轮数（与既有机器一致的上限） */
    private static final int MAX_BATCH_CRAFT = 64;

    private final Container container;
    private final ContainerLevelAccess access;

    private final int gridSize;
    private final int resultSlotIndex;
    private final int machineSlots;
    private final int invSlotStart;
    private final int invSlotEnd;
    private final int hotbarSlotEnd;

    /** 子类提供：GUI 距离校验用的对应方块 */
    protected abstract Block validBlock();

    /** 按当前网格查询本机配方表（仅服务端 BlockEntity 容器有结果，客户端空容器返回空栈） */
    private ItemStack gridResult() {
        if (!(container instanceof GridCraftingTableBlockEntity be)) {
            return ItemStack.EMPTY;
        }
        ItemStack[] grid = new ItemStack[resultSlotIndex];
        for (int g = 0; g < grid.length; g++) {
            grid[g] = container.getItem(g);
        }
        return be.getCraftResult(grid);
    }

    /**
     * 服务端构造（由具体子类转发）。
     *
     * @param gridSize    网格边长 N
     * @param gridX       网格左上槽的 16x16 凹槽 X
     * @param gridY       网格左上槽的 16x16 凹槽 Y
     * @param pitchX      网格横向槽间距
     * @param pitchY      网格纵向槽间距
     * @param resultX     结果槽 X
     * @param resultY     结果槽 Y
     * @param playerInvY  玩家背包首行 Y（快捷栏自动 +58）
     */
    protected GridCraftingTableMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
                                    Container container, ContainerLevelAccess access,
                                    int gridSize, int gridX, int gridY, int pitchX, int pitchY,
                                    int resultX, int resultY, int playerInvY) {
        super(menuType, containerId);
        this.gridSize = gridSize;
        this.resultSlotIndex = gridSize * gridSize;
        this.machineSlots = resultSlotIndex + 1;
        checkContainerSize(container, machineSlots);
        this.container = container;
        this.access = access;
        this.invSlotStart = machineSlots;
        this.invSlotEnd = invSlotStart + 27;
        this.hotbarSlotEnd = invSlotEnd + 9;

        // NxN 合成网格：任意物品可放（工作台语义，能否合成由 BE 配方表决定）
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                addSlot(new Slot(container, row * gridSize + col,
                        gridX + col * pitchX, gridY + row * pitchY));
            }
        }
        // 结果槽：只取不放；取走 1 份结果即在服务端消耗各非空网格槽 1 个
        addSlot(new Slot(container, resultSlotIndex, resultX, resultY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (container instanceof GridCraftingTableBlockEntity be) {
                    be.consumeInputsForCraft();
                }
                super.onTake(player, stack);
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_START_X + col * 18,
                        playerInvY + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_START_X + col * 18, playerInvY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, validBlock());
    }

    /** 关闭界面：丢弃尚未取走的结果预览（预览不是真实产物） */
    @Override
    public void removed(Player player) {
        if (container instanceof GridCraftingTableBlockEntity be) {
            be.clearResultPreview();
        }
        super.removed(player);
    }

    /**
     * Shift+点击：
     * <ul>
     *   <li>结果槽：工作台式连续合成——每轮先确认背包放得下整份产物才消耗网格，
     *       直到原料耗尽、产物堆满或背包无空位；</li>
     *   <li>网格槽 → 玩家背包；</li>
     *   <li>玩家背包 → 全部网格槽（结果槽不接收）；背包↔快捷栏互转。</li>
     * </ul>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 结果槽批量合成（仅服务端容器为 BlockEntity；客户端空容器直接返回）
        if (index == resultSlotIndex) {
            if (!(container instanceof GridCraftingTableBlockEntity be)) {
                return ItemStack.EMPTY;
            }
            ItemStack crafted = ItemStack.EMPTY;
            for (int i = 0; i < MAX_BATCH_CRAFT; i++) {
                ItemStack one = gridResult();
                if (one.isEmpty()) {
                    break;
                }
                // 先尝试把整份产物放进玩家背包；放得下（被全部移走）才真正消耗网格
                ItemStack trial = one.copy();
                if (!moveItemStackTo(trial, invSlotStart, hotbarSlotEnd, true)
                        || !trial.isEmpty()) {
                    break;
                }
                be.consumeInputsForCraft();
                if (crafted.isEmpty()) {
                    crafted = one.copy();
                } else if (ItemStack.isSameItemSameTags(crafted, one)
                        && crafted.getCount() + one.getCount() <= crafted.getMaxStackSize()) {
                    crafted.grow(one.getCount());
                } else {
                    break;
                }
            }
            this.broadcastChanges();
            return crafted;
        }

        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack clicked = slot.getItem();
            result = clicked.copy();

            if (index < machineSlots) {
                // 网格槽 → 玩家背包/快捷栏
                if (!moveItemStackTo(clicked, invSlotStart, hotbarSlotEnd, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 全部网格槽（0..resultSlotIndex 不含结果槽）
                if (!moveItemStackTo(clicked, 0, resultSlotIndex, false)) {
                    // 背包↔快捷栏
                    if (index < invSlotEnd) {
                        if (!moveItemStackTo(clicked, invSlotEnd, hotbarSlotEnd, true)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(clicked, invSlotStart, invSlotEnd, false)) {
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
