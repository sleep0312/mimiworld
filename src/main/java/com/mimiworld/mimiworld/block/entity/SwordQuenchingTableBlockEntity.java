package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.SwordQuenchingTableMenu;
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
 * 淬剑台方块实体（方块编码 164 = sword_quenching_table）。
 * <p>
 * 有序三输入熔炉式定时加工：槽0=左上、槽1=右上、槽2=正中间 → 槽3=最下方输出。
 * <b>不自动运转</b>：玩家必须按下 GUI 右侧"开始"按钮（{@link #requiresStartButton()}
 * 为 true），且当前配料确实命中配方时才启动一轮加工；加工完成或配料被取走后停止，
 * 需再次按下按钮。配方表独立，当前为空。
 */
public class SwordQuenchingTableBlockEntity extends MultiInputProcessorBlockEntity {

    private static final int INPUTS = 3;
    private static final List<ProcessorRecipe> RECIPES = new ArrayList<>();

    /** 注册一条淬火配方：左上 + 右上 + 正中间 → result（默认时长 200 tick） */
    public static void registerRecipe(ItemStack result, Item topLeft, Item topRight, Item center) {
        registerRecipe(result, DEFAULT_PROCESS_TICKS, topLeft, topRight, center);
    }

    /** 注册一条淬火配方（自定义加工时长 tick） */
    public static void registerRecipe(ItemStack result, int processTicks,
                                      Item topLeft, Item topRight, Item center) {
        RECIPES.add(new ProcessorRecipe(new Item[]{topLeft, topRight, center},
                result.copy(), processTicks));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<ProcessorRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    public SwordQuenchingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SWORD_QUENCHING_TABLE.get(), pos, state, INPUTS);
    }

    @Override
    protected boolean requiresStartButton() {
        return true;
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack[] inputs) {
        return matchOrdered(RECIPES, inputs);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.sword_quenching_table");
    }

    @Nullable
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                               ContainerData data, ContainerLevelAccess access) {
        return new SwordQuenchingTableMenu(containerId, playerInventory, this, data, access);
    }
}
