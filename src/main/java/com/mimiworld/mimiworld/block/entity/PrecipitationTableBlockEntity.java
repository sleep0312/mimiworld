package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.PrecipitationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 沉淀台方块实体（方块编码 57 = precipitation_table）。
 * 双输入定时加工、无燃料（见 {@link DualInputProcessorBlockEntity}）；
 * 当前配方表为空，后续内容通过 {@link #registerRecipe} 注册扩展。
 */
public class PrecipitationTableBlockEntity extends DualInputProcessorBlockEntity {

    /** 键：双输入排序 long（见 recipeKey） */
    private static final Map<Long, ProcessorRecipe> RECIPES = new HashMap<>();

    /** 注册一条沉淀配方：inputA + inputB → result，加工时长使用配方自带的 processTicks */
    public static void registerRecipe(Item inputA, Item inputB, ItemStack result, int processTicks) {
        RECIPES.put(recipeKey(inputA, inputB),
                new ProcessorRecipe(inputA, inputB, result, processTicks));
    }

    /** 注册一条沉淀配方（使用默认加工时长 {@value #DEFAULT_PROCESS_TICKS} tick） */
    public static void registerRecipe(Item inputA, Item inputB, ItemStack result) {
        registerRecipe(inputA, inputB, result, DEFAULT_PROCESS_TICKS);
    }

    @Nullable
    public static ProcessorRecipe findRecipeStatic(ItemStack inputA, ItemStack inputB) {
        if (inputA.isEmpty() || inputB.isEmpty()) {
            return null;
        }
        return RECIPES.get(recipeKey(inputA.getItem(), inputB.getItem()));
    }

    public PrecipitationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRECIPITATION_TABLE.get(), pos, state);
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack inputA, ItemStack inputB) {
        return findRecipeStatic(inputA, inputB);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.precipitation_table");
    }

    @Override
    @Nullable
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                               ContainerData data, ContainerLevelAccess access) {
        return new PrecipitationTableMenu(containerId, playerInventory, this, data, access);
    }
}
