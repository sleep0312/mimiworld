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
