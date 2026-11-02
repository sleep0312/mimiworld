package com.mimiworld.mimiworld.jei;

import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.DualInputProcessorBlockEntity;
import com.mimiworld.mimiworld.block.entity.GridCraftingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.GridCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.MixerBlockEntity;
import com.mimiworld.mimiworld.block.entity.MultiInputProcessorBlockEntity;
import com.mimiworld.mimiworld.block.entity.RiceMakerBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * 各机器配方记录 → {@link JeiView} 的映射函数（每族机器仅此一份布局定义）。
 * <p>
 * 坐标口径：全部是 GUI 内的<b>绝对像素坐标</b>，与各 Menu#addSlot 逐像素一致
 * （即游戏内 16x16 物品格左上角），因此 JEI 页面能直接复用机器 GUI 贴图。
 * <ul>
 *   <li>MultiInput（搅碎器/碾磨台/淬剑台/汲血台）：inputs 下标 → 真实槽坐标表，null 跳过；</li>
 *   <li>DualInput（电离/沉淀）：A(61,20) / B(61,46)；</li>
 *   <li>离解台：上槽 (35,24) / 下槽 (79,53)；</li>
 *   <li>网格工作台（杵界台/凝锭台）：shaped 按裁剪矩阵逐格，shapeless 行优先填满；</li>
 *   <li>煮锅族：NxN 网格 (8,17) 间距 18 + 右上独立输入槽（各锅位置不同）；</li>
 *   <li>搅拌器：左列 (43,10+21r)、右列 (115,10+21r)，中央槽 (79,84)；</li>
 *   <li>锤米台：两槽 (30,26)/(30,62)；</li>
 *   <li>米片机：原料 (30,26) + 注水槽 (30,62)。</li>
 * </ul>
 * 产物槽坐标由 {@link ViewCategory} 持有（见 MimiJeiPlugin 的注册参数）。
 */
final class JeiLayouts {

    private JeiLayouts() {
    }

    // -------------------- A. MultiInputProcessor（搅碎器/碾磨台/淬剑台/汲血台）--------------------

