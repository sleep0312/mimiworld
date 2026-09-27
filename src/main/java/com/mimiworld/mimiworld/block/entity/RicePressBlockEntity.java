package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.RicePressMenu;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 压饭机方块实体（方块编码 31 = rice_press）。
 * <p>
 * 合成方式类似原版工作台（即时合成、无加工计时）：
 * <ul>
 *   <li>槽 0..8：横排 9 个输入槽，<b>任意物品</b>均可放入（与工作台网格一致）；
 *       配方为无序合成，只看 9 格中已放置物品的<b>多重集合</b>（物品种类与数量，
 *       与放在哪一格无关，空格忽略）；</li>
 *   <li>槽 9：输出槽，输入命中配方时即时显示 1 个合成结果（仅预览），
 *       玩家取走结果时每个非空输入槽各消耗 1 个；shift+取走可连续批量合成；</li>
 * </ul>
 * 目前尚未注册任何配方。通过 {@link #registerRecipe} 扩展。
 */
public class RicePressBlockEntity extends BlockEntity implements MenuProvider, Container {

    /** 输入槽数量（槽位 0..8） */
    public static final int INPUT_SLOTS = 9;
    public static final int SLOT_OUTPUT = INPUT_SLOTS;   // 9
    public static final int TOTAL_SLOTS = INPUT_SLOTS + 1;

    // -------------------- 配方表（无序 1..9 输入 → 单产物，当前为空） --------------------

    /** 一条无序合成配方：inputs 为所需物品的多重集合（1..9 个，可重复，顺序无关） */
    public record PressRecipe(List<Item> inputs, ItemStack result) {
    }

    /** 键：所需物品注册 ID 升序拼接（重复物品重复出现），冻结为唯一匹配口径 */
    private static final Map<String, PressRecipe> RECIPES = new HashMap<>();

    /**
     * 注册一条无序配方（1..9 个输入，顺序无关，同种物品写多次即表示需要多个）。
     * 应在模组加载阶段调用；当前尚无配方。
     */
    public static void registerRecipe(ItemStack result, Item... inputs) {
        if (inputs.length < 1 || inputs.length > INPUT_SLOTS) {
            throw new IllegalArgumentException("rice press recipe needs 1..9 inputs");
        }
        List<Item> list = new ArrayList<>(inputs.length);
        for (Item item : inputs) {
            list.add(item);
        }
        RECIPES.put(canonicalKeyOfItems(list), new PressRecipe(List.copyOf(list), result.copy()));
    }

    /** 查询当前输入是否命中配方：命中返回 1 个结果副本，否则返回空栈 */
    public static ItemStack getCraftResult(Container container) {
        List<Item> occupied = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                occupied.add(stack.getItem());
            }
        }
        if (occupied.isEmpty()) {
            return ItemStack.EMPTY;
        }
        PressRecipe recipe = RECIPES.get(canonicalKeyOfItems(occupied));
        return recipe == null ? ItemStack.EMPTY : recipe.result().copy();
    }

    /** 物品多重集合 → 规范键：注册 ID 升序、逗号拼接（种类/数量/重复次数全部体现在键里） */
    private static String canonicalKeyOfItems(List<Item> items) {
        int[] ids = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ids[i] = Item.getId(items.get(i));
        }
        java.util.Arrays.sort(ids);
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        return sb.toString();
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    // 在构造器中初始化（inputOnlyView 在下方声明，字段初始化器不能非法前向引用）
    private LazyOptional<IItemHandler> itemHandler;

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<PressRecipe> jeiRecipes() {
        return List.copyOf(RECIPES.values());
    }

    public RicePressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RICE_PRESS.get(), pos, state);
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- 服务端 tick --------------------

    /** 服务端每 tick 刷新输出预览 */
    public static void serverTick(Level level, BlockPos pos, BlockState state, RicePressBlockEntity be) {
        be.refreshResultPreview();
    }

    /** 依据 9 个输入槽刷新输出槽预览（不消耗输入）；无配方时清空预览 */
    public void refreshResultPreview() {
        ItemStack expected = getCraftResult(this);
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

    /** 玩家取走 1 次合成结果时调用：9 个输入槽中每个非空槽各消耗 1 个，然后刷新预览 */
    public void consumeInputsForCraft() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            items.get(i).shrink(1);
        }
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
     * 对外只暴露槽 0..8 共 9 个输入槽的视图。
     */
    private final Container inputOnlyView = new Container() {
        @Override
        public int getContainerSize() {
            return INPUT_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < INPUT_SLOTS; i++) {
                if (!items.get(i).isEmpty()) {
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
        public void setChanged() {
            RicePressBlockEntity.this.setChanged();
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
            for (int i = 0; i < INPUT_SLOTS; i++) {
                items.set(i, ItemStack.EMPTY);
            }
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

    /** 9 个输入槽接受任意物品（工作台网格语义），输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < INPUT_SLOTS;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // -------------------- 菜单 --------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.rice_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new RicePressMenu(containerId, playerInventory, this, access);
    }

    /** 方块被移除时供 Block 调用：先抹掉预览再掉落真实物品 */
    public void dropContents(Level level, BlockPos pos) {
        clearResultPreview();
        Containers.dropContents(level, pos, this);
    }
}
