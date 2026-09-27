package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.GridCookingPotBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeHooks;

/**
 * 铁锅菜单（iron_wok.png 176x178）。
 * <p>
 * 4 个输入槽在 GUI 里呈菱形：左上(8,17)、右上(62,17)、左下(26,35)、右下(44,35)；
 * 槽 4 = 右上单独输入(116,26)，槽 5 = 燃料(35,74)，槽 6 = 输出(116,74)。
 * 槽序与 {@link com.mimiworld.mimiworld.block.entity.IronWokBlockEntity} 的 2x2 基类一致。
 */
public class IronWokMenu extends AbstractContainerMenu {

    public static final int INPUT_TOP_LEFT = 0;
    public static final int INPUT_TOP_RIGHT = 1;
    public static final int INPUT_BOTTOM_LEFT = 2;
    public static final int INPUT_BOTTOM_RIGHT = 3;
    public static final int SLOT_EXTRA = 4;
    public static final int SLOT_FUEL = 5;
    public static final int SLOT_RESULT = 6;
    public static final int MACHINE_SLOTS = 7;

    private static final int INV_START = MACHINE_SLOTS;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** 客户端构造 */
    public IronWokMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(MACHINE_SLOTS),
                new SimpleContainerData(GridCookingPotBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    /** 服务端构造 */
    public IronWokMenu(int containerId, Inventory playerInventory,
                       Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.IRON_WOK_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, GridCookingPotBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;

        // 菱形四输入
        addSlot(new Slot(container, INPUT_TOP_LEFT, 8, 17));
        addSlot(new Slot(container, INPUT_TOP_RIGHT, 62, 17));
        addSlot(new Slot(container, INPUT_BOTTOM_LEFT, 26, 35));
        addSlot(new Slot(container, INPUT_BOTTOM_RIGHT, 44, 35));
        // 右上单独输入
        addSlot(new Slot(container, SLOT_EXTRA, 116, 26));
        // 燃料槽：只收可燃烧物品
        addSlot(new Slot(container, SLOT_FUEL, 35, 74) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });
        // 输出槽：只取不放
        addSlot(new Slot(container, SLOT_RESULT, 116, 74) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
            }
        }
        // 快捷栏 9
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 154));
        }

        this.addDataSlots(data);
    }

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

    private Block validBlock() {
        return ModBlocks.IRON_WOK.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack clicked = slot.getItem();
        ItemStack result = clicked.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(clicked, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(clicked, result);
        } else {
            // 可燃烧物品优先进燃料槽，其余进四个输入槽与右上输入槽（不含输出槽）
            if (ForgeHooks.getBurnTime(clicked, null) > 0) {
                if (!moveItemStackTo(clicked, SLOT_FUEL, SLOT_FUEL + 1, false)
                        && !moveItemStackTo(clicked, 0, SLOT_RESULT, false)) {
                    if (!moveToInvOrHotbar(index, clicked)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(clicked, 0, SLOT_RESULT, false)) {
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

    private boolean moveToInvOrHotbar(int index, ItemStack clicked) {
        if (index < INV_END) {
            return moveItemStackTo(clicked, INV_END, HOTBAR_END, true);
        }
        return moveItemStackTo(clicked, INV_START, INV_END, false);
    }
}
