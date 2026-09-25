package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.entity.ForgingTableBlockEntity;
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
 * 装备锻造台菜单基类（铸剑台 / 铸弓台 / 铸甲台共用）。
 * <p>
 * 槽位布局与原版工作台 crafting_table.png <b>逐像素一致</b>：
 * <ul>
 *   <li>3x3 合成网格：槽 (30+col*18, 17+row*18)，槽位索引 0~8（行优先）；</li>
 *   <li>结果槽：(124,35)，槽位索引 9，只取不放，取走时 9 格各消耗 1 个；</li>
 *   <li>玩家背包 (8,84) 起 3x9，快捷栏 y=142。</li>
 * </ul>
 */
public abstract class ForgingTableMenu extends AbstractContainerMenu {

    // 3x3 网格左上角（16x16 凹槽左上角像素坐标）
    public static final int GRID_START_X = 30;
    public static final int GRID_START_Y = 17;
    public static final int SLOT_RESULT_X = 124;
    public static final int SLOT_RESULT_Y = 35;

    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int MACHINE_SLOTS = ForgingTableBlockEntity.TOTAL_SLOTS; // 10
    private static final int RESULT_SLOT = ForgingTableBlockEntity.SLOT_RESULT;   // 9
    private static final int INV_SLOT_START = MACHINE_SLOTS;                      // 10
    private static final int INV_SLOT_END = INV_SLOT_START + 27;                  // 37（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;                  // 46（不含）

    /** shift+合成一次最多处理的轮数（与锤米台一致的上限） */
    private static final int MAX_BATCH_CRAFT = 64;

    private final Container container;
    private final ContainerLevelAccess access;

    /** 子类提供：GUI 距离校验用的对应方块 */
    protected abstract Block validBlock();

    /** 按当前 3x3 网格查询本机配方表（仅服务端 BlockEntity 容器有结果，客户端空容器返回空栈） */
    private ItemStack gridResult() {
        if (!(container instanceof ForgingTableBlockEntity be)) {
            return ItemStack.EMPTY;
        }
        ItemStack[] grid = new ItemStack[ForgingTableBlockEntity.GRID_SLOTS];
        for (int g = 0; g < grid.length; g++) {
            grid[g] = container.getItem(g);
        }
        return be.getCraftResult(grid);
    }

    /** 服务端构造（由具体子类转发） */
    protected ForgingTableMenu(MenuType<?> menuType, int containerId, Inventory playerInventory,
                               Container container, ContainerLevelAccess access) {
        super(menuType, containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        this.container = container;
        this.access = access;

        // 3x3 合成网格：任意物品可放（工作台语义，能否合成由 BE 配方表决定）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, row * 3 + col,
                        GRID_START_X + col * 18, GRID_START_Y + row * 18));
            }
        }
        // 结果槽：只取不放；取走 1 份结果即在服务端消耗网格各 1 个
        addSlot(new Slot(container, RESULT_SLOT, SLOT_RESULT_X, SLOT_RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (container instanceof ForgingTableBlockEntity be) {
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
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, validBlock());
    }

    /** 关闭界面：丢弃尚未取走的结果预览（预览不是真实产物） */
    @Override
    public void removed(Player player) {
        if (container instanceof ForgingTableBlockEntity be) {
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
     *   <li>玩家背包 → 9 个网格槽（结果槽不接收）；背包↔快捷栏互转。</li>
     * </ul>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 结果槽批量合成（仅服务端容器为 BlockEntity；客户端空容器直接返回）
        if (index == RESULT_SLOT) {
            if (!(container instanceof ForgingTableBlockEntity be)) {
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
                if (!moveItemStackTo(trial, INV_SLOT_START, HOTBAR_SLOT_END, true)
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

            if (index < MACHINE_SLOTS) {
                // 网格槽 → 玩家背包/快捷栏
                if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 9 个网格槽（0..9 不含结果槽）
                if (!moveItemStackTo(clicked, 0, RESULT_SLOT, false)) {
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
