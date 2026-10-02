package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.ArmorForgingTableMenu;
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
 * 铸甲台方块实体（armor_forging_table）。
 * 3x3 工作台式即时合成（见 {@link ForgingTableBlockEntity}），配方表<b>独立</b>，
 * 与铸剑台/铸弓台/原版工作台均不通用。当前配方表为空，通过 {@link #registerShaped}
 * / {@link #registerShapeless} 注册扩展。
 */
public class ArmorForgingTableBlockEntity extends ForgingTableBlockEntity {

    private static final List<ForgingRecipe> RECIPES = new ArrayList<>();

    /** 注册一条有序配方：pattern 1~3 行等宽（' ' 为空），字符到物品的映射由 keys 给出 */
    public static void registerShaped(ItemStack result, String[] pattern, Map<Character, Item> keys) {
        RECIPES.add(ForgingRecipe.shaped(result, pattern, keys));
    }

    /** 注册一条无序配方 */
    public static void registerShapeless(ItemStack result, Item... ingredients) {
        RECIPES.add(ForgingRecipe.shapeless(result, ingredients));
    }

    public ArmorForgingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARMOR_FORGING_TABLE.get(), pos, state);
    }

    @Override
    protected ItemStack findRecipe(ItemStack[] grid) {
        return matchRecipes(RECIPES, grid);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.armor_forging_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ContainerLevelAccess access = level == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(level, worldPosition);
        return new ArmorForgingTableMenu(containerId, playerInventory, this, access);
    }
}
