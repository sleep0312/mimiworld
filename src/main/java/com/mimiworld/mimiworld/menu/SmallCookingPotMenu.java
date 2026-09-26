package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.SmallCookingPotBlockEntity;
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
import net.minecraftforge.common.ForgeHooks;

/**
 * 小煮锅菜单：原版熔炉布局（small_cooking_pot.png 176x166）。
 * <ul>
 *   <li>槽 0 输入：(56,17)，任意物品；</li>
 *   <li>槽 1 燃料：(56,53)，仅可燃烧物品；</li>
 *   <li>槽 2 输出：(116,35)，只取不放；</li>
 *   <li>4 个 DataSlot：燃料剩余 / 燃料总时长 / 烹煮进度 / 烹煮总时长。</li>
 * </ul>
 */
public class SmallCookingPotMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = SmallCookingPotBlockEntity.TOTAL_SLOTS; // 3
    private static final int SLOT_INPUT = SmallCookingPotBlockEntity.SLOT_INPUT;
    private static final int SLOT_FUEL = SmallCookingPotBlockEntity.SLOT_FUEL;
    private static final int SLOT_RESULT = SmallCookingPotBlockEntity.SLOT_RESULT;
    private static final int INV_SLOT_START = MACHINE_SLOTS;                        // 3
    private static final int INV_SLOT_END = INV_SLOT_START + 27;                    // 30（不含）
    private static final int HOTBAR_SLOT_END = INV_SLOT_END + 9;                    // 39（不含）

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** 客户端构造 */
    public SmallCookingPotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(SmallCookingPotBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(SmallCookingPotBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造 */
    public SmallCookingPotMenu(int containerId, Inventory playerInventory,
                               Container container, ContainerData data,
                               ContainerLevelAccess access) {
        super(ModMenuTypes.SMALL_COOKING_POT_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, SmallCookingPotBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;

        addSlot(new Slot(container, SLOT_INPUT, 56, 17));
        addSlot(new Slot(container, SLOT_FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });
        addSlot(new Slot(container, SLOT_RESULT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col,
                    PLAYER_INV_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    // -------------------- 屏幕渲染读取 --------------------

    public int getLitTime() {
        return data.get(SmallCookingPotBlockEntity.DATA_LIT_TIME);
    }

    public int getLitDuration() {
        return data.get(SmallCookingPotBlockEntity.DATA_LIT_DURATION);
    }

    public int getProgress() {
        return data.get(SmallCookingPotBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        int max = data.get(SmallCookingPotBlockEntity.DATA_MAX_PROGRESS);
        return max > 0 ? max : SmallCookingPotBlockEntity.DEFAULT_COOK_TICKS;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SMALL_COOKING_POT.get());
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

        if (index < MACHINE_SLOTS) {
            // 机器槽（含输出）→ 玩家背包
            if (!moveItemStackTo(clicked, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(clicked, result);
        } else {
            // 玩家背包 → 机器：燃料优先进燃料槽，其余进输入槽
            if (ForgeHooks.getBurnTime(clicked, null) > 0) {
                if (!moveItemStackTo(clicked, SLOT_FUEL, SLOT_FUEL + 1, false)) {
                    if (!moveToInputOrBack(index, clicked)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(clicked, SLOT_INPUT, SLOT_INPUT + 1, false)) {
                if (!moveToInputOrBack(index, clicked)) {
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

    /** 输入槽放不下时，在玩家背包与快捷栏之间回退移动 */
    private boolean moveToInputOrBack(int index, ItemStack clicked) {
        if (index < INV_SLOT_END) {
            return moveItemStackTo(clicked, INV_SLOT_END, HOTBAR_SLOT_END, true);
        }
        return moveItemStackTo(clicked, INV_SLOT_START, INV_SLOT_END, false);
    }
}
