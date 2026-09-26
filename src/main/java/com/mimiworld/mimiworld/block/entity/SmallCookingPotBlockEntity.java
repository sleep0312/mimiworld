package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.SmallCookingPotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 小煮锅方块实体（方块编码 367 = small_cooking_pot）。
 * 加工节奏与原版熔炉一致：输入槽放配料、燃料槽放燃料（煤/木板/木棍等，
 * 燃烧时长通过 {@link ForgeHooks#getBurnTime} 获取，本模组 ModFuelEvents 注册的燃料同样生效），
 * 配方命中且输出槽可容纳时持续烹煮，默认 200 tick（10 秒）产出 1 份。
 * <ul>
 *   <li>槽 0：输入（漏斗仅上方可入）；</li>
 *   <li>槽 1：燃料（侧方可入）；</li>
 *   <li>槽 2：输出（仅下方可抽）。</li>
 * </ul>
 * 配方为单输入 -> 单输出（可指定自定义时长），配方表独立且当前为空。
 */
public class SmallCookingPotBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_RESULT = 2;
    public static final int TOTAL_SLOTS = 3;

    public static final int DEFAULT_COOK_TICKS = 200;

    /** 一条烹煮配方：1 件输入 -> 1 份输出，cookTicks 为加工时长 */
    public record CookingRecipe(Item input, ItemStack result, int cookTicks) {
    }

    private static final List<CookingRecipe> RECIPES = new ArrayList<>();

    /** 注册一条默认时长（200t）的烹煮配方 */
    public static void registerRecipe(ItemStack result, Item input) {
        registerRecipe(result, input, DEFAULT_COOK_TICKS);
    }

    /** 注册一条自定义时长的烹煮配方 */
    public static void registerRecipe(ItemStack result, Item input, int cookTicks) {
        RECIPES.add(new CookingRecipe(input, result.copy(), cookTicks));
    }

    @Nullable
    private static CookingRecipe findRecipeFor(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        for (CookingRecipe recipe : RECIPES) {
            if (input.is(recipe.input())) {
                return recipe;
            }
        }
        return null;
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    private int litTime = 0;
    private int litDuration = 0;
    private int cookingProgress = 0;
    private int cookingTotalTime = DEFAULT_COOK_TICKS;

    /*
     * DataSlot 布局（与 SmallCookingPotMenu 一致）：
     * 0 = 燃料剩余燃烧 tick，1 = 当前燃料总燃烧 tick，
     * 2 = 烹煮进度，3 = 烹煮总时长
     */
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_COUNT = 4;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTime;
                case DATA_LIT_DURATION -> litDuration;
                case DATA_PROGRESS -> cookingProgress;
                case DATA_MAX_PROGRESS -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LIT_TIME -> litTime = value;
                case DATA_LIT_DURATION -> litDuration = value;
                case DATA_PROGRESS -> cookingProgress = value;
                case DATA_MAX_PROGRESS -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private final LazyOptional<? extends IItemHandler>[] handlers =
            SidedInvWrapper.create(this, Direction.values());

    public SmallCookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMALL_COOKING_POT.get(), pos, state);
    }

    // -------------------- 服务端 tick（原版 AbstractFurnaceBlockEntity 节奏） --------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  SmallCookingPotBlockEntity be) {
        be.tick();
    }

    private void tick() {
        boolean changed = false;
        boolean wasLit = litTime > 0;

        if (litTime > 0) {
            litTime--;
        }

        ItemStack input = items.get(SLOT_INPUT);
        CookingRecipe recipe = findRecipeFor(input);
        boolean canCook = recipe != null && canOutputAccept(recipe.result());
        if (canCook) {
            cookingTotalTime = recipe.cookTicks();
        }

        // 需要点火且当前火已灭：消耗一个燃料
        if (litTime == 0 && canCook) {
            int burnTime = ForgeHooks.getBurnTime(items.get(SLOT_FUEL), null);
            if (burnTime > 0) {
                ItemStack fuel = items.get(SLOT_FUEL);
                if (fuel.hasCraftingRemainingItem()) {
                    items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                } else {
                    fuel.shrink(1);
                }
                litTime = burnTime;
                litDuration = burnTime;
                changed = true;
            }
        }

        // 有火且能煮：推进进度，满一个周期产出
        if (litTime > 0 && canCook) {
            cookingProgress++;
            if (cookingProgress >= cookingTotalTime) {
                cookingProgress = 0;
                cookingTotalTime = recipe.cookTicks();
                input.shrink(1);
                ItemStack output = items.get(SLOT_RESULT);
                if (output.isEmpty()) {
                    items.set(SLOT_RESULT, recipe.result().copy());
                } else {
                    output.grow(recipe.result().getCount());
                }
                changed = true;
            }
        } else if (cookingProgress > 0) {
            // 条件不满足时进度缓慢回退（同原版熔炉）
            cookingProgress = Math.max(0, cookingProgress - 2);
            changed = true;
        }

        if (wasLit != (litTime > 0)) {
            changed = true;
        }

        data.set(DATA_LIT_TIME, litTime);
        data.set(DATA_LIT_DURATION, litDuration);
        data.set(DATA_PROGRESS, cookingProgress);
        data.set(DATA_MAX_PROGRESS, cookingTotalTime);

        if (changed) {
            setChanged();
        }
    }

    /** 输出槽能否再放入这份结果：空槽可放；同种物品堆叠不爆自身上限可放 */
    private boolean canOutputAccept(ItemStack result) {
        ItemStack output = items.get(SLOT_RESULT);
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
        tag.putInt("BurnTime", litTime);
        tag.putInt("BurnDuration", litDuration);
        tag.putInt("CookTime", cookingProgress);
        tag.putInt("CookTimeTotal", cookingTotalTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        litTime = tag.getInt("BurnTime");
        litDuration = tag.getInt("BurnDuration");
        cookingProgress = tag.getInt("CookTime");
        cookingTotalTime = tag.getInt("CookTimeTotal");
        if (cookingTotalTime <= 0) {
            cookingTotalTime = DEFAULT_COOK_TICKS;
        }
    }

    // -------------------- 方向感知（WorldlyContainer） --------------------

    private static final int[] SLOTS_DOWN = {SLOT_RESULT};
    private static final int[] SLOTS_UP = {SLOT_INPUT};
    private static final int[] SLOTS_SIDE = {SLOT_FUEL};

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> SLOTS_DOWN;
            case UP -> SLOTS_UP;
            default -> SLOTS_SIDE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == SLOT_RESULT;
    }

    // -------------------- 能力 --------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return handlers[side.ordinal()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : handlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        // 无法重建 final handlers 数组（原版熔炉同样不重建），重载后通常已有新 BE
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
        ItemStack old = items.get(slot);
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        // 首次放入新配料时重置烹饪总时长（与原版熔炉一致）
        if (slot == SLOT_INPUT && !stack.is(old.getItem())) {
            CookingRecipe recipe = findRecipeFor(stack);
            if (recipe != null) {
                cookingTotalTime = recipe.cookTicks();
            }
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

    /** 输入槽任意物品；燃料槽只收可燃烧物品；输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            return true;
        }
        if (slot == SLOT_FUEL) {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }

    // -------------------- 菜单 --------------------

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.mimiworld.small_cooking_pot");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new SmallCookingPotMenu(containerId, playerInventory, this, data, access);
    }
}
