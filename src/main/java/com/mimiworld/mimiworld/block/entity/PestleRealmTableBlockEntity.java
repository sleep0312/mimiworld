package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.PestleRealmTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 杵界台方块实体（pestle_realm_table）。
 * 2x2 工作台式即时合成（见 {@link GridCraftingTableBlockEntity}）：上方 4 格输入、
 * 右下角结果槽，配方表<b>独立</b>，与凝锭台/原版工作台均不通用。
 * 当前配方表为空，通过 {@link #registerShaped} / {@link #registerShapeless} 注册扩展。
 */
public class PestleRealmTableBlockEntity extends GridCraftingTableBlockEntity {

    /** 本机网格边长：2x2 */
    private static final int GRID = 2;

    private static final List<GridRecipe> RECIPES = new ArrayList<>();

    /** 注册一条有序配方：pattern 1~2 行等宽（' ' 为空），字符到物品的映射由 keys 给出 */
    public static void registerShaped(ItemStack result, String[] pattern, Map<Character, Item> keys) {
        RECIPES.add(GridRecipe.shaped(GRID, result, pattern, keys));
    }

    /** 注册一条无序配方（最多 4 种配料，每格 1 件） */
    public static void registerShapeless(ItemStack result, Item... ingredients) {
        RECIPES.add(GridRecipe.shapeless(GRID, result, ingredients));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<GridRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    public PestleRealmTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PESTLE_REALM_TABLE.get(), pos, state, GRID);
    }

    @Override
    protected ItemStack findRecipe(ItemStack[] grid) {
        return matchRecipes(RECIPES, grid);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.pestle_realm_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new PestleRealmTableMenu(containerId, playerInventory, this, access);
    }
}
