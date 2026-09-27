package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.MixerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 搅拌器方块实体（方块编码 366 = mixer）。
 * 工作台式<b>即时合成</b>（同原版工作台：摆好即预览、取走各配料消耗 1、shift 批量合成）：
 * <ul>
 *   <li>槽 0~7：两列 x4 普通输入槽；</li>
 *   <li>槽 8：输出槽上方居中的<b>额外输入槽</b>（碗/瓶等额外配料只能放此槽）；</li>
 *   <li>槽 9：结果预览槽，禁止手动放入，关闭/破坏时清空。</li>
 * </ul>
 * 配方为<b>无序</b>匹配（搅拌混合语义），分两种：
 * <ul>
 *   <li>带额外配料（extra 非 null）：槽 8 必须恰好放该额外物，槽 0~7 非空物品的种类多重集
 *       与配料一致（每格 1 件，顺序无关）；</li>
 *   <li>无额外配料（extra 为 null）：全部 9 个输入槽非空物品的种类多重集与配料一致
 *       （额外槽当普通槽使用）。</li>
 * </ul>
 * 配方表独立且当前为空，通过 {@link #registerRecipe} 注册扩展。
 */
public class MixerBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int INPUT_SLOTS = 9;
    /** 槽 8：额外输入槽（输出槽上方居中） */
    public static final int SLOT_EXTRA = 8;
    public static final int SLOT_RESULT = 9;
    public static final int TOTAL_SLOTS = 10;

    /**
     * 一条搅拌配方：ingredients 为普通配料多重集（每格 1 件，顺序无关）；
     * extra 非 null 时必须放在槽 8（额外输入槽），为 null 时额外槽按普通槽参与多重集匹配。
     */
    public record MixerRecipe(List<Item> ingredients, Item extra, ItemStack result) {
    }

    private static final List<MixerRecipe> RECIPES = new ArrayList<>();

    /**
     * 注册一条搅拌配方。
     *
     * @param extra       额外输入槽（槽 8）专属物品：碗/瓶等只能放此槽；null 表示无额外配料，
     *                    此时 9 个槽整体按无序多重集匹配
     * @param ingredients 普通配料（槽 0~7；extra 为 null 时可占满 9 槽），各 1 件，顺序无关
     */
    public static void registerRecipe(ItemStack result, Item extra, Item... ingredients) {
        List<Item> list = new ArrayList<>(ingredients.length);
        for (Item item : ingredients) {
            list.add(item);
        }
        RECIPES.add(new MixerRecipe(List.copyOf(list), extra, result.copy()));
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    /** 对外（漏斗/管道）只暴露 9 个输入槽，结果预览槽绝不能被抽出 */
    private final Container inputOnlyView = new Container() {
        @Override public int getContainerSize() { return INPUT_SLOTS; }
        @Override public boolean isEmpty() {
            for (int i = 0; i < INPUT_SLOTS; i++) {
                if (!items.get(i).isEmpty()) return false;
            }
            return true;
        }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
        @Override public ItemStack removeItem(int slot, int amount) {
            return ContainerHelper.removeItem(items, slot, amount);
        }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(items, slot);
        }
        @Override public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
        }
        @Override public void setChanged() { MixerBlockEntity.this.setChanged(); }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
        @Override public void clearContent() {
            for (int i = 0; i < INPUT_SLOTS; i++) {
                items.set(i, ItemStack.EMPTY);
            }
        }
    };

    private LazyOptional<IItemHandler> itemHandler;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXER.get(), pos, state);
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- 配方匹配 --------------------

    private ItemStack findRecipe() {
        // 普通槽 0~7 的非空物品多重集
        List<Item> grid = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < SLOT_EXTRA; i++) {
            ItemStack s = items.get(i);
            if (!s.isEmpty()) {
                grid.add(s.getItem());
            }
        }
        ItemStack extraStack = items.get(SLOT_EXTRA);
        // 全部 9 槽的非空物品多重集（无额外配料的配方使用：此时槽 8 当普通第 9 槽）
        List<Item> all = new ArrayList<>(grid);
        if (!extraStack.isEmpty()) {
            all.add(extraStack.getItem());
        }
        for (MixerRecipe recipe : RECIPES) {
            List<Item> have;
            Item expectedExtra = recipe.extra();
            if (expectedExtra != null) {
                // 带额外配料：额外物必须恰好放在槽 8，普通配料只能来自槽 0~7
                if (extraStack.isEmpty() || extraStack.getItem() != expectedExtra) {
                    continue;
                }
                have = grid;
            } else {
                // 无额外配料：9 槽整体无序匹配（槽 8 可放普通配料，如 #358 的 9 料）
                have = all;
            }
            if (recipe.ingredients().size() != have.size()) {
                continue;
            }
            // 多重集比较：配料每命中一个就从副本中移除
            List<Item> remain = new ArrayList<>(recipe.ingredients());
            boolean ok = true;
            for (Item item : have) {
                if (!remain.remove(item)) {
                    ok = false;
                    break;
                }
            }
            if (ok && remain.isEmpty()) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /** 供菜单批量合成查询配方 */
    public ItemStack getCraftResult() {
        return findRecipe();
    }

    // -------------------- 服务端 tick：刷新结果预览 --------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntity be) {
        be.refreshResultPreview();
    }

    public void refreshResultPreview() {
        ItemStack expected = findRecipe();
        ItemStack current = items.get(SLOT_RESULT);
        if (expected.isEmpty()) {
            if (!current.isEmpty()) {
                items.set(SLOT_RESULT, ItemStack.EMPTY);
                setChanged();
            }
            return;
        }
        boolean same = !current.isEmpty()
                && ItemStack.isSameItemSameTags(current, expected)
                && current.getCount() == expected.getCount();
        if (!same) {
            items.set(SLOT_RESULT, expected);
            setChanged();
        }
    }

    /** 玩家取走 1 次合成结果：全部非空输入槽各消耗 1 个，然后刷新下一预览 */
    public void consumeInputsForCraft() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            items.get(i).shrink(1);
        }
        refreshResultPreview();
        setChanged();
    }

    public void clearResultPreview() {
        if (!items.get(SLOT_RESULT).isEmpty()) {
            items.set(SLOT_RESULT, ItemStack.EMPTY);
            setChanged();
        }
    }

    // -------------------- 存档 --------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        refreshResultPreview();
    }

    // -------------------- 能力 --------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- Container --------------------

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < INPUT_SLOTS;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents(Level level, BlockPos pos) {
        clearResultPreview();
        Containers.dropContents(level, pos, this);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.mimiworld.mixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new MixerMenu(containerId, playerInventory, this, access);
    }
}
