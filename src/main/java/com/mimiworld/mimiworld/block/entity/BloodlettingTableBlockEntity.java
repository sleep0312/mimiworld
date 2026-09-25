package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.BloodlettingTableMenu;
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
 * 汲血台方块实体（方块编码 166 = bloodletting_table）。
 * 有序双输入熔炉式定时加工（槽0/槽1 两个输入槽 → 槽2 输出），配方表独立，当前为空。
 */
public class BloodlettingTableBlockEntity extends MultiInputProcessorBlockEntity {

    private static final int INPUTS = 2;
    private static final List<ProcessorRecipe> RECIPES = new ArrayList<>();

    /** 注册一条汲血配方：input0（上槽）+ input1（下槽）→ result，时长默认 200 tick */
    public static void registerRecipe(ItemStack result, Item input0, Item input1) {
        registerRecipe(result, DEFAULT_PROCESS_TICKS, input0, input1);
    }

    /** 注册一条汲血配方（自定义加工时长 tick） */
    public static void registerRecipe(ItemStack result, int processTicks, Item input0, Item input1) {
        Item[] inputs = new Item[INPUTS];
        inputs[0] = input0;
        inputs[1] = input1;
        RECIPES.add(new ProcessorRecipe(inputs, result.copy(), processTicks));
    }

    public BloodlettingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOODLETTING_TABLE.get(), pos, state, INPUTS);
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack[] inputs) {
        return matchOrdered(RECIPES, inputs);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.bloodletting_table");
    }

    @Nullable
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                               ContainerData data, ContainerLevelAccess access) {
        return new BloodlettingTableMenu(containerId, playerInventory, this, data, access);
    }
}
