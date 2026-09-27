package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.IronWokMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 铁锅方块实体（方块编码 372 = iron_wok）。
 * <p>
 * 逻辑与 2x2 网格煮锅同构（4 个输入槽，仅 GUI 里摆成菱形）：
 * 槽 0~3 = 四个输入（GUI 顺序：左上、右上、左下、右下），槽 4 = 右上单独输入，
 * 槽 5 = 燃料，槽 6 = 输出。配方为四输入逐槽<b>有序</b>匹配 + 右上槽指定物品，
 * 配方表独立且当前为空。
 */
public class IronWokBlockEntity extends GridCookingPotBlockEntity {

    private static final List<PotRecipe> RECIPES = new ArrayList<>();

    /** 注册一条默认时长（200t）配方：inputs 按槽序给满 4 个输入（null 位表示该槽必须为空），extra 为右上槽（可 null） */
    public static void registerRecipe(ItemStack result, Item extra, Item... inputs) {
        registerRecipe(result, extra, DEFAULT_COOK_TICKS, inputs);
    }

    /** 注册一条自定义时长配方 */
    public static void registerRecipe(ItemStack result, Item extra, int cookTicks, Item... inputs) {
        Item[] copy = new Item[4];
        System.arraycopy(inputs, 0, copy, 0, Math.min(inputs.length, 4));
        RECIPES.add(new PotRecipe(copy, extra, result.copy(), cookTicks));
    }

    public IronWokBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_WOK.get(), pos, state, 2);
    }

    @Override
    protected PotRecipe findRecipe(ItemStack[] grid, ItemStack extra) {
        return matchGrid(RECIPES, grid, extra);
    }

    @Override
    protected AbstractContainerMenu createPotMenu(int containerId, Inventory playerInventory,
                                                  ContainerData data, ContainerLevelAccess access) {
        return new IronWokMenu(containerId, playerInventory, this, data, access);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.mimiworld.iron_wok");
    }
}
