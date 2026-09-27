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
import com.mimiworld.mimiworld.block.entity.AdvancedRicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * 各机器配方记录 → {@link JeiView} 的映射函数（每族机器仅此一份布局定义）。
 * <p>
 * 坐标口径与 {@link ViewCategory} 完全一致：槽位像素 = (7 + col*18, 7 + row*18)，
 * col/row 是相对输入区左上的槽格坐标。机器真实 GUI 的槽序语义在此逐族复刻：
 * <ul>
 *   <li>MultiInput（搅碎器/碾磨台/淬剑台/汲血台）：inputs 下标 → pos 表给出的槽格，null 跳过；</li>
 *   <li>DualInput（电离/沉淀）：两槽无序（单输入配方只画 A）；</li>
 *   <li>离解台：上槽/下槽纵向有序；</li>
 *   <li>网格工作台（杵界台/凝锭台）：shaped 按裁剪矩阵逐格，shapeless 行优先填满并打无序标记；</li>
 *   <li>煮锅族：NxN 行优先网格 + (N,0) 右上额外槽（null 不画）；铁锅 4 槽为菱形错位；</li>
 *   <li>搅拌器：槽 0~7 为两列四行（col=idx/4,row=idx%4），第 9 料/专属额外料走居中的额外槽；</li>
 *   <li>锤米台：两槽无序；压饭机：最多 9 料 3x3 无序。</li>
 * </ul>
 */
final class JeiLayouts {

    private JeiLayouts() {
    }

    private static final int SLOT = 18;
    private static final int PAD = 7;

    private static int x(int col) {
        return PAD + col * SLOT;
    }

    private static int y(int row) {
        return PAD + row * SLOT;
    }

    // -------------------- A. MultiInputProcessor（搅碎器/碾磨台/淬剑台/汲血台）--------------------

