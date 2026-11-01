package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.DissociationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
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
 * 离解台方块实体（dissociation_table）。
 * <p>
 * 熔炉式加工（不消耗燃料）：上方输入槽放待离解物、正下方输入槽放离解介质，
 * 命中已注册的有序双输入配方且输出槽可容纳时持续推进进度，满一个周期后两种
 * 原料各消耗 1 个、产出 1 个结果，进度归零重新计时。
 * <p>
 * 与 {@link DualInputProcessorBlockEntity} 的区别：两个输入槽<b>有序</b>
 * （上=原料、下=介质，不可互换），槽位布局为熔炉式（左上输入、右上输出、正下输入）。
 * <ul>
 *   <li>槽 0：上方输入槽（待离解原料）；</li>
 *   <li>槽 1：输出槽，禁止手动放入；</li>
 *   <li>槽 2：下方输入槽（离解介质）。</li>
 * </ul>
 * 当前配方表为空（尚无配方），后续内容通过 {@link #registerRecipe} 注册扩展。
 */
public class DissociationTableBlockEntity extends BlockEntity
        implements MenuProvider, net.minecraft.world.Container {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_INPUT_BOTTOM = 2;
    public static final int TOTAL_SLOTS = 3;

    /** 默认加工时长（tick）：与原版熔炉冶炼一致，10 秒；每条配方可用 processTicks 自定义 */
    public static final int DEFAULT_PROCESS_TICKS = 200;

    /** 一条「上输入 + 下输入 → 产物」有序双输入配方（两槽不可互换） */
    public record DissociationRecipe(Item top, Item bottom, ItemStack result, int processTicks) {
    }

    /** 有序配方键：高位为上方原料、低位为下方介质的注册名（不可互换） */
    public static String recipeKey(Item top, Item bottom) {
        return BuiltInRegistries.ITEM.getKey(top) + "|" + BuiltInRegistries.ITEM.getKey(bottom);
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int progress = 0;

    /*
     * DataSlot 布局：0 = 当前进度，1 = 当前配方加工总时长
     */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_COUNT = 2;

    private final ContainerData data = new SimpleContainerData(DATA_COUNT);

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    /** 键：有序配方注册名（见 recipeKey）；当前为空表 */
    private static final Map<String, DissociationRecipe> RECIPES = new HashMap<>();

    /** 注册一条离解配方：top（上槽原料）+ bottom（下槽介质）→ result */
    public static void registerRecipe(Item top, Item bottom, ItemStack result, int processTicks) {
        RECIPES.put(recipeKey(top, bottom),
                new DissociationRecipe(top, bottom, result, processTicks));
    }

    /** 注册一条离解配方（使用默认加工时长 {@value #DEFAULT_PROCESS_TICKS} tick） */
    public static void registerRecipe(Item top, Item bottom, ItemStack result) {
        registerRecipe(top, bottom, result, DEFAULT_PROCESS_TICKS);
    }

    @Nullable
    private static DissociationRecipe findRecipeStatic(ItemStack top, ItemStack bottom) {
        if (top.isEmpty() || bottom.isEmpty()) {
            return null;
        }
        return RECIPES.get(recipeKey(top.getItem(), bottom.getItem()));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static java.util.List<DissociationRecipe> jeiRecipes() {
        return java.util.List.copyOf(RECIPES.values());
    }

    public DissociationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISSOCIATION_TABLE.get(), pos, state);
        this.data.set(DATA_MAX_PROGRESS, DEFAULT_PROCESS_TICKS);
    }

    // -------------------- 服务端 tick --------------------

    /** 服务端每 tick 加工逻辑（BlockEntityTicker 回调） */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  DissociationTableBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (processRecipe()) {
            setChanged();
        }
    }

    /**
     * 配方加工循环：上/下输入持续命中配方、输出槽可容纳时进度推进；
     * 到达配方时长后两输入各 shrink(1)、产出 1 个结果，进度归零重新计时。
     * 条件不满足时进度立即归零。
     */
    private boolean processRecipe() {
        ItemStack top = items.get(SLOT_INPUT);
        ItemStack bottom = items.get(SLOT_INPUT_BOTTOM);
        DissociationRecipe recipe = findRecipeStatic(top, bottom);
        boolean canProcess = recipe != null && canOutputAccept(recipe.result());

        if (!canProcess) {
            if (progress != 0) {
                progress = 0;
                data.set(DATA_PROGRESS, progress);
                return true;
            }
            return false;
        }

        data.set(DATA_MAX_PROGRESS, recipe.processTicks());
        progress++;
        if (progress >= recipe.processTicks()) {
            top.shrink(1);
            bottom.shrink(1);
            ItemStack output = items.get(SLOT_OUTPUT);
            if (output.isEmpty()) {
                items.set(SLOT_OUTPUT, recipe.result().copy());
            } else {
                output.grow(recipe.result().getCount());
            }
            progress = 0;
        }
        data.set(DATA_PROGRESS, progress);
        return true;
    }

    /** 输出槽能否再放入这份结果：空槽可放；同种物品堆叠不爆自身上限可放 */
    private boolean canOutputAccept(ItemStack result) {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    // -------------------- 存档 --------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        data.set(DATA_PROGRESS, progress);
    }

    // -------------------- 能力（漏斗/管道交互） --------------------

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
        itemHandler = LazyOptional.of(() -> new InvWrapper(this));
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

    /** 槽位准入：上/下两个输入槽接受任意物品（是否参与加工由配方表决定），输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT || slot == SLOT_INPUT_BOTTOM;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // -------------------- 菜单 --------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.dissociation_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new DissociationTableMenu(containerId, playerInventory, this, data, access);
    }

    /** 方块被移除时供 Block 调用：掉落槽内物品 */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }
}
