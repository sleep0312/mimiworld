package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.menu.BloodlettingTableMenu;
import com.mimiworld.mimiworld.tags.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
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
 * 汲血台方块实体（bloodletting_table）。
 * 有序双输入熔炉式定时加工（槽0=左上槽 / 槽1=左下槽 → 槽2 输出），配方表独立。
 * 除普通精确物品配方外，支持上槽按<b>物品标签</b>匹配（如"任意肉类"）：
 * 命中标签配方时以槽内实际物品回填 {@link ProcessorRecipe}，基类仍按两个非空槽各消耗 1 个。
 */
public class BloodlettingTableBlockEntity extends MultiInputProcessorBlockEntity {

    private static final int INPUTS = 2;
    private static final List<ProcessorRecipe> RECIPES = new ArrayList<>();
    /** 上槽按标签匹配的配方（如任意肉类 + 玻璃瓶） */
    private static final List<TaggedRecipe> TAG_RECIPES = new ArrayList<>();

    /** 上槽标签配方：上槽物品属于 tag、下槽恰为 bottom → result */
    public record TaggedRecipe(TagKey<Item> topTag, Item bottom, ItemStack result, int processTicks) {
    }

    /** 注册一条汲血配方：input0（左上槽）+ input1（左下槽）→ result，时长默认 200 tick */
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

    /**
     * 注册一条标签汲血配方：左上槽放属于 topTag 的任意物品（如 {@link ModItemTags#MEATS} 任意肉类），
     * 左下槽恰放 bottom（如玻璃瓶），各消耗 1 个产出 1 份 result；时长默认 200 tick。
     */
    public static void registerRecipe(ItemStack result, TagKey<Item> topTag, Item bottom) {
        registerRecipe(result, DEFAULT_PROCESS_TICKS, topTag, bottom);
    }

    /** 注册一条标签汲血配方（自定义加工时长 tick） */
    public static void registerRecipe(ItemStack result, int processTicks,
                                      TagKey<Item> topTag, Item bottom) {
        TAG_RECIPES.add(new TaggedRecipe(topTag, bottom, result.copy(), processTicks));
    }

    /** JEI 插件读取：精确物品配方只读快照 */
    public static List<ProcessorRecipe> jeiRecipes() {
        return List.copyOf(RECIPES);
    }

    /** JEI 插件读取：标签配方只读快照 */
    public static List<TaggedRecipe> jeiTaggedRecipes() {
        return List.copyOf(TAG_RECIPES);
    }

    public BloodlettingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOODLETTING_TABLE.get(), pos, state, INPUTS);
    }

    @Override
    @Nullable
    protected ProcessorRecipe findRecipe(ItemStack[] inputs) {
        ProcessorRecipe exact = matchOrdered(RECIPES, inputs);
        if (exact != null) {
            return exact;
        }
        // 标签配方：两个槽都非空，上槽物品属于标签、下槽物品精确匹配
        if (inputs.length == INPUTS && !inputs[0].isEmpty() && !inputs[1].isEmpty()) {
            Item topItem = inputs[0].getItem();
            Item bottomItem = inputs[1].getItem();
            for (TaggedRecipe recipe : TAG_RECIPES) {
                if (inputs[0].is(recipe.topTag()) && bottomItem == recipe.bottom()) {
                    // 用槽内实际物品回填，使基类对两个非空槽各 shrink(1)
                    return new ProcessorRecipe(new Item[]{topItem, bottomItem},
                            recipe.result(), recipe.processTicks());
                }
            }
        }
        return null;
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
