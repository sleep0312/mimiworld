package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.CrusherMenu;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 搅碎器方块实体（方块编码 365 = crusher）。
 * 有序三输入熔炉式定时加工：槽0=左上、槽1=左下、槽2=右上 → 槽3=右下输出；
 * 配方表独立，当前为空。
 */
public class CrusherBlockEntity extends MultiInputProcessorBlockEntity {

    private static final int INPUTS = 3;
    private static final List<ProcessorRecipe> RECIPES = new ArrayList<>();

    /** 注册一条搅碎配方：左上 + 左下 + 右上 → result（默认时长 200 tick） */
    public static void registerRecipe(ItemStack result, Item topLeft, Item bottomLeft, Item topRight) {
        registerRecipe(result, DEFAULT_PROCESS_TICKS, topLeft, bottomLeft, topRight);
    }

    /** 注册一条搅碎配方（自定义加工时长 tick） */
    public static void registerRecipe(ItemStack result, int processTicks,
                                      Item topLeft, Item bottomLeft, Item topRight) {
        RECIPES.add(new ProcessorRecipe(new Item[]{topLeft, bottomLeft, topRight},
                result.copy(), processTicks));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<ProcessorRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER.get(), pos, state, INPUTS);
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack[] inputs) {
        return matchOrdered(RECIPES, inputs);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.crusher");
    }

    @Nullable
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                               ContainerData data, ContainerLevelAccess access) {
        return new CrusherMenu(containerId, playerInventory, this, data, access);
    }
}
