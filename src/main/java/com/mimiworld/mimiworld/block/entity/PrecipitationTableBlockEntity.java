package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.PrecipitationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * 沉淀台方块实体（precipitation_table）。
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

    /**
     * 注册一条【单输入】沉淀配方：原料放任意一个输入槽、另一个输入槽必须为空时加工。
     * 键低位用 {@link #EMPTY_SLOT_MARKER} 占位，与任何双输入配方键都不冲突。
     */
    public static void registerRecipe(Item singleInput, ItemStack result, int processTicks) {
        RECIPES.put(singleInputKey(singleInput),
                new ProcessorRecipe(singleInput, null, result, processTicks));
    }

    /** 注册一条单输入沉淀配方（使用默认加工时长 {@value #DEFAULT_PROCESS_TICKS} tick） */
    public static void registerRecipe(Item singleInput, ItemStack result) {
        registerRecipe(singleInput, result, DEFAULT_PROCESS_TICKS);
    }

    /** 低位 32 位全 1 作为"另一槽为空"标记（真实物品注册 ID 不可能为 -1） */
    private static final long EMPTY_SLOT_MARKER = 0xFFFFFFFFL;

    private static long singleInputKey(Item input) {
        return ((long) BuiltInRegistries.ITEM.getId(input) << 32) | EMPTY_SLOT_MARKER;
    }

    @Nullable
    public static ProcessorRecipe findRecipeStatic(ItemStack inputA, ItemStack inputB) {
        boolean emptyA = inputA.isEmpty();
        boolean emptyB = inputB.isEmpty();
        if (emptyA && emptyB) {
            return null;
        }
        if (!emptyA && !emptyB) {
            // 双输入：无序匹配
            return RECIPES.get(recipeKey(inputA.getItem(), inputB.getItem()));
        }
        // 恰好一个槽有料：命中单输入配方（放 A 或 B 均可）
        Item single = emptyA ? inputB.getItem() : inputA.getItem();
        return RECIPES.get(singleInputKey(single));
    }

    /** JEI 插件读取：本机已注册配方（含单输入配方）的只读快照 */
    public static java.util.List<ProcessorRecipe> jeiRecipes() {
        return java.util.List.copyOf(RECIPES.values());
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