    static List<JeiView> multi(List<MultiInputProcessorBlockEntity.ProcessorRecipe> recipes, int[][] pos) {
        List<JeiView> views = new ArrayList<>();
        for (MultiInputProcessorBlockEntity.ProcessorRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] inputs = recipe.inputs();
            for (int i = 0; i < pos.length && i < inputs.length; i++) {
                if (inputs[i] != null) {
                    slots.add(JeiView.Slot.item(pos[i][0], pos[i][1], inputs[i]));
                }
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    // -------------------- B. DualInputProcessor（电离台/沉淀台）--------------------

    static List<JeiView> dual(List<DualInputProcessorBlockEntity.ProcessorRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (DualInputProcessorBlockEntity.ProcessorRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            slots.add(JeiView.Slot.item(61, 20, recipe.inputA(), recipe.countA()));
            if (recipe.hasInputB()) {
                slots.add(JeiView.Slot.item(61, 46, recipe.inputB(), recipe.countB()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), true));
        }
        return views;
    }

    // -------------------- C. 离解台（上槽 + 下槽）--------------------

    static List<JeiView> dissociation(List<DissociationTableBlockEntity.DissociationRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (DissociationTableBlockEntity.DissociationRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.item(35, 24, recipe.top()),
                    JeiView.Slot.item(79, 53, recipe.bottom()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }

    // -------------------- D. 网格工作台（杵界台 2x2 / 凝锭台 5x5）--------------------

    static List<JeiView> gridCrafting(int n, int gridX, int gridY, int pitchX, int pitchY,
                                      List<GridCraftingTableBlockEntity.GridRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (GridCraftingTableBlockEntity.GridRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            boolean shapeless;
            Item[][] shaped = recipe.jeiShaped();
            if (shaped != null) {
                // 有序：裁剪后的矩阵按行列逐格摆放
                shapeless = false;
                for (int r = 0; r < shaped.length; r++) {
                    for (int c = 0; c < shaped[r].length; c++) {
                        if (shaped[r][c] != null) {
                            slots.add(JeiView.Slot.item(gridX + c * pitchX, gridY + r * pitchY, shaped[r][c]));
                        }
                    }
                }
            } else {
                // 无序：配料行优先填入网格
                shapeless = true;
                List<Item> ingredients = recipe.jeiShapeless();
                for (int i = 0; i < ingredients.size(); i++) {
                    slots.add(JeiView.Slot.item(gridX + (i % n) * pitchX, gridY + (i / n) * pitchY,
                            ingredients.get(i)));
                }
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), shapeless));
        }
        return views;
    }

    // -------------------- E. 煮锅族（NxN 网格 + 右上独立输入槽）--------------------

    static List<JeiView> pot(int n, int extraX, int extraY,
                             List<GridCookingPotBlockEntity.PotRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (GridCookingPotBlockEntity.PotRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] grid = recipe.grid();
            for (int i = 0; i < grid.length && i < n * n; i++) {
                if (grid[i] != null) {
                    slots.add(JeiView.Slot.item(8 + (i % n) * 18, 17 + (i / n) * 18, grid[i]));
                }
            }
            if (recipe.extra() != null) {
                slots.add(JeiView.Slot.item(extraX, extraY, recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    /** 铁锅：4 槽菱形（8,17)/(62,17)/(26,35)/(44,35)，右上独立槽 (116,26)。 */
    static List<JeiView> ironWok(List<GridCookingPotBlockEntity.PotRecipe> recipes) {
        int[][] pos = {{8, 17}, {62, 17}, {26, 35}, {44, 35}};
        List<JeiView> views = new ArrayList<>();
        for (GridCookingPotBlockEntity.PotRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] grid = recipe.grid();
            for (int i = 0; i < grid.length && i < pos.length; i++) {
                if (grid[i] != null) {
                    slots.add(JeiView.Slot.item(pos[i][0], pos[i][1], grid[i]));
                }
            }
            if (recipe.extra() != null) {
                slots.add(JeiView.Slot.item(116, 26, recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    // -------------------- F. 搅拌器（两列四行 + 中央槽）--------------------

    static List<JeiView> mixer(List<MixerBlockEntity.MixerRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (MixerBlockEntity.MixerRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            List<Item> ingredients = recipe.ingredients();
            for (int i = 0; i < ingredients.size(); i++) {
                if (i < 8) {
                    // 槽 0~3 左列 (43,10+21r)，槽 4~7 右列 (115,10+21r)
                    int col = i / 4;
                    int row = i % 4;
                    slots.add(JeiView.Slot.item(col == 0 ? 43 : 115, 10 + row * 21, ingredients.get(i)));
                } else {
                    // 第 9 料：中央槽（箭头上方）
                    slots.add(JeiView.Slot.item(79, 84, ingredients.get(i)));
                }
            }
            if (recipe.extra() != null) {
                // 专属额外料（碗/瓶）：同样画在中央槽
                slots.add(JeiView.Slot.item(79, 84, recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), true));
        }
        return views;
    }

    // -------------------- G. 锤米台（两槽）--------------------

    static List<JeiView> pounding(List<RicePoundingTableBlockEntity.PoundingRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (RicePoundingTableBlockEntity.PoundingRecipe recipe : recipes) {
            views.add(twoUnordered(recipe.inputA(), recipe.inputB(), recipe.result()));
        }
        return views;
    }

    private static JeiView twoUnordered(Item a, Item b, net.minecraft.world.item.ItemStack result) {
        return new JeiView(List.of(
                JeiView.Slot.item(30, 26, a),
                JeiView.Slot.item(30, 62, b)), result, true);
    }

    // -------------------- 汲血台标签配方（上槽标签 + 下槽物品）--------------------
    static List<JeiView> bloodTags(List<BloodlettingTableBlockEntity.TaggedRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (BloodlettingTableBlockEntity.TaggedRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.tag(44, 18, recipe.topTag()),
                    JeiView.Slot.item(44, 50, recipe.bottom()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }

    // -------------------- 米片机（原料 + 500mB 水 → 米，水桶注水返空桶）--------------------

    static List<JeiView> riceMaker(List<RiceMakerBlockEntity.RiceRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (RiceMakerBlockEntity.RiceRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.item(30, 26, recipe.input()),
                    JeiView.Slot.fluid(30, 62, Fluids.WATER, recipe.waterCost()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }
}
