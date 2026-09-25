package com.mimiworld.mimiworld.block.entity;

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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
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

import java.util.List;

/**
 * 多输入熔炉式定时加工台方块实体基类（淬剑台 / 汲血台 / 搅碎器 / 碾磨台共用）。
 * <p>
 * 加工节奏同原版熔炉（默认 200 tick = 10 秒，无燃料）：N 个输入槽<b>有序</b>摆放原料，
 * 命中已注册配方且输出槽可容纳时持续推进进度，满一个周期后每个非空配料槽消耗 1 个、
 * 产出 1 个结果，进度归零重新计时。
 * <ul>
 *   <li>槽 0 ~ N-1：输入槽（任意物品可放，位置语义由配方表决定）；</li>
 *   <li>槽 N：输出槽，禁止手动放入；产物真实存放其中（不是预览）。</li>
 * </ul>
 * 需要"按按钮才开工"的机器（淬剑台）覆写 {@link #requiresStartButton()} 返回 true：
 * 玩家点开始按钮 → {@link #startProcessing()} 置运行态，加工完一轮自动停止，中途配料
 * 被取走也立即停止。其余机器配方满足即自动运转（熔炉式）。
 * <p>
 * 子类各自持有一份 static 配方表（{@link ProcessorRecipe}，有序逐槽匹配），
 * 各机器配方互不通用；当前所有机器配方表均为空。
 */
