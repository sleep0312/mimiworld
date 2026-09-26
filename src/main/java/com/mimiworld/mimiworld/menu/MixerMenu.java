package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.MixerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 搅拌器菜单：9 个输入槽（两列 x4 + 箭头上方居中 1 个）+ 1 个结果槽（mixer.png 对齐）。
 * <ul>
 *   <li>左列槽 0~3：x=43，y=10/31/52/73；右列槽 4~7：x=115，同 y；</li>
 *   <li>中央输入槽 8：(79,84)；</li>
 *   <li>结果槽 9：(79,115)，只取不放，取走时 9 个输入槽各消耗 1 个；</li>
 *   <li>玩家背包 y=152，快捷栏 y=210（加高面板，高 236）。</li>
 * </ul>
 */
public class MixerMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = MixerBlockEntity.TOTAL_SLOTS; // 10
    private static final int RESULT_SLOT = MixerBlockEntity.SLOT_RESULT;   // 9
    private static final int INV_SLOT_START = MACHINE_SLOTS;               // 10
    private static final int INV_SLOT_END = INV_SLOT_START + 27;           // 37（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;           // 46（不含）

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 152;
    private static final int HOTBAR_Y = 210;

    private static final int MAX_BATCH_CRAFT = 64;

    private final Container container;
    private final ContainerLevelAccess access;

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器使用本地空实现（槽位同步照常工作） */
    public MixerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(MixerBlockEntity.TOTAL_SLOTS),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 MixerBlockEntity#createMenu 调用 */
    public MixerMenu(int containerId, Inventory playerInventory,
                     Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.MIXER_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        this.container = container;
        this.access = access;

        int[] xs = {43, 115};
        int[] ys = {10, 31, 52, 73};
        // 左列槽 0~3、右列槽 4~7
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < 4; row++) {
                addSlot(new Slot(container, col * 4 + row, xs[col], ys[row]));
            }
        }
        // 中央输入槽 8（箭头上方）
        addSlot(new Slot(container, 8, 79, 84));
        // 结果槽 9：只取不放；取走 1 份即在服务端消耗各输入槽 1 个
        addSlot(new Slot(container, RESULT_SLOT, 79, 115) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (container instanceof MixerBlockEntity be) {
                    be.consumeInputsForCraft();
                }
                super.onTake(player, stack);
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MIXER.get());
    }

    /** 关闭界面：丢弃尚未取走的结果预览 */
    @Override
    public void removed(Player player) {
        if (container instanceof MixerBlockEntity be) {
            be.clearResultPreview();
        }
        super.removed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 结果槽批量合成
        if (index == RESULT_SLOT) {
            if (!(container instanceof MixerBlockEntity be)) {
                return ItemStack.EMPTY;
            }
            ItemStack crafted = ItemStack.EMPTY;
            for (int i = 0; i < MAX_BATCH_CRAFT; i++) {
                ItemStack one = be.getCraftResult();
                if (one.isEmpty()) {
                    break;
                }
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
                if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(clicked, result);
            } else {
                // 玩家背包 → 9 个输入槽（结果槽不接收）
                if (!moveItemStackTo(clicked, 0, RESULT_SLOT, false)) {
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
