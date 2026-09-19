package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.RiceMakerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 造米台方块实体（方块编码 27 = rice_maker）。
 * <p>
 * 3 个物品槽：0/1 为输入槽，2 为输出槽。两个输入满足已注册的 2→1 配方时开始加工，
 * 进度走完 {@link #MAX_PROGRESS} 后各消耗 1 个输入并产出结果；输出槽禁止玩家手动放入。
 * 配方默认是空表，使用 {@link #registerRecipe} 注册（后续也可以换成数据驱动配方）。
 */
public class RiceMakerBlockEntity extends BlockEntity implements MenuProvider, net.minecraft.world.Container {

    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int TOTAL_SLOTS = 3;

    /** 加工总时长（tick），120 = 6 秒 */
    public static final int MAX_PROGRESS = 120;

    /** 2→1 配方表：键为「无序」的两个输入物品，值为输出（含数量）。两个输入不分左右 */
    private static final Map<Long, ItemStack> RECIPES = new HashMap<>();

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int progress = 0;

    // DataSlot 0 = 当前进度（由 Menu 同步写回），DataSlot 1 = 总进度常量
    private final ContainerData data = new SimpleContainerData(2);

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public RiceMakerBlockEntity(BlockPos pos, BlockState state) {
        super(com.mimiworld.mimiworld.block.entity.ModBlockEntities.RICE_MAKER.get(), pos, state);
        this.data.set(1, MAX_PROGRESS);
    }

    /** 注册一条无序 2→1 配方（输出数量由 result 自带）。应在模组加载阶段调用。 */
    public static void registerRecipe(Item inputA, Item inputB, ItemStack result) {
        RECIPES.put(recipeKey(inputA, inputB), result.copy());
    }

    /** 查询两个输入的配方结果，找不到返回空栈；输入不分左右 */
    public static ItemStack findResult(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = RECIPES.get(recipeKey(a.getItem(), b.getItem()));
        return result == null ? ItemStack.EMPTY : result.copy();
    }

    private static long recipeKey(Item a, Item b) {
        int ia = Item.getId(a);
        int ib = Item.getId(b);
        if (ia > ib) {
            int t = ia;
            ia = ib;
            ib = t;
        }
        return ((long) ia << 32) | (ib & 0xFFFFFFFFL);
    }

    /** 服务端每 tick 加工逻辑（BlockEntityTicker 回调） */
    public static void serverTick(Level level, BlockPos pos, BlockState state, RiceMakerBlockEntity be) {
        be.tick();
    }

    private void tick() {
        ItemStack result = findResult(items.get(SLOT_INPUT_1), items.get(SLOT_INPUT_2));
        boolean canProcess = !result.isEmpty() && canOutputAccept(result);

        if (canProcess) {
            progress++;
            if (progress >= MAX_PROGRESS) {
                items.get(SLOT_INPUT_1).shrink(1);
                items.get(SLOT_INPUT_2).shrink(1);
                ItemStack output = items.get(SLOT_OUTPUT);
                if (output.isEmpty()) {
                    items.set(SLOT_OUTPUT, result.copy());
                } else {
                    output.grow(result.getCount());
                }
                progress = 0;
            }
            data.set(0, progress);
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            data.set(0, progress);
            setChanged();
        }
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

    public int getProgress() {
        return progress;
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
        data.set(0, progress);
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

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != SLOT_OUTPUT;
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
