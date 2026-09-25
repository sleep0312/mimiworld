package com.mimiworld.mimiworld.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 装备锻造台方块实体基类（铸剑台 / 铸弓台 / 铸甲台共用）。
 * <p>
 * GUI 与运作方式与原版工作台一致：3x3 合成网格 + 1 个结果槽，<b>即时合成</b>——
 * 网格摆好即预览结果，取走结果时 9 格各消耗 1 个，shift+取走可连续批量合成。
 * <p>
 * 与原版工作台的区别：<b>配方系统完全独立</b>。每个子类持有自己的配方表
 * （{@link ForgingRecipe}，支持有序 shaped 与无序 shapeless），三台配方互不通用，
 * 也不能合成原版工作台配方。
 * <ul>
 *   <li>槽 0~8：3x3 合成网格（行优先，任意物品可放）；</li>
 *   <li>槽 9：结果预览槽，禁止手动放入，关闭界面/破坏方块时清空（预览不是真实产物）。</li>
 * </ul>
 */
public abstract class ForgingTableBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int GRID_SLOTS = 9;
    public static final int SLOT_RESULT = 9;
    public static final int TOTAL_SLOTS = 10;

    // -------------------- 配方 --------------------

    /**
     * 一条锻造台配方：有序（shaped，按 3x3 网格裁剪边界后逐格匹配）或
     * 无序（shapeless，网格非空物品种类多重集完全一致）。
     */
    public static final class ForgingRecipe {
        /** 有序配方裁剪后的矩阵（行列均 ≤3，内部无空列/空行），null 元素表示空格；shapeless 时为 null */
        @Nullable
        private final Item[][] shaped;
        /** 无序配料；shaped 时为 null */
        @Nullable
        private final List<Item> shapeless;
        private final ItemStack result;

        private ForgingRecipe(@Nullable Item[][] shaped, @Nullable List<Item> shapeless, ItemStack result) {
            this.shaped = shaped;
            this.shapeless = shapeless;
            this.result = result;
        }

        /** 有序配方：pattern 为 1~3 行等宽字符串（1~3 列），' '（空格）表示空，其余字符取 keys 映射 */
        public static ForgingRecipe shaped(ItemStack result, String[] pattern, Map<Character, Item> keys) {
            if (pattern.length < 1 || pattern.length > 3) {
                throw new IllegalArgumentException("shaped pattern must have 1~3 rows");
            }
            int width = pattern[0].length();
            if (width < 1 || width > 3) {
                throw new IllegalArgumentException("shaped pattern must have 1~3 columns");
            }
            Item[][] raw = new Item[pattern.length][width];
            for (int r = 0; r < pattern.length; r++) {
                if (pattern[r].length() != width) {
                    throw new IllegalArgumentException("shaped pattern rows must have equal width");
                }
                for (int c = 0; c < width; c++) {
                    char ch = pattern[r].charAt(c);
                    if (ch == ' ') {
                        raw[r][c] = null;
                    } else {
                        Item mapped = keys.get(ch);
                        if (mapped == null) {
                            throw new IllegalArgumentException("shaped pattern key '" + ch + "' is not mapped");
                        }
                        raw[r][c] = mapped;
                    }
                }
            }
            return new ForgingRecipe(trimMatrix(raw), null, result.copy());
        }

        /** 无序配方：网格中非空物品的种类多重集必须与配料完全一致（顺序无关，每格 1 件） */
        public static ForgingRecipe shapeless(ItemStack result, Item... ingredients) {
            List<Item> list = new ArrayList<>(ingredients.length);
            for (Item item : ingredients) {
                list.add(item);
            }
            return new ForgingRecipe(null, List.copyOf(list), result.copy());
        }

        public ItemStack result() {
            return result;
        }

        /** 判断 3x3 网格（长度 9，行优先）是否命中本配方（只看物品种类，不看每格数量） */
        public boolean matches(ItemStack[] grid) {
            return shaped != null ? matchesShaped(grid) : matchesShapeless(grid);
        }

        private boolean matchesShaped(ItemStack[] grid) {
            Item[][] g = new Item[3][3];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    ItemStack s = grid[r * 3 + c];
                    g[r][c] = s.isEmpty() ? null : s.getItem();
                }
            }
            g = trimMatrix(g);
            if (g.length != shaped.length || g[0].length != shaped[0].length) {
                return false;
            }
            for (int r = 0; r < g.length; r++) {
                for (int c = 0; c < g[r].length; c++) {
                    if (g[r][c] != shaped[r][c]) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean matchesShapeless(ItemStack[] grid) {
            List<Item> have = new ArrayList<>();
            for (ItemStack s : grid) {
                if (!s.isEmpty()) {
                    have.add(s.getItem());
                }
            }
            if (have.size() != shapeless.size()) {
                return false;
            }
            // 多重集比较：配料每命中一个就从副本中移除
            List<Item> remain = new ArrayList<>(shapeless);
            for (Item item : have) {
                if (!remain.remove(item)) {
                    return false;
                }
            }
            return remain.isEmpty();
        }

        /** 裁掉矩阵四周全空的行/列（与原版 shaped 配方可在网格内任意平移的语义一致） */
        @Nullable
        private static Item[][] trimMatrix(@Nullable Item[][] m) {
            int rows = m.length;
            int cols = m[0].length;
            int r0 = rows, r1 = -1, c0 = cols, c1 = -1;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (m[r][c] != null) {
                        r0 = Math.min(r0, r);
                        r1 = Math.max(r1, r);
                        c0 = Math.min(c0, c);
                        c1 = Math.max(c1, c);
                    }
                }
            }
            if (r1 < 0) {
                return null;
            }
            Item[][] out = new Item[r1 - r0 + 1][c1 - c0 + 1];
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    out[r - r0][c - c0] = m[r][c];
                }
            }
            return out;
        }
    }

    /**
     * 子类提供：查本机独立配方表。
     * @param grid 当前 3x3 网格（长度 9，行优先）
     * @return 命中的产物副本；无命中返回 {@link ItemStack#EMPTY}
     */
    protected abstract ItemStack findRecipe(ItemStack[] grid);

    /** 供菜单批量合成查询配方（服务端容器为本 BlockEntity 时使用） */
    public final ItemStack getCraftResult(ItemStack[] grid) {
        return findRecipe(grid);
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // 对外（漏斗/管道）只暴露 9 个网格槽，结果预览槽绝不能被抽出
    private final Container inputOnlyView = new Container() {
        @Override public int getContainerSize() { return GRID_SLOTS; }
        @Override public boolean isEmpty() {
            for (int i = 0; i < GRID_SLOTS; i++) {
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
        @Override public void setChanged() { ForgingTableBlockEntity.this.setChanged(); }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
        @Override public void clearContent() {
            for (int i = 0; i < GRID_SLOTS; i++) {
                items.set(i, ItemStack.EMPTY);
            }
        }
    };

    private LazyOptional<IItemHandler> itemHandler;

    protected ForgingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- 服务端 tick：刷新结果预览 --------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgingTableBlockEntity be) {
        be.refreshResultPreview();
    }

    /** 依据 9 格网格刷新结果槽预览（不消耗输入）；无配方清空预览 */
    public void refreshResultPreview() {
        ItemStack expected = findRecipe(items.toArray(new ItemStack[0]));
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

    /** 玩家取走 1 次合成结果：9 格非空槽各消耗 1 个，然后刷新下一预览 */
    public void consumeInputsForCraft() {
        for (int i = 0; i < GRID_SLOTS; i++) {
            items.get(i).shrink(1);
        }
        refreshResultPreview();
        setChanged();
    }

    /** 关闭界面/破坏方块时清掉尚未取走的预览结果（预览不是真实产物） */
    public void clearResultPreview() {
        if (!items.get(SLOT_RESULT).isEmpty()) {
            items.set(SLOT_RESULT, ItemStack.EMPTY);
            setChanged();
        }
    }

    /** 供子类配方表查询工具：遍历配方列表找首个命中 */
    protected static ItemStack matchRecipes(List<ForgingRecipe> recipes, ItemStack[] grid) {
        for (ForgingRecipe recipe : recipes) {
            if (recipe.matches(grid)) {
                return recipe.result().copy();
            }
        }
        return ItemStack.EMPTY;
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

    /** 9 个网格槽接受任意物品（工作台语义），结果槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < GRID_SLOTS;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    /** 方块被移除时供 Block 调用：先抹掉预览再掉落网格内物品 */
    public void dropContents(Level level, BlockPos pos) {
        clearResultPreview();
        Containers.dropContents(level, pos, this);
    }
}
