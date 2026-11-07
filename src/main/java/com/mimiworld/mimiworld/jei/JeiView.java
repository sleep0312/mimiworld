package com.mimiworld.mimiworld.jei;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * JEI 视图模型：一条机器配方在 JEI 分类中渲染所需的<b>唯一槽位契约</b>。
 * <p>
 * 所有机器的配方记录（ProcessorRecipe / PotRecipe / GridRecipe / MixerRecipe 等）
 * 都由 {@link JeiLayouts} 中各自唯一的映射函数转换为本模型；{@link ViewCategory}
 * 只按 {@link Slot#x()} / {@link Slot#y()} 摆放，坐标语义在整条链路上只有一份定义。
 *
 * @param inputs    输入槽（像素坐标 + 物品或标签）；顺序无关紧要，位置即语义
 * @param output    产物（数量已在 ItemStack 中）
 * @param shapeless 是否在右下角显示 JEI 无序配方标记
 */
public record JeiView(List<Slot> inputs, ItemStack output, boolean shapeless) {

    /**
     * 一个输入槽：tag 非 null 时按标签配料展开（如 mimiworld:meats）；
     * fluid 非 null 时显示流体（amount 单位 mB，如米片机 100mB 水）；否则显示 stack。
     */
    public record Slot(int x, int y, ItemStack stack, TagKey<Item> tag,
                       Fluid fluid, long fluidAmount) {

        public static Slot item(int x, int y, Item item) {
            return item(x, y, item, 1);
        }

        public static Slot item(int x, int y, Item item, int count) {
            return new Slot(x, y, new ItemStack(item, count), null, null, 0);
        }

        public static Slot tag(int x, int y, TagKey<Item> tag) {
            return new Slot(x, y, null, tag, null, 0);
        }

        public static Slot fluid(int x, int y, Fluid fluid, long amountMb) {
            return new Slot(x, y, null, null, fluid, amountMb);
        }
    }
}
