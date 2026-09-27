package com.mimiworld.mimiworld.block.entity;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * NxN 网格煮锅方块实体基类（煮锅 2x2 / 大煮锅 3x3 / 超大煮锅 4x4 / 巨无霸大大大煮锅 5x5）。
 * <p>
 * 熔炉式燃料定时加工（默认 200 tick）：左上 NxN 网格 + 右上单独输入槽共同构成配料，
 * 左下燃料槽烧炼（煤/木板/米木燃料等，燃烧时长走 {@link ForgeHooks#getBurnTime}），
 * 右下输出槽存放成品。配方为网格逐槽<b>有序</b>匹配（null=该格必须为空）+ 右上槽指定物品，
 * 各子类持有独立配方表，当前均为空。
 * <ul>
 *   <li>槽 0 ~ N*N-1：NxN 输入网格（行优先）；</li>
 *   <li>槽 N*N：右上单独输入槽；</li>
 *   <li>槽 N*N+1：燃料槽；</li>
 *   <li>槽 N*N+2：输出槽，禁止手动放入。</li>
 * </ul>
 */
public abstract class GridCookingPotBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider {

    public static final int DEFAULT_COOK_TICKS = 200;

    /** 一条煮锅配方：grid[i] 为网格第 i 格要求物品（null=该格必须为空），extra 为右上槽要求（null=必须空） */
    public record PotRecipe(Item[] grid, Item extra, ItemStack result, int cookTicks) {
    }

    protected final int gridSize;
    protected final int extraSlot;
    protected final int fuelSlot;
    protected final int resultSlot;
    protected final NonNullList<ItemStack> items;

    private int litTime = 0;
    private int litDuration = 0;
    private int cookingProgress = 0;
    private int cookingTotalTime = DEFAULT_COOK_TICKS;

    /*
     * DataSlot 布局（与 GridCookingPotMenu 一致）：
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

    protected GridCookingPotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                        int gridSize) {
        super(type, pos, state);
        this.gridSize = gridSize;
        this.extraSlot = gridSize * gridSize;
        this.fuelSlot = extraSlot + 1;
        this.resultSlot = fuelSlot + 1;
        this.items = NonNullList.withSize(resultSlot + 1, ItemStack.EMPTY);
    }

    // -------------------- 子类扩展点 --------------------

    public final int getGridSize() {
        return gridSize;
    }

    public final int getExtraSlot() {
        return extraSlot;
    }

    public final int getFuelSlot() {
        return fuelSlot;
    }

    public final int getResultSlot() {
        return resultSlot;
    }

    /** 查本机独立配方表；无命中返回 null */
    @Nullable
    protected abstract PotRecipe findRecipe(ItemStack[] grid, ItemStack extra);

    /** 由具体子类创建对应的菜单 */
    protected abstract AbstractContainerMenu createPotMenu(int containerId, Inventory playerInventory,
                                                           ContainerData data, ContainerLevelAccess access);

    /** 子类配方表有序匹配工具：网格逐槽比对（配方 null 位要求该格为空）+ 右上槽比对 */
    @Nullable
    protected static PotRecipe matchGrid(java.util.List<PotRecipe> recipes,
                                         ItemStack[] grid, ItemStack extra) {
        outer:
        for (PotRecipe recipe : recipes) {
            if (recipe.grid().length != grid.length) {
                continue;
            }
            for (int i = 0; i < grid.length; i++) {
                Item expected = recipe.grid()[i];
                ItemStack got = grid[i];
                if (expected == null) {
                    if (!got.isEmpty()) {
                        continue outer;
                    }
                } else if (got.isEmpty() || got.getItem() != expected) {
                    continue outer;
                }
            }
            Item expectedExtra = recipe.extra();
            if (expectedExtra == null) {
                if (!extra.isEmpty()) {
                    continue;
                }
            } else if (extra.isEmpty() || extra.getItem() != expectedExtra) {
                continue;
            }
            return recipe;
        }
        return null;
    }

    // -------------------- 服务端 tick（原版熔炉节奏） --------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  GridCookingPotBlockEntity be) {
        be.tick();
    }

    private void tick() {
        boolean changed = false;
        boolean wasLit = litTime > 0;

        if (litTime > 0) {
            litTime--;
        }

        ItemStack[] grid = collectGrid();
        ItemStack extra = items.get(extraSlot);
        PotRecipe recipe = findRecipe(grid, extra);
        boolean canCook = recipe != null && canOutputAccept(recipe.result());
        if (canCook) {
            cookingTotalTime = recipe.cookTicks();
        }

        // 需要点火且当前火已灭：消耗一个燃料
        if (litTime == 0 && canCook) {
            int burnTime = ForgeHooks.getBurnTime(items.get(fuelSlot), null);
            if (burnTime > 0) {
                ItemStack fuel = items.get(fuelSlot);
                if (fuel.hasCraftingRemainingItem()) {
                    items.set(fuelSlot, fuel.getCraftingRemainingItem());
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
                for (int i = 0; i < extraSlot; i++) {
                    if (recipe.grid()[i] != null) {
                        items.get(i).shrink(1);
                    }
                }
                if (recipe.extra() != null) {
                    items.get(extraSlot).shrink(1);
                }
                ItemStack output = items.get(resultSlot);
                if (output.isEmpty()) {
                    items.set(resultSlot, recipe.result().copy());
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

    private ItemStack[] collectGrid() {
        ItemStack[] grid = new ItemStack[extraSlot];
        for (int i = 0; i < extraSlot; i++) {
            grid[i] = items.get(i);
        }
        return grid;
    }

    /** 输出槽能否再放入这份结果：空槽可放；同种物品堆叠不爆自身上限可放 */
    private boolean canOutputAccept(ItemStack result) {
        ItemStack output = items.get(resultSlot);
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

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] up = new int[extraSlot + 1];
        for (int i = 0; i <= extraSlot; i++) {
            up[i] = i;
        }
        return switch (side) {
            case DOWN -> new int[]{resultSlot};
            case UP -> up;
            default -> new int[]{fuelSlot};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == resultSlot;
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

    // -------------------- Container --------------------

    @Override
    public int getContainerSize() {
        return items.size();
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

    /** 网格槽与右上输入槽接受任意物品；燃料槽只收可燃烧物品；输出槽不可放入 */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) {
            return false;
        }
        if (slot == resultSlot) {
            return false;
        }
        if (slot == fuelSlot) {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return createPotMenu(containerId, playerInventory, data, access);
    }
}
