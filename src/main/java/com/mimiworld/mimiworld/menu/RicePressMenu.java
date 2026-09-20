package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 压饭机菜单：上方横排 9 个输入槽（任意物品）、下方居中 1 个输出槽
 * （工作台式即时预览/合成）；槽位坐标与 rice_press.png 槽框严格一致。
 */
public class RicePressMenu extends AbstractContainerMenu {

    // 输入槽（16x16 凹槽左上角；贴图槽框精灵位于 (x-1,y-1)）：x = 9+col*18, y = 18
    public static final int INPUT_START_X = 9;
    public static final int INPUT_Y = 18;
    public static final int INPUT_X_STRIDE = 18;
    // 输出槽（贴图槽框 (80,53)）
    public static final int SLOT_OUTPUT_X = 81;
    public static final int SLOT_OUTPUT_Y = 54;

    // 玩家背包槽
    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int MACHINE_SLOTS = RicePressBlockEntity.TOTAL_SLOTS;  // 10
    private static final int INV_SLOT_START = MACHINE_SLOTS;                     // 10
    private static final int INV_SLOT_END = INV_SLOT_START + 27;                 // 37（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;                 // 46（不含）

    /** shift+合成一次最多处理的产物数（一组上限） */
    private static final int MAX_BATCH_CRAFT = 64;

    private final Container container;
    private final ContainerLevelAccess access;

    /** 客户端构造：网络打开时调用，容器用本地空实现（槽位内容由网络同步） */
    public RicePressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOTS), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 RicePressBlockEntity#createMenu 调用 */
    public RicePressMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.RICE_PRESS_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        this.container = container;
        this.access = access;

        // 9 个输入槽：任意物品可放（工作台网格语义）
        for (int col = 0; col < RicePressBlockEntity.INPUT_SLOTS; col++) {
            addSlot(new Slot(container, col,
                    INPUT_START_X + col * INPUT_X_STRIDE, INPUT_Y));
        }
        // 输出槽：只取不放；取走 1 个结果即在服务端让每个非空输入槽消耗 1 个
        addSlot(new Slot(container, RicePressBlockEntity.SLOT_OUTPUT,
                SLOT_OUTPUT_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (container instanceof RicePressBlockEntity be) {
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
            addSlot(new Slot(playerInventory, col, PLAYER_INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RICE_PRESS.get());
    }

    /** 关闭界面：丢弃尚未取走的输出预览（预览不是真实产物） */
    @Override
    public void removed(Player player) {
        if (container instanceof RicePressBlockEntity be) {
            be.clearResultPreview();
        }
        super.removed(player);
    }

    /**
     * Shift+点击：
     * <ul>
     *   <li>输出槽：连续合成——每轮先确认背包放得下 1 个产物才消耗输入，
     *       直到原料耗尽、产物堆满或背包无空位；</li>
     *   <li>输入槽 → 玩家背包；</li>
     *   <li>玩家背包 → 9 个输入槽；背包↔快捷栏互转。</li>
     * </ul>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 输出槽批量合成（仅服务端容器为 BlockEntity；客户端空容器直接返回）
        if (index == RicePressBlockEntity.SLOT_OUTPUT) {
            if (!(container instanceof RicePressBlockEntity be)) {
                return ItemStack.EMPTY;
            }
            ItemStack crafted = ItemStack.EMPTY;
            for (int i = 0; i < MAX_BATCH_CRAFT; i++) {
                ItemStack one = RicePressBlockEntity.getCraftResult(container);
                if (one.isEmpty()) {
                    break;
                }
                // 先尝试把这 1 个产物放进玩家背包；放得下（被全部移走）才真正消耗输入
                ItemStack trial = one.copy();
                if (!moveItemStackTo(trial, INV_SLOT_START, HOTBAR_SLOT_END, true)
                        || !trial.isEmpty()) {
                    break;
                }
                be.consumeInputsForCraft();
                if (crafted.isEmpty()) {
                    crafted = one.copy();
                } else if (ItemStack.isSameItemSameTags(crafted, one)
                        && crafted.getCount() + 1 <= crafted.getMaxStackSize()) {
                    crafted.grow(1);
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
                // 输入槽 → 玩家背包/快捷栏
                if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 9 个输入槽（不含输出槽）
                if (!moveItemStackTo(clicked, 0, RicePressBlockEntity.SLOT_OUTPUT, false)) {
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
