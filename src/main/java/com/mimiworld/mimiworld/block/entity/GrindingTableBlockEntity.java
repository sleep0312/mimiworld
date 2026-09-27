package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.GrindingTableMenu;
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
 * 碾磨台方块实体（方块编码 287 = grinding_table）。
 * 有序六输入熔炉式定时加工：槽0~2 为左列上中下、槽3~5 为右列上中下 → 槽6 底部中央输出；
 * 配方表独立，当前为空。
 */
public class GrindingTableBlockEntity extends MultiInputProcessorBlockEntity {

    private static final int INPUTS = 6;
    private static final List<ProcessorRecipe> RECIPES = new ArrayList<>();

    /**
     * 注册一条碾磨配方（默认时长 200 tick）。参数按槽位顺序：
     * 左列上/中/下、右列上/中/下（某槽不参与配料传 null，匹配时要求该槽为空）。
     */
    public static void registerRecipe(ItemStack result,
                                      Item leftTop, Item leftMid, Item leftBottom,
                                      Item rightTop, Item rightMid, Item rightBottom) {
        registerRecipe(result, DEFAULT_PROCESS_TICKS,
                leftTop, leftMid, leftBottom, rightTop, rightMid, rightBottom);
    }

    /** 注册一条碾磨配方（自定义加工时长 tick） */
    public static void registerRecipe(ItemStack result, int processTicks,
                                      Item leftTop, Item leftMid, Item leftBottom,
                                      Item rightTop, Item rightMid, Item rightBottom) {
        RECIPES.add(new ProcessorRecipe(
                new Item[]{leftTop, leftMid, leftBottom, rightTop, rightMid, rightBottom},
                result.copy(), processTicks));
    }

    /** JEI 插件读取：本机已注册配方的只读快照 */
    public static List<ProcessorRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    public GrindingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDING_TABLE.get(), pos, state, INPUTS);
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack[] inputs) {
        return matchOrdered(RECIPES, inputs);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mimiworld.grinding_table");
    }

    @Nullable
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
                                               ContainerData data, ContainerLevelAccess access) {
        return new GrindingTableMenu(containerId, playerInventory, this, data, access);
    }
}
