package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.function.Consumer;

/**
 * 米米世界配方数据生成器。运行 gradlew runData 后，配方 JSON 生成到
 * src/generated/resources/data/mimiworld/recipes/（该目录已在 build.gradle 中加入资源源集）。
 * 配方里的物品统一用 ModItems.byCode(编号) 表达需求。
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // 烧炼：1 号 米 → 2 号 熟米，经验 0.5，烧炼时间 120 ticks（6 秒）
        // 注：1.20.1 官方映射中类名为 SimpleCookingRecipeBuilder（其他映射常称 CookingRecipeBuilder）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(1)), RecipeCategory.FOOD, ModItems.byCode(2), 0.5F, 120)
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                .save(consumer, MimiWorld.MODID + ":rice_smelting");

        // 烧炼/高炉：17 号 结晶米矿石 → 16 号 结晶米（锭），经验 0.7（照抄原版铁矿石），
        // 熔炉 200t（10s），高炉 100t（5s）。矿石本身还会在被正确工具破坏时另掉 3~7 经验球。
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(17)), RecipeCategory.MISC, ModItems.byCode(16), 0.7F, 200)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.byCode(17)))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(17)), RecipeCategory.MISC, ModItems.byCode(16), 0.7F, 100)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.byCode(17)))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_blasting");

        // 18 号 重结晶米矿石烧炼成 19 号重结晶米（精炼锭），经验 0.7，熔炉 200t / 高炉 100t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(18)), RecipeCategory.MISC, ModItems.byCode(19), 0.7F, 200)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.byCode(18)))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(18)), RecipeCategory.MISC, ModItems.byCode(19), 0.7F, 100)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.byCode(18)))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_blasting");

        // 21 号 可燃米矿石烧炼成 20 号可燃米（强燃料材料），经验 0.7，熔炉 200t / 高炉 100t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(21)), RecipeCategory.MISC, ModItems.byCode(20), 0.7F, 200)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.byCode(21)))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(21)), RecipeCategory.MISC, ModItems.byCode(20), 0.7F, 100)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.byCode(21)))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_blasting");

        // 有序合成米团（ShapedRecipeBuilder 天然不支持无序合成）：
        // 需求形状 ["BBA", "BBA", "AAA"]，B = 1 号米；A = 无（空槽）。
        // ShapedRecipeBuilder 校验要求：除空格外的每个图案字符都必须 define，
        // 因此空槽 A 在代码中用空格字符 ' ' 表达（空格是原版规定的空槽符号）。
        // 实际布局：4 个米在工作台左上 2×2，第三列与第三行为空。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(3))
                .pattern("BB ")
                .pattern("BB ")
                .pattern("   ")
                .define('B', ModItems.byCode(1))
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                // 1.20.1 规则：配方 ID 与输出物品默认名（mimiworld:rice_ball）相同时，
                // save 必须省略显式 ID 参数，否则会抛 IllegalStateException
                .save(consumer);
    }
}
