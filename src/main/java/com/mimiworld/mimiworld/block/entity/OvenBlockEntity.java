package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.OvenMenu;
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
 * 烤箱方块实体（方块编码 373 = oven）：GUI/槽位与大煮锅相同，
 * 3x3 网格（9 入）+ 右上单独输入 + 燃料 + 输出。配方为网格有序 + 右上槽指定物品，
 * 配方表独立（与大煮锅不通用），当前为空。
 */
public class OvenBlockEntity extends GridCookingPotBlockEntity {

    private static final List<PotRecipe> RECIPES = new ArrayList<>();

    /** 注册一条默认时长（200t）配方：grid 按行优先给满 3x3=9 格（null 位表示该格必须为空），extra 为右上槽（可 null） */
    public static void registerRecipe(ItemStack result, Item extra, Item... grid) {
        registerRecipe(result, extra, DEFAULT_COOK_TICKS, grid);
    }

    /** 注册一条自定义时长配方 */
    public static void registerRecipe(ItemStack result, Item extra, int cookTicks, Item... grid) {
        Item[] copy = new Item[9];
        System.arraycopy(grid, 0, copy, 0, Math.min(grid.length, 9));
        RECIPES.add(new PotRecipe(copy, extra, result.copy(), cookTicks));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<PotRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    public OvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OVEN.get(), pos, state, 3);
    }

    @Override
    protected PotRecipe findRecipe(ItemStack[] grid, ItemStack extra) {
        return matchGrid(RECIPES, grid, extra);
    }

    @Override
    protected AbstractContainerMenu createPotMenu(int containerId, Inventory playerInventory,
                                                  ContainerData data, ContainerLevelAccess access) {
        return new OvenMenu(containerId, playerInventory, this, data, access);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.mimiworld.oven");
    }
}