    static List<JeiView> multi(List<MultiInputProcessorBlockEntity.ProcessorRecipe> recipes, int[][] pos) {
        List<JeiView> views = new ArrayList<>();
        for (MultiInputProcessorBlockEntity.ProcessorRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] inputs = recipe.inputs();
            for (int i = 0; i < pos.length && i < inputs.length; i++) {
                if (inputs[i] != null) {
                    slots.add(JeiView.Slot.item(x(pos[i][0]), y(pos[i][1]), inputs[i]));
                }
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    // -------------------- B. DualInputProcessor（电离台/沉淀台，两槽无序）--------------------

    static List<JeiView> dual(List<DualInputProcessorBlockEntity.ProcessorRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (DualInputProcessorBlockEntity.ProcessorRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            slots.add(JeiView.Slot.item(x(0), y(0), recipe.inputA()));
            if (recipe.inputB() != null) {
                slots.add(JeiView.Slot.item(x(1), y(0), recipe.inputB()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), true));
        }
        return views;
    }

    // -------------------- C. 离解台（上槽 + 下槽，纵向有序）--------------------

    static List<JeiView> dissociation(List<DissociationTableBlockEntity.DissociationRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (DissociationTableBlockEntity.DissociationRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.item(x(0), y(0), recipe.top()),
                    JeiView.Slot.item(x(0), y(1), recipe.bottom()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }

    // -------------------- D. 网格工作台（杵界台 2x2 / 凝锭台 5x5）--------------------

    static List<JeiView> gridCrafting(int n, List<GridCraftingTableBlockEntity.GridRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (GridCraftingTableBlockEntity.GridRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            boolean shapeless;
            Item[][] shaped = recipe.jeiShaped();
            if (shaped != null) {
                // 有序：裁剪后的矩阵按行列逐格摆放（可在 NxN 网格内平移）
                shapeless = false;
                for (int r = 0; r < shaped.length; r++) {
                    for (int c = 0; c < shaped[r].length; c++) {
                        if (shaped[r][c] != null) {
                            slots.add(JeiView.Slot.item(x(c), y(r), shaped[r][c]));
                        }
                    }
                }
            } else {
                // 无序：配料行优先填入网格
                shapeless = true;
                List<Item> ingredients = recipe.jeiShapeless();
                for (int i = 0; i < ingredients.size(); i++) {
                    slots.add(JeiView.Slot.item(x(i % n), y(i / n), ingredients.get(i)));
                }
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), shapeless));
        }
        return views;
    }

    // -------------------- E. 煮锅族（NxN 行优先 + 右上额外槽，需燃料）--------------------

    static List<JeiView> pot(int n, List<GridCookingPotBlockEntity.PotRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (GridCookingPotBlockEntity.PotRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] grid = recipe.grid();
            for (int i = 0; i < grid.length && i < n * n; i++) {
                if (grid[i] != null) {
                    slots.add(JeiView.Slot.item(x(i % n), y(i / n), grid[i]));
                }
            }
            if (recipe.extra() != null) {
                slots.add(JeiView.Slot.item(x(n), y(0), recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    /**
     * 铁锅：grid 仅 4 槽且 GUI 摆成菱形错位——
     * 槽0 左上(0,0)、槽1 右上(3,0)、槽2 左下(1,1)、槽3 右下(2,1)，额外槽在 (4,0)。
     */
    static List<JeiView> ironWok(List<GridCookingPotBlockEntity.PotRecipe> recipes) {
        int[][] pos = {{0, 0}, {3, 0}, {1, 1}, {2, 1}};
        List<JeiView> views = new ArrayList<>();
        for (GridCookingPotBlockEntity.PotRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            Item[] grid = recipe.grid();
            for (int i = 0; i < grid.length && i < pos.length; i++) {
                if (grid[i] != null) {
                    slots.add(JeiView.Slot.item(x(pos[i][0]), y(pos[i][1]), grid[i]));
                }
            }
            if (recipe.extra() != null) {
                slots.add(JeiView.Slot.item(x(4), y(0), recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), false));
        }
        return views;
    }

    // -------------------- F. 搅拌器（两列四行 + 居中额外槽，无序）--------------------

    static List<JeiView> mixer(List<MixerBlockEntity.MixerRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (MixerBlockEntity.MixerRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            List<Item> ingredients = recipe.ingredients();
            for (int i = 0; i < ingredients.size(); i++) {
                if (i < 8) {
                    // 槽 0~7：col=idx/4（左右两列），row=idx%4（上到下）
                    slots.add(JeiView.Slot.item(x(i / 4), y(i % 4), ingredients.get(i)));
                } else {
                    // 第 9 料：extra=null 时槽 8 当普通槽，位于四行区域右侧居中
                    slots.add(JeiView.Slot.item(x(2), PAD + (4 * SLOT - SLOT) / 2, ingredients.get(i)));
                }
            }
            if (recipe.extra() != null) {
                // 专属额外料（碗/瓶）：同样画在右侧居中的槽 8
                slots.add(JeiView.Slot.item(x(2), PAD + (4 * SLOT - SLOT) / 2, recipe.extra()));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), true));
        }
        return views;
    }

    // -------------------- G. 锤米台 / 高级锤米台（两槽无序）--------------------

    static List<JeiView> pounding(List<RicePoundingTableBlockEntity.PoundingRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (RicePoundingTableBlockEntity.PoundingRecipe recipe : recipes) {
            views.add(twoUnordered(recipe.inputA(), recipe.inputB(), recipe.result()));
        }
        return views;
    }

    static List<JeiView> advancedPounding(List<AdvancedRicePoundingTableBlockEntity.PoundingRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (AdvancedRicePoundingTableBlockEntity.PoundingRecipe recipe : recipes) {
            views.add(twoUnordered(recipe.inputA(), recipe.inputB(), recipe.result()));
        }
        return views;
    }

    private static JeiView twoUnordered(Item a, Item b, net.minecraft.world.item.ItemStack result) {
        return new JeiView(List.of(
                JeiView.Slot.item(x(0), y(0), a),
                JeiView.Slot.item(x(1), y(0), b)), result, true);
    }

    // -------------------- H. 压饭机（最多 9 料，3x3 无序多重集）--------------------

    static List<JeiView> press(List<RicePressBlockEntity.PressRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (RicePressBlockEntity.PressRecipe recipe : recipes) {
            List<JeiView.Slot> slots = new ArrayList<>();
            List<Item> inputs = recipe.inputs();
            for (int i = 0; i < inputs.size(); i++) {
                slots.add(JeiView.Slot.item(x(i % 3), y(i / 3), inputs.get(i)));
            }
            views.add(new JeiView(List.copyOf(slots), recipe.result(), true));
        }
        return views;
    }

    // -------------------- 汲血台标签配方（上槽标签 + 下槽物品，纵向有序）--------------------
    static List<JeiView> bloodTags(List<BloodlettingTableBlockEntity.TaggedRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (BloodlettingTableBlockEntity.TaggedRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.tag(x(0), y(0), recipe.topTag()),
                    JeiView.Slot.item(x(0), y(1), recipe.bottom()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }

    // -------------------- 米片机（原料 + 500mB 水 → 米，水桶注水返空桶）--------------------

    static List<JeiView> riceMaker(List<RiceMakerBlockEntity.RiceRecipe> recipes) {
        List<JeiView> views = new ArrayList<>();
        for (RiceMakerBlockEntity.RiceRecipe recipe : recipes) {
            List<JeiView.Slot> slots = List.of(
                    JeiView.Slot.item(x(0), y(0), recipe.input()),
                    JeiView.Slot.fluid(x(1), y(0), Fluids.WATER, recipe.waterCost()));
            views.add(new JeiView(slots, recipe.result(), false));
        }
        return views;
    }
}
