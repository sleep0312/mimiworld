package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.AdvancedRicePoundingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 高级锤米台方块实体（方块编码 49 = advanced_rice_pounding_table）。
 * <p>
 * 行为与普通锤米台 {@link RicePoundingTableBlockEntity} <b>完全一致</b>（仅贴图外观
 * 不同：右上角金色锤子装饰），合成方式类似原版工作台（即时合成、无加工计时）：
 * <ul>
 *   <li>槽 0 / 槽 1：两个输入槽，<b>任意物品</b>都可放入；</li>
 *   <li>槽 2：输出槽，命中配方时即时显示 1 个结果（仅预览），取走时两个输入槽
 *       各消耗 1 个；shift+取走可连续批量合成。</li>
 * </ul>
 * 配方表与普通锤米台相互独立（各自 registerRecipe），当前均为空。
 */
public class AdvancedRicePoundingTableBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_INPUT_B = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int TOTAL_SLOTS = 3;

    // -------------------- 配方表（无序双输入 → 单产物，当前为空，与普通锤米台独立） --------------------

    /** 一条「物品 A + 物品 B → 产物」配方，A/B 顺序无关 */
    public record PoundingRecipe(Item inputA, Item inputB, ItemStack result) {
    }

    private static final Map<Long, PoundingRecipe> RECIPES = new HashMap<>();

    /** 注册一条无序双输入配方。应在模组加载阶段调用；当前尚无配方。 */
    public static void registerRecipe(Item inputA, Item inputB, ItemStack result) {
        RECIPES.put(recipeKey(inputA, inputB), new PoundingRecipe(inputA, inputB, result.copy()));
    }

    private static long recipeKey(Item a, Item b) {
        int idA = Item.getId(a);
        int idB = Item.getId(b);
        int lo = Math.min(idA, idB);
        int hi = Math.max(idA, idB);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    /** 查询两槽物品能否合成：两槽都非空且命中配方时返回结果副本，否则返回空栈 */
    public static ItemStack getCraftResult(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return ItemStack.EMPTY;
        }
        PoundingRecipe recipe = RECIPES.get(recipeKey(a.getItem(), b.getItem()));
        return recipe == null ? ItemStack.EMPTY : recipe.result().copy();
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // 在构造器中初始化（inputOnlyView 在下方声明，字段初始化器不能非法前向引用）
    private LazyOptional<IItemHandler> itemHandler;

    public AdvancedRicePoundingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_RICE_POUNDING_TABLE.get(), pos, state);
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- 服务端 tick --------------------

    /** 服务端每 tick 刷新输出预览 */
    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedRicePoundingTableBlockEntity be) {
        be.refreshResultPreview();
    }

    /**
     * 依据两个输入槽刷新输出槽的预览结果（不消耗输入）。
     * 无配方时清空预览；已有同种结果则保持不动。
     */
    public void refreshResultPreview() {
        ItemStack expected = getCraftResult(items.get(SLOT_INPUT_A), items.get(SLOT_INPUT_B));
        ItemStack current = items.get(SLOT_OUTPUT);

        if (expected.isEmpty()) {
            if (!current.isEmpty()) {
                items.set(SLOT_OUTPUT, ItemStack.EMPTY);
                setChanged();
            }
            return;
        }
        boolean same = !current.isEmpty()
                && ItemStack.isSameItemSameTags(current, expected)
                && current.getCount() == expected.getCount();
        if (!same) {
            items.set(SLOT_OUTPUT, expected);
            setChanged();
        }
    }

    /** 玩家取走 1 次合成结果时调用：两输入各消耗 1 个，然后刷新下一预览 */
    public void consumeInputsForCraft() {
        items.get(SLOT_INPUT_A).shrink(1);
        items.get(SLOT_INPUT_B).shrink(1);
        refreshResultPreview();
        setChanged();
    }

    /** 关闭界面时清掉尚未取走的预览结果（预览不是真实产物） */
    public void clearResultPreview() {
        if (!items.get(SLOT_OUTPUT).isEmpty()) {
            items.set(SLOT_OUTPUT, ItemStack.EMPTY);
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

    // -------------------- 能力（漏斗/管道交互） --------------------

    /*
     * 输出槽只是预览，绝不能被漏斗抽出（否则等于免费刷产物）。
     * 对外只暴露槽 0/1 两个输入槽的 2 格视图。
     */
    private final Container inputOnlyView = new Container() {
        @Override
        public int getContainerSize() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return items.get(0).isEmpty() && items.get(1).isEmpty();
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
        public void setChanged() {
            AdvancedRicePoundingTableBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return true;
        }

        @Override
        public void clearContent() {
            items.set(0, ItemStack.EMPTY);
            items.set(1, ItemStack.EMPTY);
        }
    };

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

    // -------------------- Container 实现 --------------------

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

    /** 两个输入槽接受任意物品（工作台网格语义），输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT_A || slot == SLOT_INPUT_B;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // -------------------- 菜单 --------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.advanced_rice_pounding_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new AdvancedRicePoundingTableMenu(containerId, playerInventory, this, access);
    }

    /** 方块被移除时供 Block 调用：先抹掉预览再掉落真实物品 */
    public void dropContents(Level level, BlockPos pos) {
        clearResultPreview();
        Containers.dropContents(level, pos, this);
    }
}
