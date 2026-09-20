package com.mimiworld.mimiworld.block.entity;

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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * 双输入定时加工台方块实体基类（电离台 / 沉淀台共用）。
 * <p>
 * 加工节奏与原版熔炉一致：两个输入槽各放 1 种原料，命中已注册的双输入配方且输出槽
 * 可容纳时持续推进进度，加工满一个周期后两种原料各消耗 1 个、产出 1 个结果，进度归零
 * 重新计时（整组原料也要逐个周期加工）。
 * <p>
 * 与熔炉的区别：<b>不消耗燃料</b>，没有燃料槽；只要原料满足就一直加工。
 * <ul>
 *   <li>槽 0：输入 A（任意物品，工作台语义，是否参与加工由配方表决定）；</li>
 *   <li>槽 1：输入 B（同上，两槽无序，配方只按物品种类匹配，与摆放槽位无关）；</li>
 *   <li>槽 2：输出槽，禁止手动放入。</li>
 * </ul>
 * 子类各自持有一份 static 配方表（当前为空表，通过各自的 {@code registerRecipe} 扩展）。
 */
public abstract class DualInputProcessorBlockEntity extends BlockEntity
        implements MenuProvider, net.minecraft.world.Container {

    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_INPUT_B = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int TOTAL_SLOTS = 3;

    /** 默认加工时长（tick）：与原版熔炉冶炼一致，10 秒；每条配方可用 processTicks 自定义 */
    public static final int DEFAULT_PROCESS_TICKS = 200;

    /** 一条「输入 A + 输入 B → 产物」双输入无序配方（两槽顺序无关） */
    public record ProcessorRecipe(Item inputA, Item inputB, ItemStack result, int processTicks) {
    }

    /**
     * 双输入配方键：两种物品的注册 ID 先排序再拼成一个 long，
     * 保证 (A,B) 与 (B,A) 命中同一条配方（无序合成）。
     */
    public static long recipeKey(Item a, Item b) {
        int idA = BuiltInRegistries.ITEM.getId(a);
        int idB = BuiltInRegistries.ITEM.getId(b);
        int lo = Math.min(idA, idB);
        int hi = Math.max(idA, idB);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int progress = 0;

    /*
     * DataSlot 布局（无燃料/液体，只需 2 个）：
     * 0 = 当前进度
     * 1 = 当前配方加工总时长
     */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_COUNT = 2;

    private final ContainerData data = new SimpleContainerData(DATA_COUNT);

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    protected DualInputProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.data.set(DATA_MAX_PROGRESS, DEFAULT_PROCESS_TICKS);
    }

    /** 查本机配方表（无序双输入）；无命中返回 null */
    @Nullable
    protected abstract ProcessorRecipe findRecipe(ItemStack inputA, ItemStack inputB);

    /** 由具体子类创建对应的菜单 */
    @Nullable
    protected abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                                        ContainerData data, ContainerLevelAccess access);

    // -------------------- 服务端 tick --------------------

    /** 服务端每 tick 加工逻辑（BlockEntityTicker 回调，泛型 ? super 子类即可匹配） */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  DualInputProcessorBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (processRecipe()) {
            setChanged();
        }
    }

    /**
     * 配方加工循环：两输入持续命中配方、输出槽可容纳时进度推进；到达配方时长后
     * 两输入各 shrink(1)、产出 1 个结果，进度归零重新计时。条件不满足时进度立即归零。
     */
    private boolean processRecipe() {
        ItemStack inputA = items.get(SLOT_INPUT_A);
        ItemStack inputB = items.get(SLOT_INPUT_B);
        ProcessorRecipe recipe = findRecipe(inputA, inputB);
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
            inputA.shrink(1);
            inputB.shrink(1);
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

    /** 槽位准入：两个输入槽接受任意物品（工作台语义），输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT_A || slot == SLOT_INPUT_B;
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

    /** 方块被移除时供 Block 调用：掉落槽内物品 */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }
}