public abstract class MultiInputProcessorBlockEntity extends BlockEntity
        implements MenuProvider, Container {

    /** 默认加工时长（tick）：与原版熔炉冶炼一致，10 秒 */
    public static final int DEFAULT_PROCESS_TICKS = 200;

    /** 一条有序多输入配方：inputs[i] 为第 i 个输入槽要求的物品（null=该槽必须为空） */
    public record ProcessorRecipe(Item[] inputs, ItemStack result, int processTicks) {
    }

    // -------------------- 状态 --------------------

    private final int inputCount;
    private final NonNullList<ItemStack> items;

    private int progress = 0;
    private boolean running = false;

    /*
     * DataSlot 布局：0 = 当前进度，1 = 当前配方加工总时长，2 = 是否运行中（0/1）
     */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_RUNNING = 2;
    public static final int DATA_COUNT = 3;

    private final ContainerData data = new SimpleContainerData(DATA_COUNT);

    private LazyOptional<IItemHandler> itemHandler;

    protected MultiInputProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                             int inputCount) {
        super(type, pos, state);
        this.inputCount = inputCount;
        this.items = NonNullList.withSize(inputCount + 1, ItemStack.EMPTY);
        this.data.set(DATA_MAX_PROGRESS, DEFAULT_PROCESS_TICKS);
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inputOnlyView));
    }

    // -------------------- 子类扩展点 --------------------

    /** 输入槽数量（输出槽固定在最后一个） */
    public final int getInputCount() {
        return inputCount;
    }

    /** 结果槽索引 */
    public final int getResultSlot() {
        return inputCount;
    }

    /** 查本机独立配方表（有序逐槽匹配）；无命中返回 null */
    @Nullable
    protected abstract ProcessorRecipe findRecipe(ItemStack[] inputs);

    /** 是否需要玩家点"开始"按钮才运转（默认 false=熔炉式自动运转） */
    protected boolean requiresStartButton() {
        return false;
    }

    /** 由具体子类创建对应的菜单 */
    @Nullable
    protected abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                                        ContainerData data, ContainerLevelAccess access);

    /** 子类配方表有序匹配工具：逐槽比对物品（配方 null 位要求该槽为空） */
    @Nullable
    protected static ProcessorRecipe matchOrdered(List<ProcessorRecipe> recipes, ItemStack[] inputs) {
        outer:
        for (ProcessorRecipe recipe : recipes) {
            if (recipe.inputs().length != inputs.length) {
                continue;
            }
            for (int i = 0; i < inputs.length; i++) {
                Item expected = recipe.inputs()[i];
                ItemStack got = inputs[i];
                if (expected == null) {
                    if (!got.isEmpty()) {
                        continue outer;
                    }
                } else if (got.isEmpty() || got.getItem() != expected) {
                    continue outer;
                }
            }
            return recipe;
        }
        return null;
    }

    // -------------------- 开始按钮 --------------------

    /** 玩家按下开始按钮（服务端）：当前确实能加工时进入运行态 */
    public void startProcessing() {
        if (!requiresStartButton() || running) {
            return;
        }
        ItemStack[] inputs = collectInputs();
        ProcessorRecipe recipe = findRecipe(inputs);
        if (recipe != null && canOutputAccept(recipe.result())) {
            running = true;
            data.set(DATA_RUNNING, 1);
            setChanged();
        }
    }

    public boolean isRunning() {
        return running;
    }

    // -------------------- 服务端 tick --------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MultiInputProcessorBlockEntity be) {
        be.tick();
    }

    private void tick() {
        ItemStack[] inputs = collectInputs();
        ProcessorRecipe recipe = findRecipe(inputs);
        boolean canProcess = recipe != null && canOutputAccept(recipe.result());

        if (!canProcess) {
            boolean changed = progress != 0;
            progress = 0;
            data.set(DATA_PROGRESS, 0);
            if (requiresStartButton() && running) {
                running = false;
                data.set(DATA_RUNNING, 0);
                changed = true;
            }
            if (changed) {
                setChanged();
            }
            return;
        }

        data.set(DATA_MAX_PROGRESS, recipe.processTicks());
        boolean active = !requiresStartButton() || running;
        if (active) {
            progress++;
            if (progress >= recipe.processTicks()) {
                for (int i = 0; i < inputCount; i++) {
                    if (recipe.inputs()[i] != null) {
                        items.get(i).shrink(1);
                    }
                }
                ItemStack output = items.get(getResultSlot());
                if (output.isEmpty()) {
                    items.set(getResultSlot(), recipe.result().copy());
                } else {
                    output.grow(recipe.result().getCount());
                }
                progress = 0;
                if (requiresStartButton()) {
                    // 一轮加工完成即停止，等待再次按下开始按钮
                    running = false;
                    data.set(DATA_RUNNING, 0);
                }
            }
        }
        data.set(DATA_PROGRESS, progress);
        setChanged();
    }

    private ItemStack[] collectInputs() {
        ItemStack[] inputs = new ItemStack[inputCount];
        for (int i = 0; i < inputCount; i++) {
            inputs[i] = items.get(i);
        }
        return inputs;
    }

    /** 输出槽能否再放入这份结果：空槽可放；同种物品堆叠不爆自身上限可放 */
    private boolean canOutputAccept(ItemStack result) {
        ItemStack output = items.get(getResultSlot());
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
        tag.putBoolean("Running", running);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        running = tag.getBoolean("Running");
        data.set(DATA_PROGRESS, progress);
        data.set(DATA_RUNNING, running ? 1 : 0);
    }

    // -------------------- 能力（漏斗/管道只能进输入槽，抽不走产物） --------------------

    private final Container inputOnlyView = new Container() {
        @Override public int getContainerSize() { return inputCount; }
        @Override public boolean isEmpty() {
            for (int i = 0; i < inputCount; i++) {
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
        @Override public void setChanged() { MultiInputProcessorBlockEntity.this.setChanged(); }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
        @Override public void clearContent() {
            for (int i = 0; i < inputCount; i++) {
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
        return inputCount + 1;
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

    /** 输入槽接受任意物品（工作台语义），输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < inputCount;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // -------------------- 菜单 --------------------

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return createMenu(containerId, playerInventory, data, access);
    }

    /** 方块被移除时供 Block 调用：掉落槽内物品（含输出槽产物） */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }
}
