package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.menu.RiceMakerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * 造米台方块实体（rice_maker）。
 * <p>
 * 3 个物品槽：
 * <ul>
 *   <li>槽 0：原料槽（仅可放入已注册配方的原料，目前仅小麦种子），可批量放入整组；</li>
 *   <li>槽 1：注水槽（仅可放入水桶），放入 1 桶水立即消耗水桶、在原槽返还空桶，
 *       并向内部水存储注入 {@value #WATER_PER_BUCKET} mB 水；</li>
 *   <li>槽 2：输出槽，禁止手动放入。</li>
 * </ul>
 * 内部液体存储：普通水，1 桶 = 1000 mB，最大 {@value #MAX_WATER} mB（= 10 桶）。
 * 配方消耗水与原料，加工循环每条配方独立计时（米配方 40 tick = 2 秒，每次只产出 1 个，
 * 即便槽内有一组原料也必须每 2 秒加工一次）。
 */
public class RiceMakerBlockEntity extends BlockEntity implements MenuProvider, net.minecraft.world.Container {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_WATER_BUCKET = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int TOTAL_SLOTS = 3;

    /** 每桶水注入的水量：1 桶 = 1000 mB */
    public static final int WATER_PER_BUCKET = 1000;
    /** 水存储上限：10000 mB = 10 桶水 */
    public static final int MAX_WATER = 10000;
    /** 默认/米配方加工时长（tick），2 秒 */
    public static final int RICE_PROCESS_TICKS = 40;
    /** 米配方耗水量（mB） */
    public static final int RICE_WATER_COST = 100;

    // -------------------- 配方表 --------------------

    /** 一条「原料 + 水 → 产物」配方 */
    public record RiceRecipe(Item input, ItemStack result, int waterCost, int processTicks) {
    }

    /** 键：原料物品；可放原料槽的物品必须在此表中 */
    private static final Map<Item, RiceRecipe> RECIPES = new HashMap<>();

    static {
        // 1 小麦种子 + 100 mB 水 → 1 米，加工 2 秒（40 tick）
        registerRecipe(new RiceRecipe(
                Items.WHEAT_SEEDS,
                new ItemStack(ModItems.RICE.get()),
                RICE_WATER_COST,
                RICE_PROCESS_TICKS));
    }

    /** 注册一条「原料 + 水 → 产物」配方。应在模组加载阶段调用。 */
    public static void registerRecipe(RiceRecipe recipe) {
        RECIPES.put(recipe.input(), recipe);
    }

    @Nullable
    public static RiceRecipe findRecipe(ItemStack input) {
        return input.isEmpty() ? null : RECIPES.get(input.getItem());
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static java.util.List<RiceRecipe> jeiRecipes() {
        return java.util.List.copyOf(RECIPES.values());
    }

    // -------------------- 状态 --------------------

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int progress = 0;
    private int water = 0;

    /*
     * DataSlot 布局（水量上限 10000 < 单 slot 上限 65535，无需拆分）：
     * 0 = 当前进度
     * 1 = 当前配方加工总时长
     * 2 = 当前水量（mB）
     * 3 = 储水上限（mB）
     */
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_WATER = 2;
    public static final int DATA_MAX_WATER = 3;
    public static final int DATA_COUNT = 4;

    private final ContainerData data = new SimpleContainerData(DATA_COUNT);

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public RiceMakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RICE_MAKER.get(), pos, state);
        this.data.set(DATA_MAX_PROGRESS, RICE_PROCESS_TICKS);
        this.data.set(DATA_MAX_WATER, MAX_WATER);
        syncWaterToData();
    }

    // -------------------- 服务端 tick --------------------

    /** 服务端每 tick 加工逻辑（BlockEntityTicker 回调） */
    public static void serverTick(Level level, BlockPos pos, BlockState state, RiceMakerBlockEntity be) {
        be.tick();
    }

    private void tick() {
        boolean changed = consumeWaterBucket();
        changed |= processRecipe();
        if (changed) {
            setChanged();
        }
    }

    /**
     * 注水槽投入水桶时立即处理：消耗 1 个水桶 → 原槽返还 1 个空桶 + 注入 1B 水。
     * 水已满时不处理（水桶留在槽内）。水桶原版最大堆叠为 1，无需处理成组情况。
     */
    private boolean consumeWaterBucket() {
        ItemStack bucketSlot = items.get(SLOT_WATER_BUCKET);
        if (bucketSlot.is(Items.WATER_BUCKET) && water + WATER_PER_BUCKET <= MAX_WATER) {
            water += WATER_PER_BUCKET;
            items.set(SLOT_WATER_BUCKET, new ItemStack(Items.BUCKET));
            syncWaterToData();
            return true;
        }
        return false;
    }

    /**
     * 配方加工循环：原料与水持续满足时进度推进，到达配方时长后消耗 1 原料 + 定量水、
     * 产出 1 个结果，进度归零重新计时（每 2 秒只产出一次，原料整组放入也一样）。
     */
    private boolean processRecipe() {
        RiceRecipe recipe = findRecipe(items.get(SLOT_INPUT));
        boolean canProcess = recipe != null
                && water >= recipe.waterCost()
                && canOutputAccept(recipe.result());

        if (!canProcess) {
            if (progress != 0) {
                progress = 0;
                data.set(DATA_PROGRESS, progress);
                return true;
            }
            return false;
        }

        progress++;
        if (progress >= recipe.processTicks()) {
            items.get(SLOT_INPUT).shrink(1);
            water -= recipe.waterCost();
            ItemStack output = items.get(SLOT_OUTPUT);
            if (output.isEmpty()) {
                items.set(SLOT_OUTPUT, recipe.result().copy());
            } else {
                output.grow(recipe.result().getCount());
            }
            progress = 0;
            syncWaterToData();
        }
        data.set(DATA_PROGRESS, progress);
        return true;
    }

    /** 输出槽能否再放入这份结果：空槽可放；同种物品堆叠不爆上限可放 */
    private boolean canOutputAccept(ItemStack result) {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= getMaxStackSize();
    }

    private void syncWaterToData() {
        this.data.set(DATA_WATER, water);
    }

    // -------------------- 存档 --------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("Water", water);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        water = tag.getInt("Water");
        data.set(DATA_PROGRESS, progress);
        syncWaterToData();
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

    /** 槽位准入：原料槽仅接受已注册配方的原料；注水槽仅接受水桶；输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            return findRecipe(stack) != null;
        }
        if (slot == SLOT_WATER_BUCKET) {
            return stack.is(Items.WATER_BUCKET);
        }
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // -------------------- 菜单 --------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.rice_maker");
    }

    @Nullable
    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new RiceMakerMenu(containerId, playerInventory, this, data, access);
    }

    /** 方块被移除时供 Block 调用：掉落槽内物品 */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }
}
