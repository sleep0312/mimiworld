package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.tags.ModItemTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * 米米世界配方数据生成器。运行 gradlew runData 后，配方 JSON 生成到
 * src/generated/resources/data/mimiworld/recipes/（该目录已在 build.gradle 中加入资源源集）。
 * 配方里的本模物品统一用 ModItems 的具名常量引用（如 ModItems.RICE）。
 *
 * ============================================================================
 * 【配方书写约定（用户约定，必须严格遵守）】
 * ----------------------------------------------------------------------------
 * 1. 工作台配方一律为【有序合成（crafting_shaped）】：用 pattern 给出最多 3 行字符网格
 * （空格字符代表空槽），再用 define 把字符绑定到物品常量或标签，产物直接给物品常量。
 * 有序图案经原版去空行空列归一化，整体平移到网格角落不影响匹配。
 * 例外（原版硬约束，runData 实测）：当图案归一化后【只占 1×1 一格】（如单个物品转 N 个）时，
 * ShapedRecipeBuilder 会抛 "only takes in a single item" 异常，必须改用 ShapelessRecipeBuilder；
 * 单材料但占多格（如 3 格 L 形、四角、3×3 填满）仍可正常有序注册。
 * 2. “原版XX” = 原版物品（如 原版水桶=Items.WATER_BUCKET、原版红石=Items.REDSTONE）。
 * “任意XX”类同名物品直接用基物品匹配即可（如任意药水 = Items.POTION，不区分药水效果）；
 * 若某个配方确实需要按药水效果区分 NBT，可用 Forge 的 StrictNBTIngredient.of(...) 精确匹配
 * （Ingredient.of(ItemStack) 会丢 NBT，任意药水都满足）。
 * 工作台消耗药水后原版会按合成剩余物返还玻璃瓶，同理水桶返还空桶。
 * 3. “任意树苗/任意木板”等“任意XX” = 原版同名物品标签（ItemTags.SAPLINGS / PLANKS）。
 * “任意肉类”= 本模自建标签 mimiworld:meats（ModItemTags.MEATS，生/熟/鱼/腐肉均含），
 * 内容由 ModItemTagsProvider 生成；因 Forge 1.20.1（47.4.10）Tags.Items 尚无肉类标签。
 * 4. 熔炉烧炼用 SimpleCookingRecipeBuilder.smelting/blasting，时间单位 tick，1s = 20 tick
 * （1s=20t、3s=60t、4s=80t）；高炉若保留同名烧炼，时间按惯例为熔炉一半。
 * 5. 工作台之外的工作方块（锤米台等）不是数据包配方，不能写在本类；
 * 须在对应方块实体类的静态配方表注册，统一入口见 ModMachineRecipes。
 * 6. 配方 ID：默认等于产物注册名；只有“同一产物存在多条配方”时才必须显式指定不同 ID。
 * 【产物是原版物品时例外】：默认 ID 会落到 minecraft 命名空间（无效/冲突），
 * 必须显式 save(consumer, MimiWorld.MODID + ":产物名")（如原版龙蛋 mimiworld:dragon_egg）。
 * ============================================================================
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // ================= 熔炉烧炼 =================

        // 米 → 熟米，用户指定烧制 1s（20t），经验 0.5
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE.get()), RecipeCategory.FOOD, ModItems.COOKED_RICE.get(), 0.5F, 20)
                .unlockedBy("has_rice", has(ModItems.RICE.get()))
                .save(consumer, MimiWorld.MODID + ":rice_smelting");

        // 米球 → 熟米球，用户指定烧制 4s（80t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_BALL.get()), RecipeCategory.FOOD, ModItems.COOKED_RICE_BALL.get(), 0.3F, 80)
                .unlockedBy("has_rice_ball", has(ModItems.RICE_BALL.get()))
                .save(consumer);

        // 结晶米矿石 → 结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.CRYSTALLIZED_RICE_ORE.get()), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE.get(), 0.7F, 60)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.CRYSTALLIZED_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.CRYSTALLIZED_RICE_ORE.get()), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE.get(), 0.7F, 30)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.CRYSTALLIZED_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_blasting");

        // 重结晶米矿石 → 重结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RECRYSTALLIZED_RICE_ORE.get()), RecipeCategory.MISC, ModItems.RECRYSTALLIZED_RICE.get(), 0.7F, 60)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.RECRYSTALLIZED_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RECRYSTALLIZED_RICE_ORE.get()), RecipeCategory.MISC, ModItems.RECRYSTALLIZED_RICE.get(), 0.7F, 30)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.RECRYSTALLIZED_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_blasting");

        // 可燃米矿石烧炼成可燃米（强燃料材料），经验 0.7，熔炉 200t / 高炉 100t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COMBUSTIBLE_RICE_ORE.get()), RecipeCategory.MISC, ModItems.COMBUSTIBLE_RICE.get(), 0.7F, 200)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.COMBUSTIBLE_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.COMBUSTIBLE_RICE_ORE.get()), RecipeCategory.MISC, ModItems.COMBUSTIBLE_RICE.get(), 0.7F, 100)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.COMBUSTIBLE_RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_blasting");

        // ================= 生鲜食材熔炉烧炼（统一 200t=10s，经验 0.35）=================
        // 熟界天胎花的生版物品不存在，经用户确认用界天脑花烧制熟界天胎花。
        Item[][] smeltPairs = {
                {ModItems.RAW_MIMOO_BEEF.get(), ModItems.COOKED_MIMOO_BEEF.get()},
                {ModItems.RAW_MIMI_MUTTON.get(), ModItems.COOKED_MIMI_MUTTON.get()},
                {ModItems.RAW_MIHENG_PORK.get(), ModItems.COOKED_MIHENG_PORK.get()},
                {ModItems.RAW_TANMI_FISH.get(), ModItems.COOKED_TANMI_FISH.get()},
                {ModItems.REALM_HEAVEN_BRAIN_FLOWER.get(), ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER.get()},
                {ModItems.VOID_TOUCH_TENTACLE.get(), ModItems.COOKED_VOID_TOUCH_TENTACLE.get()},
                {ModItems.THORN_PETAL.get(), ModItems.COOKED_THORN_PETAL.get()},
                {ModItems.RICE_PHOENIX_MEAT_CHUNK.get(), ModItems.COOKED_RICE_PHOENIX_MEAT_CHUNK.get()},
                {ModItems.SPECIALTY_FALLEN_RICE.get(), ModItems.COOKED_SPECIALTY_FALLEN_RICE.get()},
                {ModItems.PALE_MERMAID_TAIL.get(), ModItems.COOKED_PALE_MERMAID_TAIL.get()},
                {ModItems.WANGMI_WOUNDED_MERMAID_LEG.get(), ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG.get()},
                {ModItems.HALF_MIRAGE_MEAT_SLICE.get(), ModItems.COOKED_HALF_MIRAGE_MEAT_SLICE.get()},
                {ModItems.DEVOURER_VULTURE_WING.get(), ModItems.COOKED_DEVOURER_VULTURE_WING.get()}
        };
        for (Item[] pair : smeltPairs) {
            Item raw = pair[0];
            Item cooked = pair[1];
            SimpleCookingRecipeBuilder
                    .smelting(Ingredient.of(raw), RecipeCategory.FOOD,
                            cooked, 0.35F, 200)
                    .unlockedBy("has_" + ForgeRegistries.ITEMS.getKey(raw).getPath(), has(raw))
                    .save(consumer);
        }

        // ================= 工作台有序合成 =================

        // （4 个米在右下 2×2 → 米球）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BALL.get())
                .pattern("   ")
                .pattern(" RR")
                .pattern(" RR")
                .define('R', ModItems.RICE.get())
                .unlockedBy("has_rice", has(ModItems.RICE.get()))
                .save(consumer);

        // （两个 米木板竖排 → 米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_STICK.get())
                .pattern("   ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS.get()))
                .save(consumer);

        // （6 个米木板摆阶梯形 → 4 个米楼梯，同原版楼梯配方）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_STAIRS.get(), 4)
                .pattern("  P")
                .pattern(" PP")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS.get()))
                .save(consumer);

        // （3 个米木板横排 → 6 个米台阶，同原版台阶配方）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_SLAB.get(), 6)
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS.get()))
                .save(consumer);

        // （4 个米木板 + 2 个米木棍 → 3 个米栅栏，同原版栅栏配方，棍用本模米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.RICE_FENCE.get(), 3)
                .pattern("PSP")
                .pattern("PSP")
                .define('P', ModItems.RICE_PLANKS.get())
                .define('S', ModItems.RICE_STICK.get())
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS.get()))
                .save(consumer);

        // （8 个米块围桶 → 米桶；桶按原版机制返还空桶）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_BUCKET.get())
                .pattern("BBB")
                .pattern("BWB")
                .pattern("BBB")
                .define('B', ModItems.RICE_BLOCK.get())
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_block", has(ModItems.RICE_BLOCK.get()))
                .save(consumer);

        // （米 + 任意树苗 → 米树苗）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.RICE_SAPLING.get())
                .pattern("   ")
                .pattern(" RS")
                .pattern("   ")
                .define('R', ModItems.RICE.get())
                .define('S', ItemTags.SAPLINGS)
                .unlockedBy("has_rice", has(ModItems.RICE.get()))
                .save(consumer);

        // （米木板-米木棍-米木板三层 → 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_RICE_STICK.get())
                .pattern("PPP")
                .pattern("QQQ")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS.get())
                .define('Q', ModItems.RICE_STICK.get())
                .unlockedBy("has_rice_stick", has(ModItems.RICE_STICK.get()))
                .save(consumer);

        // （粗米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.COARSE_RICE_BLOCK.get())
                .pattern("WMW")
                .pattern("BWC")
                .pattern("WPW")
                .define('W', Items.WHEAT)
                .define('M', Items.MUTTON)
                .define('B', Items.BEEF)
                .define('C', Items.CHICKEN)
                .define('P', Items.PORKCHOP)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(consumer);

        // （造米机）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_MAKER.get())
                .pattern("LSL")
                .pattern("RLR")
                .pattern("PPP")
                .define('L', Items.LEATHER)
                .define('S', Items.STRING)
                .define('R', Items.PAPER)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(consumer);

        // （米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_BLOCK.get())
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .define('R', ModItems.RICE.get())
                .unlockedBy("has_rice", has(ModItems.RICE.get()))
                .save(consumer);

        // （米原木夹饭晶 → 锤米台）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_POUNDING_TABLE.get())
                .pattern("LLL")
                .pattern("RHR")
                .pattern("LLL")
                .define('L', ModItems.RICE_LOG.get())
                .define('R', Items.REDSTONE)
                .define('H', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （5 个饭桶与 4 个钻石交错 → 饭晶；原「粗制饭晶 → 熔炉精炼」两步已合并为一步）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .pattern("ANA")
                .pattern("NAN")
                .pattern("ANA")
                .define('A', ModItems.RICE_PAIL.get())
                .define('N', Items.DIAMOND)
                .unlockedBy("has_rice_pail", has(ModItems.RICE_PAIL.get()))
                .save(consumer);

        // ================= 第二批（草麻/网/机器/米果线） =================

        // ---------- 熔炉烧炼（1s=20t，时长严格按用户秒数） ----------

        // 米矿石 熔炉 2s（40t）→ 米石（矿石，经验 0.7；高炉 20t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_ORE.get()), RecipeCategory.MISC, ModItems.RICE_STONE.get(), 0.7F, 40)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE.get()))
                .save(consumer);
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RICE_ORE.get()), RecipeCategory.MISC, ModItems.RICE_STONE.get(), 0.7F, 20)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":rice_stone_blasting");

        // ---------- 工作台有序合成 ----------

        // （饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .pattern("NAN")
                .pattern("AGA")
                .pattern("NAN")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('N', Items.NETHERITE_SCRAP)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （3 个米草 L 形 → 米麻；低阶装备通用纤维）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REFINED_RICE_FIBER.get())
                .pattern("   ")
                .pattern(" GG")
                .pattern(" G ")
                .define('G', ModItems.RICE_GRASS.get())
                .unlockedBy("has_rice_grass", has(ModItems.RICE_GRASS.get()))
                .save(consumer);

        // （4 个米麻四角 → 米网；高阶装备编织材料）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REFINED_REINFORCED_RICE_NET.get())
                .pattern("N N")
                .pattern("   ")
                .pattern("N N")
                .define('N', ModItems.REFINED_RICE_FIBER.get())
                .unlockedBy("has_refined_rice_fiber", has(ModItems.REFINED_RICE_FIBER.get()))
                .save(consumer);

        // （超重结晶；原料中的超结晶块已改为饭晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL.get())
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('D', Items.DIAMOND)
                .define('B', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （沉淀台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.PRECIPITATION_TABLE.get())
                .pattern("NNN")
                .pattern(" G ")
                .pattern("TTT")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('G', Items.GOLD_INGOT)
                .define('T', ModItems.RICE_STONE_BLOCK.get())
                .unlockedBy("has_refined_reinforced_rice_net", has(ModItems.REFINED_REINFORCED_RICE_NET.get()))
                .save(consumer);

        // （复合米饭锭；原「粗制锭 → 烧板 → 围板成锭」三连已合并为一步）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .pattern("AXA")
                .pattern("XHX")
                .pattern("AXA")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('X', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('H', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （米果渣无合成配方：改为每食用 1 个米果直接获得 1 个米果渣，见 ModItems.RICE_FRUIT 的 finishUsingItem）

        // ================= 第三批（装备/清洁/超懒合金/可燃线） =================

        // ---------- 熔炉烧炼（均 2s=40t，经验 0.3） ----------

        // 可燃混合液 熔炉 2s（40t）→ 可燃固态物
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COMBUSTIBLE_MIXTURE.get()), RecipeCategory.MISC, ModItems.COMBUSTIBLE_SOLID.get(), 0.3F, 40)
                .unlockedBy("has_combustible_mixture", has(ModItems.COMBUSTIBLE_MIXTURE.get()))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // （饭晶弓；A=饭晶 F=粗制米麻 S=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_BOW.get())
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.RICE_STICK.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （配方一）米书：米果清洁液 + 3 张米纸
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BOOK.get())
                .pattern("   ")
                .pattern(" L ")
                .pattern("PPP")
                .define('L', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('P', ModItems.RICE_PAPER.get())
                .unlockedBy("has_rice_paper", has(ModItems.RICE_PAPER.get()))
                .save(consumer);

        // （修订版）3 个米果渣横排 → 米纸
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_PAPER.get())
                .pattern("   ")
                .pattern("FFF")
                .pattern("   ")
                .define('F', ModItems.RICE_POMACE.get())
                .unlockedBy("has_rice_pomace", has(ModItems.RICE_POMACE.get()))
                .save(consumer);

        // （米饭混合液；饭桶+水桶+米桶，空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_MEAL_MIXTURE.get())
                .pattern("   ")
                .pattern("PWB")
                .pattern("   ")
                .define('P', ModItems.RICE_PAIL.get())
                .define('W', Items.WATER_BUCKET)
                .define('B', ModItems.RICE_BUCKET.get())
                .unlockedBy("has_rice_pail", has(ModItems.RICE_PAIL.get()))
                .save(consumer);

        // （清洁复合锭；原「粗制锭 → 碎块 → 板 → 成锭」四连已合并为一步）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_INGOT.get())
                .pattern("ACA")
                .pattern("CNC")
                .pattern("ACA")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('C', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （第一排 3 个超懒精华 + 第二排 清洁复合锭/米果清洁液/清洁复合锭 + 第三排 3 个超懒精华 → 粗制超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUPER_LAZY_ALLOY_INGOT.get())
                .pattern("SSS")
                .pattern("ACA")
                .pattern("SSS")
                .define('S', ModItems.SUPER_LAZY_ESSENCE.get())
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('C', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_super_lazy_essence", has(ModItems.SUPER_LAZY_ESSENCE.get()))
                .save(consumer);

        // （米石块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_STONE_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.RICE_STONE.get())
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE.get()))
                .save(consumer);

        // （8 个可燃米围米饭混合液 → 可燃混合液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMBUSTIBLE_MIXTURE.get())
                .pattern("FFF")
                .pattern("FMF")
                .pattern("FFF")
                .define('F', ModItems.COMBUSTIBLE_RICE.get())
                .define('M', ModItems.RICE_MEAL_MIXTURE.get())
                .unlockedBy("has_combustible_rice", has(ModItems.COMBUSTIBLE_RICE.get()))
                .save(consumer);

        // （清洁米沉离液；A=米果清洁液 C=可燃米，不再需要熔炼可燃固态物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .pattern("   ")
                .pattern(" AC")
                .pattern("   ")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('C', ModItems.COMBUSTIBLE_RICE.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （饭晶剑）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_SWORD.get())
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.RICE_STICK.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （饭晶合金剑；A=饭晶合金锭 F=S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_SWORD.get())
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_BOW.get())
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金头盔；A=饭晶合金锭 M=米麻 T=猴王毫毛门票 D=下界合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_HELMET.get())
                .pattern("ADA")
                .pattern("ATA")
                .pattern(" M ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .define('D', Items.NETHERITE_HELMET)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金胸甲；A=饭晶合金锭 M=米麻 T=猴王毫毛门票 D=下界合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_CHESTPLATE.get())
                .pattern("M M")
                .pattern("ATA")
                .pattern("ADA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .define('D', Items.NETHERITE_CHESTPLATE)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金护腿；A=饭晶合金锭 M=米麻 T=猴王毫毛门票 D=下界合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_LEGGINGS.get())
                .pattern("ADA")
                .pattern("ATA")
                .pattern("M M")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .define('D', Items.NETHERITE_LEGGINGS)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金靴子；A=饭晶合金锭 M=米麻 T=猴王毫毛门票 D=下界合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_BOOTS.get())
                .pattern(" D ")
                .pattern("ATA")
                .pattern("AMA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .define('D', Items.NETHERITE_BOOTS)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （超重结晶剑；F=粗制米网 H=超重结晶 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_SWORD.get())
                .pattern(" FH")
                .pattern("SHF")
                .pattern("SS ")
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOW.get())
                .pattern("FHS")
                .pattern("HSF")
                .pattern("FHS")
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // ================= 第四批（锻造三锭/胚/套装） =================

        // （超重结晶头盔；H=超重结晶 M=米麻 T=虎王利爪门票 D=饭晶合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_HELMET.get())
                .pattern("HTH")
                .pattern("HDH")
                .pattern(" M ")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .define('D', ModItems.RICE_CRYSTAL_ALLOY_HELMET.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶胸甲；H=超重结晶 M=米麻 T=虎王利爪门票 D=饭晶合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_CHESTPLATE.get())
                .pattern("HHH")
                .pattern("HTH")
                .pattern("MDM")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .define('D', ModItems.RICE_CRYSTAL_ALLOY_CHESTPLATE.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶护腿；M=米麻 T=虎王利爪门票 D=饭晶合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_LEGGINGS.get())
                .pattern("MDM")
                .pattern("HTH")
                .pattern("HHH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .define('D', ModItems.RICE_CRYSTAL_ALLOY_LEGGINGS.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶靴子；T=虎王利爪门票 D=饭晶合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOOTS.get())
                .pattern(" T ")
                .pattern("HDH")
                .pattern("HMH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .define('D', ModItems.RICE_CRYSTAL_ALLOY_BOOTS.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （复合米饭剑；F=P=复合米饭锭 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_SWORD.get())
                .pattern(" FP")
                .pattern("SPF")
                .pattern("SS ")
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOW.get())
                .pattern("FPS")
                .pattern("PSF")
                .pattern("FPS")
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭头盔；M=米麻 T=牛王晶角门票 D=超重结晶头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_HELMET.get())
                .pattern("PDP")
                .pattern("PMP")
                .pattern(" T ")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.OX_KING_CRYSTAL_HORN.get())
                .define('D', ModItems.SUPER_HEAVY_CRYSTAL_HELMET.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭胸甲；M=米麻 T=牛王晶角门票 D=超重结晶胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_CHESTPLATE.get())
                .pattern("PPP")
                .pattern("PTP")
                .pattern("MDM")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.OX_KING_CRYSTAL_HORN.get())
                .define('D', ModItems.SUPER_HEAVY_CRYSTAL_CHESTPLATE.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭护腿；M=米麻 T=牛王晶角门票 D=超重结晶护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_LEGGINGS.get())
                .pattern("MDM")
                .pattern("PTP")
                .pattern("PPP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.OX_KING_CRYSTAL_HORN.get())
                .define('D', ModItems.SUPER_HEAVY_CRYSTAL_LEGGINGS.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭靴子；T=牛王晶角门票 D=超重结晶靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOOTS.get())
                .pattern(" T ")
                .pattern("PMP")
                .pattern("PDP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.OX_KING_CRYSTAL_HORN.get())
                .define('D', ModItems.SUPER_HEAVY_CRYSTAL_BOOTS.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （清洁复合剑；B=清洁复合锭 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_SWORD.get())
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" S ")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合弓；B=清洁复合锭 N=精制强化米网 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOW.get())
                .pattern("BNS")
                .pattern("NSB")
                .pattern("BNS")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合头盔；B=清洁复合锭 M=米麻 T=兔王血红之眼门票 D=复合米饭头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_HELMET.get())
                .pattern("BDB")
                .pattern("BMB")
                .pattern(" T ")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .define('D', ModItems.COMPOUND_RICE_MEAL_HELMET.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合胸甲；M=米麻 T=兔王血红之眼门票 D=复合米饭胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_CHESTPLATE.get())
                .pattern("BBB")
                .pattern("BTB")
                .pattern("MDM")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .define('D', ModItems.COMPOUND_RICE_MEAL_CHESTPLATE.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合护腿；M=米麻 T=兔王血红之眼门票 D=复合米饭护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_LEGGINGS.get())
                .pattern("MDM")
                .pattern("BTB")
                .pattern("BBB")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .define('D', ModItems.COMPOUND_RICE_MEAL_LEGGINGS.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合靴子；M=米麻 T=兔王血红之眼门票 D=复合米饭靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOOTS.get())
                .pattern("BTB")
                .pattern("BMB")
                .pattern("BDB")
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .define('T', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .define('D', ModItems.COMPOUND_RICE_MEAL_BOOTS.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （超懒合金剑；B=超懒合金锭 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_SWORD.get())
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" S ")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （超懒合金弓；B=超懒合金锭 N=精制强化米网 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOW.get())
                .pattern("BNS")
                .pattern("NSB")
                .pattern("BNS")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （超懒合金头盔；B=超懒合金锭 N=精制强化米网 T=毒蟒之牙门票 D=清洁复合头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_HELMET.get())
                .pattern("BDB")
                .pattern("BNB")
                .pattern(" T ")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .define('D', ModItems.CLEANSING_COMPOUND_HELMET.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （超懒合金胸甲；B=超懒合金锭 N=精制强化米网 T=毒蟒之牙门票 D=清洁复合胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_CHESTPLATE.get())
                .pattern("BBB")
                .pattern("BTB")
                .pattern("NDN")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .define('D', ModItems.CLEANSING_COMPOUND_CHESTPLATE.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // 超懒合金护腿：精制强化米网×2 点缀；T=毒蟒之牙门票 D=清洁复合护腿
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_LEGGINGS.get())
                .pattern("NDN")
                .pattern("BTB")
                .pattern("BBB")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .define('D', ModItems.CLEANSING_COMPOUND_LEGGINGS.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // ================= 第五批（靴子/铸造台/召唤器链/Boss 生物蛋/神翼） =================

        // （超懒合金靴子；B=超懒合金锭 N=精制强化米网 T=毒蟒之牙门票 D=清洁复合靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOOTS.get())
                .pattern("BTB")
                .pattern("BNB")
                .pattern("BDB")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .define('D', ModItems.CLEANSING_COMPOUND_BOOTS.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （米米之眼；一圈米 + 中间末影之眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_EYE.get())
                .pattern("RRR")
                .pattern("RER")
                .pattern("RRR")
                .define('R', ModItems.RICE.get())
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        // （米米传送器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_TELEPORTER.get())
                .pattern("RSR")
                .pattern("SDS")
                .pattern("RSR")
                .define('R', ModItems.RICE.get())
                .define('S', Items.END_STONE)
                .define('D', Items.REINFORCED_DEEPSLATE)
                .unlockedBy("has_end_stone", has(Items.END_STONE))
                .save(consumer);

        // （小型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMALL_SUMMONER.get())
                .pattern("RDR")
                .pattern("DRD")
                .pattern("RGR")
                .define('R', ModItems.RICE.get())
                .define('D', Items.REDSTONE)
                .define('G', Items.GLOWSTONE_DUST)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);

        // （中型召唤器）
        // 注意：用户原文行尾产物写成了占位物，但占位物不开放获取，本行产物才是中型召唤器，
        // 且配方含召唤核心（对应中级），故按行首条目以中型召唤器注册
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEDIUM_SUMMONER.get())
                .pattern("LBL")
                .pattern("BLB")
                .pattern("LCL")
                .define('L', ModItems.RICE_LOG.get())
                .define('B', Items.REDSTONE_BLOCK)
                .define('C', ModItems.SUMMON_CORE.get())
                .unlockedBy("has_summon_core", has(ModItems.SUMMON_CORE.get()))
                .save(consumer);

        // （大型召唤器；粗米块 米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LARGE_SUMMONER.get())
                .pattern("ADA")
                .pattern("SRS")
                .pattern("ACA")
                .define('A', ModItems.COARSE_RICE_BLOCK.get())
                .define('D', Items.REDSTONE)
                .define('S', Items.SLIME_BALL)
                .define('R', ModItems.RICE_BLOCK.get())
                .define('C', ModItems.ADVANCED_SUMMON_CORE.get())
                .unlockedBy("has_advanced_summon_core", has(ModItems.ADVANCED_SUMMON_CORE.get()))
                .save(consumer);

        // （最懒召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_SUMMONER.get())
                .pattern("RBR")
                .pattern("KRK")
                .pattern("RCR")
                .define('R', ModItems.RICE_BLOCK.get())
                .define('B', Items.REDSTONE_BLOCK)
                .define('K', Items.SLIME_BLOCK)
                .define('C', ModItems.LAZIEST_SUMMON_CORE.get())
                .unlockedBy("has_laziest_summon_core", has(ModItems.LAZIEST_SUMMON_CORE.get()))
                .save(consumer);

        // 召唤核心/高级召唤核心不再有胚件与熔炉加工步骤，改由锤米台直接产出

        // （混合结晶；棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIXED_CRYSTAL.get())
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.CRYSTALLIZED_RICE.get())
                .define('B', ModItems.RECRYSTALLIZED_RICE.get())
                .unlockedBy("has_crystallized_rice", has(ModItems.CRYSTALLIZED_RICE.get()))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 熔融混合结晶
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.MIXED_CRYSTAL.get()), RecipeCategory.MISC,
                        ModItems.MOLTEN_MIXED_CRYSTAL.get(), 0.3F, 40)
                .unlockedBy("has_mixed_crystal", has(ModItems.MIXED_CRYSTAL.get()))
                .save(consumer);

        // （米米猴王生物蛋；四角下界合金锭，中心小型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MONKEY_KING_SPAWN_EGG.get())
                .pattern("SAS")
                .pattern("AEA")
                .pattern("SAS")
                .define('S', Items.NETHERITE_INGOT)
                .define('A', ModItems.RICE_FRUIT_BLACK_MIXTURE.get())
                .define('E', ModItems.SMALL_SUMMONER.get())
                .unlockedBy("has_small_summoner", has(ModItems.SMALL_SUMMONER.get()))
                .save(consumer);

        // （饭晶虎王生物蛋；四角饭晶合金锭，米果混黏液围边，中心小型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_TIGER_KING_SPAWN_EGG.get())
                .pattern("IVI")
                .pattern("VTV")
                .pattern("IVI")
                .define('I', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('V', ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get())
                .define('T', ModItems.SMALL_SUMMONER.get())
                .unlockedBy("has_small_summoner", has(ModItems.SMALL_SUMMONER.get()))
                .save(consumer);

        // （超晶牛王生物蛋；四角超重结晶 爆米绽放液围边，中心中型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_COW_KING_SPAWN_EGG.get())
                .pattern("RLR")
                .pattern("LCL")
                .pattern("RLR")
                .define('R', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('L', ModItems.POPRICE_BLOOM_LIQUID.get())
                .define('C', ModItems.MEDIUM_SUMMONER.get())
                .unlockedBy("has_medium_summoner", has(ModItems.MEDIUM_SUMMONER.get()))
                .save(consumer);

        // （重晶兔王生物蛋；四角复合米饭锭，眼米嘬心液围边，中心中型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG.get())
                .pattern("RHR")
                .pattern("HOH")
                .pattern("RHR")
                .define('R', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('H', ModItems.YANMI_HEART_SIP_LIQUID.get())
                .define('O', ModItems.MEDIUM_SUMMONER.get())
                .unlockedBy("has_medium_summoner", has(ModItems.MEDIUM_SUMMONER.get()))
                .save(consumer);

        // （复晶天青蟒生物蛋；爆米嘬心液围边，中心大型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_AZURE_PYTHON_SPAWN_EGG.get())
                .pattern("RYR")
                .pattern("YEY")
                .pattern("RYR")
                .define('R', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('Y', ModItems.POPRICE_HEART_SIP_LIQUID.get())
                .define('E', ModItems.LARGE_SUMMONER.get())
                .unlockedBy("has_large_summoner", has(ModItems.LARGE_SUMMONER.get()))
                .save(consumer);

        // 清洁长命夔生物蛋：超懒合金锭四角，米米杂烩液围边，中心大型召唤器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_LONGEVITY_KUI_SPAWN_EGG.get())
                .pattern("BMB")
                .pattern("MFM")
                .pattern("BMB")
                .define('B', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('M', ModItems.MIMI_MEDLEY_LIQUID.get())
                .define('F', ModItems.LARGE_SUMMONER.get())
                .unlockedBy("has_large_summoner", has(ModItems.LARGE_SUMMONER.get()))
                .save(consumer);

        // （最懒终末龙生物蛋）：四角天堕锭，浸染液围边，中心最懒召唤器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_FINAL_DRAGON_SPAWN_EGG.get())
                .pattern("RNR")
                .pattern("NMN")
                .pattern("RNR")
                .define('R', ModItems.HEAVENFALL_INGOT.get())
                .define('N', ModItems.DYEING_SOLUTION.get())
                .define('M', ModItems.LAZIEST_SUMMONER.get())
                .unlockedBy("has_heavenfall_ingot", has(ModItems.HEAVENFALL_INGOT.get()))
                .save(consumer);

        // （第八宗罪龙生物蛋）：四角残堕世界精华锭，磨制残堕精华液围边，中心最懒召唤器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIBAZONGZUILONG_SPAWN_EGG.get())
                .pattern("RNR")
                .pattern("NMN")
                .pattern("RNR")
                .define('R', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get())
                .define('N', ModItems.POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION.get())
                .define('M', ModItems.LAZIEST_SUMMONER.get())
                .unlockedBy("has_remnant_fallen_world_essence_ingot", has(ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get()))
                .save(consumer);

        // ================= 八大 Boss 战利品 → 对应生物蛋 =================
        // 每个 Boss 的专属头颅战利品，都可在工作台合回对应的生物蛋（1:1，无序单材料）。
        // 同一产物已有一条有序配方，此处必须显式指定不同的配方 ID（后缀 _from_trophy）。

        // 米米猴头 → 米米猴王生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MIMI_MONKEY_KING_SPAWN_EGG.get(), 1)
                .requires(ModItems.MIMI_MONKEY_HEAD.get())
                .unlockedBy("has_mimi_monkey_head", has(ModItems.MIMI_MONKEY_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":mimi_monkey_king_spawn_egg_from_trophy");

        // 饭晶虎王头 → 饭晶虎王生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_TIGER_KING_SPAWN_EGG.get(), 1)
                .requires(ModItems.RICE_CRYSTAL_TIGER_KING_HEAD.get())
                .unlockedBy("has_rice_crystal_tiger_king_head", has(ModItems.RICE_CRYSTAL_TIGER_KING_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":rice_crystal_tiger_king_spawn_egg_from_trophy");

        // 超晶牛头 → 超晶牛王生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_COW_KING_SPAWN_EGG.get(), 1)
                .requires(ModItems.SUPER_CRYSTAL_OX_HEAD.get())
                .unlockedBy("has_super_crystal_ox_head", has(ModItems.SUPER_CRYSTAL_OX_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":super_crystal_cow_king_spawn_egg_from_trophy");

        // 重晶兔王头 → 重晶兔王生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG.get(), 1)
                .requires(ModItems.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get())
                .unlockedBy("has_heavy_crystal_rabbit_king_head", has(ModItems.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":heavy_crystal_rabbit_king_spawn_egg_from_trophy");

        // 复晶天青蟒头 → 复晶天青蟒生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COMPOUND_AZURE_PYTHON_SPAWN_EGG.get(), 1)
                .requires(ModItems.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get())
                .unlockedBy("has_compound_crystal_azure_python_head", has(ModItems.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":compound_azure_python_spawn_egg_from_trophy");

        // 清洁长命夔头 → 清洁长命夔生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CLEANSING_LONGEVITY_KUI_SPAWN_EGG.get(), 1)
                .requires(ModItems.PURIFIED_LONGEVITY_KUI_HEAD.get())
                .unlockedBy("has_purified_longevity_kui_head", has(ModItems.PURIFIED_LONGEVITY_KUI_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":cleansing_longevity_kui_spawn_egg_from_trophy");

        // 最懒终末龙头 → 最懒终末龙生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LAZIEST_FINAL_DRAGON_SPAWN_EGG.get(), 1)
                .requires(ModItems.LAZIEST_END_DRAGON_HEAD.get())
                .unlockedBy("has_laziest_end_dragon_head", has(ModItems.LAZIEST_END_DRAGON_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":laziest_final_dragon_spawn_egg_from_trophy");

        // 第八宗罪龙头 → 第八宗罪龙生物蛋
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DIBAZONGZUILONG_SPAWN_EGG.get(), 1)
                .requires(ModItems.DIBAZONGZUI_HEAD.get())
                .unlockedBy("has_dibazongzui_head", has(ModItems.DIBAZONGZUI_HEAD.get()))
                .save(consumer, MimiWorld.MODID + ":dibazongzuilong_spawn_egg_from_trophy");

        // （米米神翼；米米幻翼膜）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MIMI_DIVINE_WINGS.get())
                .pattern("AMA")
                .pattern("PEP")
                .pattern("AMA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('M', ModItems.MIMI_MEDLEY_LIQUID.get())
                .define('P', ModItems.MIMI_PHANTOM_MEMBRANE.get())
                .define('E', Items.ELYTRA)
                .unlockedBy("has_mimi_phantom_membrane", has(ModItems.MIMI_PHANTOM_MEMBRANE.get()))
                .save(consumer);

        // （米米幻翼膜；超重结晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_PHANTOM_MEMBRANE.get())
                .pattern("RHR")
                .pattern("SMS")
                .pattern("RHR")
                .define('R', ModItems.RICE.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('S', Items.SCULK)
                .define('M', Items.PHANTOM_MEMBRANE)
                .unlockedBy("has_phantom_membrane", has(Items.PHANTOM_MEMBRANE))
                .save(consumer);

        // （启航之书；七种材料围满外圈，中心书米笔，中下米）
        // 米骨   火米药   眼米珍珠
        // 爆烈米棒 书米笔   雕米骨
        // 米莱马球 米       黏米丝线
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOOK_OF_DEPARTURE.get())
                .pattern("ABC")
                .pattern("DEF")
                .pattern("GRH")
                .define('A', ModItems.RICE_BONE.get())
                .define('B', ModItems.HUOMI_POWDER.get())
                .define('C', ModItems.YANMI_PEARL.get())
                .define('D', ModItems.BAOLIEMI_ROD.get())
                .define('E', ModItems.BOOK_RICE_PEN.get())
                .define('F', ModItems.CARVED_RICE_BONE.get())
                .define('G', ModItems.MILAIMA_BALL.get())
                .define('H', ModItems.NIANMI_SILK_THREAD.get())
                .define('R', ModItems.RICE.get())
                .unlockedBy("has_book_rice_pen", has(ModItems.BOOK_RICE_PEN.get()))
                .save(consumer);

        // ---------- 圣遗物饰品获取配方 ----------
        // 每个 Boss 至少直接掉落 1 件饰品（见 ModLootTableProvider），其余饰品用
        // 「该阶段可合成的对应等级锭 + Boss 专属掉落物」在工作台合成。

        // 如意：饭晶合金锭×4 + 猴王毫毛（猴王档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RUYI.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .unlockedBy("has_monkey_king_hair", has(ModItems.MONKEY_KING_HAIR.get()))
                .save(consumer);

        // 虎王之蔑：超重结晶×4 + 虎王利爪（虎王档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TIGER_KING_DISDAIN.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('T', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .unlockedBy("has_tiger_king_razor_claw", has(ModItems.TIGER_KING_RAZOR_CLAW.get()))
                .save(consumer);

        // 猩红竖眼：清洁复合锭×4 + 兔王腥红眼（兔王档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRIMSON_VERTICAL_EYE.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('T', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .unlockedBy("has_rabbit_king_bloody_red_eye", has(ModItems.RABBIT_KING_BLOODY_RED_EYE.get()))
                .save(consumer);

        // 暗灵毒牙：超懒合金锭×4 + 极毒青蟒牙（天青蟒档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DARK_SPIRIT_FANG.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .unlockedBy("has_venom_azure_python_fang", has(ModItems.VENOM_AZURE_PYTHON_FANG.get()))
                .save(consumer);

        // 天青轮盘：超懒合金锭×8 + 极毒青蟒牙（天青蟒档，环形成本高于暗灵毒牙）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.AZURE_WHEEL.get())
                .pattern("AAA")
                .pattern("ATA")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('T', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .unlockedBy("has_venom_azure_python_fang", has(ModItems.VENOM_AZURE_PYTHON_FANG.get()))
                .save(consumer);

        // 领域展开：天堕锭×4 + 恶夔长命角（长命夔档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DOMAIN_EXPANSION.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // 山海恩泽：天堕锭×4 + 清洁复合锭×4 + 恶夔长命角（长命夔档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SHANHAI_GRACE.get())
                .pattern("BAB")
                .pattern("ATA")
                .pattern("BAB")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // 上古毛皮：复合米饭锭×4 + 牛王晶角（牛王档）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCIENT_PELT.get())
                .pattern(" A ")
                .pattern("ATA")
                .pattern(" A ")
                .define('A', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('T', ModItems.OX_KING_CRYSTAL_HORN.get())
                .unlockedBy("has_ox_king_crystal_horn", has(ModItems.OX_KING_CRYSTAL_HORN.get()))
                .save(consumer);

        // 米米圣盾：前四档 Boss 的专属战利品各一件（猴王毫毛/虎王利爪/牛王晶角/兔王腥红眼），
        // 四角再各配一枚该 Boss 掉落的主材料锭（下界合金锭/饭晶合金锭/超重结晶/复合米饭锭），中心放 1 个米
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MIMI_SHIELD.get())
                .pattern("TNU")
                .pattern("BRA")
                .pattern("CDM")
                .define('T', ModItems.MONKEY_KING_HAIR.get())
                .define('N', Items.NETHERITE_INGOT)
                .define('U', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('B', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('R', ModItems.RICE.get())
                .define('C', ModItems.OX_KING_CRYSTAL_HORN.get())
                .define('D', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .unlockedBy("has_monkey_king_hair", has(ModItems.MONKEY_KING_HAIR.get()))
                .save(consumer);

        // 原版龙蛋：烈焰粉 龙息 烈焰粉 / 龙息 0 龙息 / 烈焰粉 龙息 烈焰粉
        // 产物是原版物品，默认 ID 会落在 minecraft 命名空间，必须显式指定 mimiworld:dragon_egg
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DRAGON_EGG)
                .pattern("BDB")
                .pattern("D D")
                .pattern("BDB")
                .define('B', Items.BLAZE_POWDER)
                .define('D', Items.DRAGON_BREATH)
                .unlockedBy("has_dragon_breath", has(Items.DRAGON_BREATH))
                .save(consumer, MimiWorld.MODID + ":dragon_egg");

        // ================= 第六批（神翼/神剑魄→剑诀/天堕套装/食材） =================

        // （超懒神翼胸甲；A=超懒合金胸甲 W=米米神翼）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_WINGS_CHESTPLATE.get())
                .pattern(" A ")
                .pattern(" W ")
                .pattern("   ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_CHESTPLATE.get())
                .define('W', ModItems.MIMI_DIVINE_WINGS.get())
                .unlockedBy("has_mimi_divine_wings", has(ModItems.MIMI_DIVINE_WINGS.get()))
                .save(consumer);

        // （上古剑意）
        // 判定：mod 无此物品，"紫晶幽匿感应器"=原版校准幽匿感测体 CALIBRATED_SCULK_SENSOR（紫水晶合成的校准版）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCIENT_SWORD_INTENT.get())
                .pattern("HEH")
                .pattern("ACA")
                .pattern("HEH")
                .define('H', Items.DRAGON_HEAD)
                .define('E', Items.END_CRYSTAL)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('C', Items.CALIBRATED_SCULK_SENSOR)
                .unlockedBy("has_dragon_head", has(Items.DRAGON_HEAD))
                .save(consumer);

        // （超懒神剑：S=超懒合金剑 I=上古剑意 L=淬剑液，原淬剑台加工改为原版工作台合成）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_DIVINE_SWORD.get())
                .pattern("SIL")
                .define('S', ModItems.SUPER_LAZY_ALLOY_SWORD.get())
                .define('I', ModItems.ANCIENT_SWORD_INTENT.get())
                .define('L', ModItems.SWORD_QUENCHING_LIQUID.get())
                .unlockedBy("has_ancient_sword_intent", has(ModItems.ANCIENT_SWORD_INTENT.get()))
                .save(consumer);

        // （血瓶：任意肉类 + 玻璃瓶，原汲血台配方并入原版工作台）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLOOD_VIAL.get())
                .requires(ModItemTags.MEATS)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_meat", has(ModItemTags.MEATS))
                .save(consumer);

        // （淬剑液；9=米桶 B=血瓶 L=米果清洁液 R=清洁米沉离液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_QUENCHING_LIQUID.get())
                .pattern("VBV")
                .pattern("LBR")
                .pattern("VBV")
                .define('V', ModItems.RICE_BUCKET.get())
                .define('B', ModItems.BLOOD_VIAL.get())
                .define('L', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('R', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .unlockedBy("has_blood_vial", has(ModItems.BLOOD_VIAL.get()))
                .save(consumer);

        // （远古剑魄）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCIENT_SWORD_SOUL.get())
                .pattern("DSD")
                .pattern("SWS")
                .pattern("DSD")
                .define('D', Items.ANCIENT_DEBRIS)
                .define('S', ModItems.COMBUSTIBLE_SOLID.get())
                .define('W', ModItems.ANCIENT_SWORD_INTENT.get())
                .unlockedBy("has_ancient_sword_intent", has(ModItems.ANCIENT_SWORD_INTENT.get()))
                .save(consumer);

        // （太古剑魄）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRIMORDIAL_SWORD_SOUL.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.COMBUSTIBLE_SOLID.get())
                .define('C', ModItems.ANCIENT_SWORD_SOUL.get())
                .unlockedBy("has_ancient_sword_soul", has(ModItems.ANCIENT_SWORD_SOUL.get()))
                .save(consumer);

        // （神玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIVINE_JADE.get())
                .pattern("DDD")
                .pattern("DAD")
                .pattern("DDD")
                .define('D', Items.DRAGON_BREATH)
                .define('A', Items.AMETHYST_SHARD)
                .unlockedBy("has_dragon_breath", has(Items.DRAGON_BREATH))
                .save(consumer);

        // （金米玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_RICE_JADE.get())
                .pattern("SJS")
                .pattern("RGR")
                .pattern("SJS")
                .define('S', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('J', ModItems.DIVINE_JADE.get())
                .define('R', ModItems.RECRYSTALLIZED_RICE.get())
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_divine_jade", has(ModItems.DIVINE_JADE.get()))
                .save(consumer);

        // （神剑鉴玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIVINE_SWORD_APPRAISAL_JADE.get())
                .pattern("SJJ")
                .pattern("JBJ")
                .pattern("JJS")
                .define('S', ModItems.COMBUSTIBLE_SOLID.get())
                .define('J', ModItems.GOLDEN_RICE_JADE.get())
                .define('B', ModItems.PRIMORDIAL_SWORD_SOUL.get())
                .unlockedBy("has_golden_rice_jade", has(ModItems.GOLDEN_RICE_JADE.get()))
                .save(consumer);

        // （剑法禁卷；米纸 米书）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORBIDDEN_SWORD_MANUAL.get())
                .pattern("TPT")
                .pattern("BBB")
                .pattern("TPT")
                .define('T', Items.GHAST_TEAR)
                .define('P', ModItems.RICE_PAPER.get())
                .define('B', ModItems.RICE_BOOK.get())
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(consumer);

        // （超懒剑法）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_SWORD_ART.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("DDD")
                .define('A', ModItems.GOLDEN_RICE_JADE.get())
                .define('B', ModItems.FORBIDDEN_SWORD_MANUAL.get())
                .define('C', ModItems.DIVINE_SWORD_APPRAISAL_JADE.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_forbidden_sword_manual", has(ModItems.FORBIDDEN_SWORD_MANUAL.get()))
                .save(consumer);

        // （结晶米玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE_JADE.get())
                .pattern("SSS")
                .pattern("LJL")
                .pattern("SSS")
                .define('S', ModItems.COMBUSTIBLE_SOLID.get())
                .define('L', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('J', ModItems.GOLDEN_RICE_JADE.get())
                .unlockedBy("has_golden_rice_jade", has(ModItems.GOLDEN_RICE_JADE.get()))
                .save(consumer);

        // （终末剑诀）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SWORD_CODEX.get())
                .pattern("HAA")
                .pattern("CBC")
                .pattern("AAH")
                .define('H', Items.DRAGON_HEAD)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('C', ModItems.CRYSTALLIZED_RICE_JADE.get())
                .define('B', ModItems.SUPER_LAZY_SWORD_ART.get())
                .unlockedBy("has_crystallized_rice_jade", has(ModItems.CRYSTALLIZED_RICE_JADE.get()))
                .save(consumer);

        // （最懒天堕剑·赝品；超懒神剑）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.SUPER_LAZY_DIVINE_SWORD.get())
                .define('C', ModItems.FINAL_SWORD_CODEX.get())
                .unlockedBy("has_final_sword_codex", has(ModItems.FINAL_SWORD_CODEX.get()))
                .save(consumer);

        // （破败之心）
        // 判定：mod 无此物品，"幽匿感应器"=原版幽匿感测体 SCULK_SENSOR；饭晶合金锭
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RUINED_HEART.get())
                .pattern("ENE")
                .pattern("DSD")
                .pattern("ENE")
                .define('E', Items.ENDER_EYE)
                .define('N', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('D', Items.DRAGON_BREATH)
                .define('S', Items.SCULK_SENSOR)
                .unlockedBy("has_sculk_sensor", has(Items.SCULK_SENSOR))
                .save(consumer);

        // （堕落之心，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_HEART.get())
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .define('B', ModItems.RUINED_HEART.get())
                .unlockedBy("has_ruined_heart", has(ModItems.RUINED_HEART.get()))
                .save(consumer);

        // （天堕锭：中间 1 破败之心，周围一圈 8 个残堕锭 → 1 天堕锭，最懒天堕套主合成锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAVENFALL_INGOT.get())
                .pattern("RRR")
                .pattern("RUR")
                .pattern("RRR")
                .define('R', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('U', ModItems.RUINED_HEART.get())
                .unlockedBy("has_ruined_heart", has(ModItems.RUINED_HEART.get()))
                .save(consumer);

        // （堕落王冠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_CROWN.get())
                .pattern("RGR")
                .pattern("RBR")
                .pattern("AAA")
                .define('R', ModItems.RECRYSTALLIZED_RICE.get())
                .define('G', Items.EMERALD)
                .define('B', ModItems.RUINED_HEART.get())
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_ruined_heart", has(ModItems.RUINED_HEART.get()))
                .save(consumer);

        // （七宗堕落）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SEVEN_DEADLY_FALLS.get())
                .pattern("CBC")
                .pattern("AWA")
                .pattern("NTN")
                .define('C', ModItems.RICE_STEALER_HEART.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('A', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .define('W', ModItems.FINAL_SWORD_CODEX.get())
                .define('N', Items.NETHER_STAR)
                .define('T', ModItems.FALLEN_HEART.get())
                .unlockedBy("has_fallen_crown", has(ModItems.FALLEN_CROWN.get()))
                .save(consumer);

        // （天堕翼胸甲；A=天堕锭 N=精制强化米网 T=恶夔长命角门票 C=超懒翼胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEAVENFALL_WINGS_CHESTPLATE.get())
                .pattern("ATA")
                .pattern("ACA")
                .pattern("N N")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .define('C', ModItems.SUPER_LAZY_WINGS_CHESTPLATE.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // （最懒天堕头盔；A=天堕锭 N=精制强化米网 T=恶夔长命角门票 D=超懒合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_HELMET.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern(" N ")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_HELMET.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // （最懒天堕护腿；A=天堕锭 N=精制强化米网 T=恶夔长命角门票 D=超懒合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_LEGGINGS.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern("N N")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_LEGGINGS.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // （最懒天堕靴子；A=天堕锭 N=精制强化米网 T=恶夔长命角门票 D=超懒合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_BOOTS.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern(" N ")
                .define('A', ModItems.HEAVENFALL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.EVIL_KUI_LONGEVITY_HORN.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_BOOTS.get())
                .unlockedBy("has_evil_kui_horn", has(ModItems.EVIL_KUI_LONGEVITY_HORN.get()))
                .save(consumer);

        // （残堕宗罪头盔；A=残堕世界精华锭 N=精制强化米网 T=终末龙逆鳞门票 D=最懒天堕头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_HELMET.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern(" N ")
                .define('A', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.END_DRAGON_REVERSE_SCALE.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_HELMET.get())
                .unlockedBy("has_end_dragon_reverse_scale", has(ModItems.END_DRAGON_REVERSE_SCALE.get()))
                .save(consumer);

        // （残堕宗罪胸甲；A=残堕世界精华锭 N=精制强化米网 T=终末龙逆鳞门票 D=天堕翼胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_CHESTPLATE.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern("N N")
                .define('A', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.END_DRAGON_REVERSE_SCALE.get())
                .define('D', ModItems.HEAVENFALL_WINGS_CHESTPLATE.get())
                .unlockedBy("has_end_dragon_reverse_scale", has(ModItems.END_DRAGON_REVERSE_SCALE.get()))
                .save(consumer);

        // （残堕宗罪护腿；A=残堕世界精华锭 N=精制强化米网 T=终末龙逆鳞门票 D=最懒天堕护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_LEGGINGS.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern("N N")
                .define('A', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.END_DRAGON_REVERSE_SCALE.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_LEGGINGS.get())
                .unlockedBy("has_end_dragon_reverse_scale", has(ModItems.END_DRAGON_REVERSE_SCALE.get()))
                .save(consumer);

        // （残堕宗罪靴子；A=残堕世界精华锭 N=精制强化米网 T=终末龙逆鳞门票 D=最懒天堕靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_BOOTS.get())
                .pattern("ATA")
                .pattern("ADA")
                .pattern(" N ")
                .define('A', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('T', ModItems.END_DRAGON_REVERSE_SCALE.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_BOOTS.get())
                .unlockedBy("has_end_dragon_reverse_scale", has(ModItems.END_DRAGON_REVERSE_SCALE.get()))
                .save(consumer);

        // 啄米鸡肉（生）在熔炉烧 2s（40t）→ 熟啄米鸡肉
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.ZHUOMI_CHICKEN.get()), RecipeCategory.FOOD,
                        ModItems.COOKED_ZHUOMI_CHICKEN.get(), 0.3F, 40)
                .unlockedBy("has_zhuomi_chicken", has(ModItems.ZHUOMI_CHICKEN.get()))
                .save(consumer);

        // ================= 第七批（大杂烩/液体链 + 四系镐） =================

        // （米米大杂烩；五格熟肉+米果+清洁液+腐米肉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MEDLEY.get())
                .pattern("RFA")
                .pattern("BCD")
                .pattern("EGH")
                .define('R', ModItems.RICE.get())
                .define('F', ModItems.RICE_FRUIT.get())
                .define('A', ModItems.COOKED_ZHUOMI_CHICKEN.get())
                .define('B', ModItems.COOKED_MIMOO_BEEF.get())
                .define('C', ModItems.COOKED_MIMI_MUTTON.get())
                .define('D', ModItems.COOKED_MIHENG_PORK.get())
                .define('E', ModItems.COOKED_TANMI_FISH.get())
                .define('G', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('H', ModItems.ROTTEN_RICE_FLESH.get())
                .unlockedBy("has_rotten_rice_flesh", has(ModItems.ROTTEN_RICE_FLESH.get()))
                .save(consumer);

        // 1 个米骨 → 3 个米骨粉（单格物品转多件，必须无序合成）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_BONE_MEAL.get(), 3)
                .requires(ModItems.RICE_BONE.get())
                .unlockedBy("has_rice_bone", has(ModItems.RICE_BONE.get()))
                .save(consumer);

        // （米精：米骨粉围火米药）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_ESSENCE.get())
                .pattern(" B ")
                .pattern("BPB")
                .pattern(" B ")
                .define('B', ModItems.RICE_BONE_MEAL.get())
                .define('P', ModItems.HUOMI_POWDER.get())
                .unlockedBy("has_rice_bone_meal", has(ModItems.RICE_BONE_MEAL.get()))
                .save(consumer);

        // （粗米；任意肉类=mimiworld:meats 标签）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_RICE.get())
                .pattern("BMB")
                .define('B', ModItems.RICE_ESSENCE.get())
                .define('M', ModItemTags.MEATS)
                .unlockedBy("has_rice_essence", has(ModItems.RICE_ESSENCE.get()))
                .save(consumer);

        // （米笔：羽毛/米骨/鱼囊各一排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BRUSH_PEN.get())
                .pattern("FFF")
                .pattern("BBB")
                .pattern("CCC")
                .define('F', ModItems.ZHUOMI_FEATHER.get())
                .define('B', ModItems.RICE_BONE.get())
                .define('C', ModItems.PENMI_FISH_SAC.get())
                .unlockedBy("has_rice_bone", has(ModItems.RICE_BONE.get()))
                .save(consumer);

        // （书米笔：丝线+米书+米笔）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOOK_RICE_PEN.get())
                .pattern("S  ")
                .pattern("KB ")
                .pattern("   ")
                .define('S', ModItems.NIANMI_SILK_THREAD.get())
                .define('K', ModItems.RICE_BOOK.get())
                .define('B', ModItems.RICE_BRUSH_PEN.get())
                .unlockedBy("has_rice_brush_pen", has(ModItems.RICE_BRUSH_PEN.get()))
                .save(consumer);

        // （米果黑混液：鱼囊围清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_BLACK_MIXTURE.get())
                .pattern(" C ")
                .pattern("CFC")
                .pattern(" C ")
                .define('C', ModItems.PENMI_FISH_SAC.get())
                .define('F', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_penmi_fish_sac", has(ModItems.PENMI_FISH_SAC.get()))
                .save(consumer);

        // （米果混黏液：丝线围米果清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get())
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.NIANMI_SILK_THREAD.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （爆米绽放液：爆烈米棒 + 火米粉末围米果清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_BLOOM_LIQUID.get())
                .pattern("PFP")
                .pattern("FBF")
                .pattern("PFP")
                .define('P', ModItems.BAOLIEMI_ROD.get())
                .define('F', ModItems.HUOMI_POWDER.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （爆米嘬心液：狼心围米果清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_HEART_SIP_LIQUID.get())
                .pattern(" W ")
                .pattern("WBW")
                .pattern(" W ")
                .define('W', ModItems.HAOMI_WOLF_HEART.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （眼米嘬心液：珍珠围米果清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.YANMI_HEART_SIP_LIQUID.get())
                .pattern(" E ")
                .pattern("EBE")
                .pattern(" E ")
                .define('E', ModItems.YANMI_PEARL.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （米米杂烩液：大杂烩围米果清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MEDLEY_LIQUID.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.MIMI_MEDLEY.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_mimi_medley", has(ModItems.MIMI_MEDLEY.get()))
                .save(consumer);

        // （超重结晶镐头：米石+硬化米围超重结晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD.get())
                .pattern("SAS")
                .pattern("AFA")
                .pattern("SAS")
                .define('S', ModItems.RICE_STONE.get())
                .define('A', ModItems.HARDENED_RICE.get())
                .define('F', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （复合米饭镐头：米石+结晶米围复合米饭锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get())
                .pattern("SCS")
                .pattern("CFC")
                .pattern("SCS")
                .define('S', ModItems.RICE_STONE.get())
                .define('C', ModItems.CRYSTALLIZED_RICE.get())
                .define('F', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （清洁复合镐头：米石+重结晶米围清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD.get())
                .pattern("SRS")
                .pattern("RFR")
                .pattern("SRS")
                .define('S', ModItems.RICE_STONE.get())
                .define('R', ModItems.RECRYSTALLIZED_RICE.get())
                .define('F', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （超懒合金镐头：米石+熔融混合结晶围超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD.get())
                .pattern("SMS")
                .pattern("MFM")
                .pattern("SMS")
                .define('S', ModItems.RICE_STONE.get())
                .define('M', ModItems.MOLTEN_MIXED_CRYSTAL.get())
                .define('F', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // 注：-四个手柄为锤米台机器配方，见 ModMachineRecipes（非数据包配方）

        // （超重结晶镐）：中上镐头 / 中间纤维 / 下左棒移至中下，竖直三连
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE.get())
                .pattern(" H ")
                .pattern(" F ")
                .pattern(" R ")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('R', ModItems.SUPER_HEAVY_CRYSTAL_ROD.get())
                .unlockedBy("has_super_heavy_crystal_pickaxe_head", has(ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD.get()))
                .save(consumer);

        // （复合米饭镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COMPOUND_RICE_MEAL_PICKAXE.get())
                .pattern(" H ")
                .pattern(" F ")
                .pattern(" R ")
                .define('H', ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .unlockedBy("has_compound_rice_meal_pickaxe_head", has(ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get()))
                .save(consumer);

        // （清洁复合镐）：原下右的复合米饭镐头（不一致项）已按 2026-11-06 精简配方去除
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLEANSING_COMPOUND_PICKAXE.get())
                .pattern(" H ")
                .pattern(" F ")
                .pattern(" R ")
                .define('H', ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD.get())
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .unlockedBy("has_cleansing_compound_pickaxe_head", has(ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD.get()))
                .save(consumer);

        // （超懒合金镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_LAZY_ALLOY_PICKAXE.get())
                .pattern(" H ")
                .pattern(" F ")
                .pattern(" R ")
                .define('H', ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD.get())
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('R', ModItems.SUPER_LAZY_ALLOY_ROD.get())
                .unlockedBy("has_super_lazy_alloy_pickaxe_head", has(ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD.get()))
                .save(consumer);

        // （最懒天堕镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LAZIEST_HEAVENFALL_PICKAXE.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('C', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_PICKAXE.get())
                .unlockedBy("has_super_lazy_alloy_pickaxe", has(ModItems.SUPER_LAZY_ALLOY_PICKAXE.get()))
                .save(consumer);

        // ================= 第八批（堕落台/深染链/世界精华机器） =================

        // （堕落台）
        // 注意：目前仍是 placeholder_占位，本配方在实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_ALTAR.get())
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.MIMI_MEDLEY_LIQUID.get())
                .define('D', Items.ANCIENT_DEBRIS)
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARROW.get())
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(consumer);

        // （堕落十字架）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_CROSS.get())
                .pattern(" C ")
                .pattern("ABA")
                .pattern(" A ")
                .define('C', ModItems.FALLEN_CROWN.get())
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('B', Items.NETHERITE_SCRAP)
                .unlockedBy("has_fallen_crown", has(ModItems.FALLEN_CROWN.get()))
                .save(consumer);

        // （浸染液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DYEING_SOLUTION.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ModItems.REMNANT_FALLEN_RICE_ESSENCE.get())
                .define('B', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('C', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .define('D', ModItems.REMNANT_FALLEN_INGOT.get())
                .unlockedBy("has_remnant_fallen_ingot", has(ModItems.REMNANT_FALLEN_INGOT.get()))
                .save(consumer);

        // 本系列九个深染部件：染液与残堕锭棋盘围，中心放对应 Boss 材料（为鞘翅类装备→COMBAT）
        // 中心残天翼
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DEEP_DYED_REMNANT_HEAVEN_WING.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.REMNANT_HEAVEN_WING.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心虚无之触（深染虚无之触）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_VOID_TOUCH.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.VOID_TOUCH.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心残花枝
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_FADED_FLOWER_BRANCH.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.FADED_FLOWER_BRANCH.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心天凤之喙
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.CELESTIAL_PHOENIX_BEAK.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心残堕米精
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.REMNANT_FALLEN_RICE_ESSENCE.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心鲛泪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MERMAID_TEAR.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.MERMAID_TEAR.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心蛟龙爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_JIAOLONG_TALON.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.JIAOLONG_TALON.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心蜃楼骨
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MIRAGE_BONE.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.MIRAGE_BONE.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心精灵帽
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_ELF_HAT.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.ELF_HAT.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // 中心噬月狼爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION.get())
                .define('B', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('C', ModItems.MOON_DEVOURER_WOLF_CLAW.get())
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION.get()))
                .save(consumer);

        // （磨制残堕世界精华：深染残堕米精 + 残堕深渊岩直接合成）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POLISHED_REMNANT_FALLEN_WORLD_ESSENCE.get())
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get())
                .define('B', ModItems.REMNANT_FALLEN_ABYSS_ROCK.get())
                .unlockedBy("has_deep_dyed_rice_essence", has(ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get()))
                .save(consumer);

        // （磨制残堕精华液）
        // 注意：目前仍是 placeholder_占位，本配方在实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION.get())
                .pattern("XYZ")
                .pattern("WWW")
                .pattern("UYV")
                .define('X', ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK.get())
                .define('Y', ModItems.DEEP_DYED_FADED_FLOWER_BRANCH.get())
                .define('Z', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get())
                .define('W', ModItems.POLISHED_REMNANT_FALLEN_WORLD_ESSENCE.get())
                .define('U', ModItems.DEEP_DYED_MIRAGE_BONE.get())
                .define('V', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW.get())
                .unlockedBy("has_deep_dyed_phoenix_beak", has(ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK.get()))
                .save(consumer);

        // （凝锭台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INGOT_CONDENSATION_TABLE.get())
                .pattern("ABA")
                .pattern("BBB")
                .pattern("CCC")
                .define('A', ModItems.DEEP_DYED_REMNANT_HEAVEN_WING.get())
                .define('B', ModItems.DEEP_DYED_MERMAID_TEAR.get())
                .define('C', ModItems.REMNANT_FALLEN_INGOT.get())
                .unlockedBy("has_deep_dyed_heaven_wing", has(ModItems.DEEP_DYED_REMNANT_HEAVEN_WING.get()))
                .save(consumer);

        // ================= 第十批（箭/餐具/饭团/烹饪机器/龙骨装备/世界净化器） =================
        // 注意：本批 8 个产物（大碗、大锅、米纸盘、小煮锅、煮锅、锅盖、终罪骊珠、终罪逆鳞）
        // 目前仍是 placeholder 占位物品，按"严格直译"照原文注册；占位实装前产物无贴图/名称且不进创造栏。
        // 其中大碗是烹饪机器链的公共配料，米纸盘是烤箱链配料——占位实装后整条链自动生效。

        // （超懒合金箭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_NUGGET.get())
                .define('B', ModItems.SUPER_LAZY_ALLOY_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_super_lazy_alloy_rod", has(ModItems.SUPER_LAZY_ALLOY_ROD.get()))
                .save(consumer);

        // （占位产物；6=米木板）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BIG_BOWL.get())
                .pattern("   ")
                .pattern("A A")
                .pattern(" A ")
                .define('A', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS.get()))
                .save(consumer);

        // （盘子）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PLATE.get())
                .pattern("   ")
                .pattern("AAA")
                .pattern("   ")
                .define('A', ModItems.RICE_STONE.get())
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE.get()))
                .save(consumer);

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_PAPER_PLATE.get())
                .pattern("   ")
                .pattern("APA")
                .pattern(" A ")
                .define('A', ModItems.RICE_PLANKS.get())
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);

        // （天脑饭团；海带片 饭团 熟界天胎花）
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.CELESTIAL_BRAIN_RICE_BALL.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE.get())
                .define('B', ModItems.ONIGIRI.get())
                .define('C', ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER.get())
                .unlockedBy("has_cooked_realm_heaven_embryo_flower", has(ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER.get()))
                .save(consumer);

        // 同上，中心熟浅鲛尾 = 浅鲛饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.PALE_MERMAID_RICE_BALL.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE.get())
                .define('B', ModItems.ONIGIRI.get())
                .define('C', ModItems.COOKED_PALE_MERMAID_TAIL.get())
                .unlockedBy("has_cooked_pale_mermaid_tail", has(ModItems.COOKED_PALE_MERMAID_TAIL.get()))
                .save(consumer);

        // 中心熟望米伤鲛足 = 蛟足饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.MERMAID_LEG_RICE_BALL.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE.get())
                .define('B', ModItems.ONIGIRI.get())
                .define('C', ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG.get())
                .unlockedBy("has_cooked_wangmi_wounded_mermaid_leg", has(ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG.get()))
                .save(consumer);

        // 中心熟噬鹫翅 = 鹫翅饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.VULTURE_WING_RICE_BALL.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE.get())
                .define('B', ModItems.ONIGIRI.get())
                .define('C', ModItems.COOKED_DEVOURER_VULTURE_WING.get())
                .unlockedBy("has_cooked_devourer_vulture_wing", has(ModItems.COOKED_DEVOURER_VULTURE_WING.get()))
                .save(consumer);

        // ---- 烹饪机器链：R=占位旋钮/部件，H=勺子，P=重型加权压力板，I=铁锭 ----
        // （搅碎器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRUSHER.get())
                .pattern(" R ")
                .pattern("ISI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.IRON_SWORD)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
                .save(consumer);

        // （搅拌器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIXER.get())
                .pattern(" R ")
                .pattern("ISI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.IRON_SHOVEL)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
                .save(consumer);

        // （大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LARGE_COOKING_POT.get())
                .pattern(" R ")
                .pattern("IHI")
                .pattern("IPI")
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON.get())
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON.get()))
                .save(consumer);

        // （巨无霸大大大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JUMBO_MEGA_COOKING_POT.get())
                .pattern("BRB")
                .pattern("BHB")
                .pattern("BPB")
                .define('R', ModItems.POT_LID.get())
                .define('B', Items.IRON_BLOCK)
                .define('H', ModItems.SPOON.get())
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_large_cooking_pot", has(ModItems.LARGE_COOKING_POT.get()))
                .save(consumer);

        // （铁锅；7=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_WOK.get())
                .pattern("SR ")
                .pattern("IHI")
                .pattern(" P ")
                .define('S', ModItems.RICE_STICK.get())
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('H', Items.IRON_SHOVEL)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
                .save(consumer);

        // （烤箱；占位）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OVEN.get())
                .pattern("PRP")
                .pattern("PRP")
                .pattern("CRC")
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('R', ModItems.RICE_PAPER_PLATE.get())
                .define('C', Items.CHARCOAL)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL))
                .save(consumer);

        // （占位产物；机器链公共部件）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POT_LID.get())
                .pattern(" W ")
                .pattern(" P ")
                .pattern("   ")
                .define('W', ItemTags.WOODEN_BUTTONS)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_heavy_weighted_pressure_plate", has(Items.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer);

        // （勺子）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPOON.get())
                .pattern("S  ")
                .pattern("S I")
                .pattern("II ")
                .define('S', ModItems.RICE_STICK.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_rice_stick", has(ModItems.RICE_STICK.get()))
                .save(consumer);

        // ---- 箭系列：竖列 材料 / 棒 / 米石 ----
        // （饭晶箭；饭晶 米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('B', ModItems.RICE_STICK.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （饭晶合金箭；饭晶合金粒 7=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_NUGGET.get())
                .define('B', ModItems.RICE_STICK.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_rice_crystal_alloy_nugget", has(ModItems.RICE_CRYSTAL_ALLOY_NUGGET.get()))
                .save(consumer);

        // （超重结晶箭；超重结晶粒 超重结晶棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL_NUGGET.get())
                .define('B', ModItems.SUPER_HEAVY_CRYSTAL_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_super_heavy_crystal_nugget", has(ModItems.SUPER_HEAVY_CRYSTAL_NUGGET.get()))
                .save(consumer);

        // （复合米饭箭；复合米饭粒 复合米饭棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.COMPOUND_RICE_MEAL_NUGGET.get())
                .define('B', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_compound_rice_meal_nugget", has(ModItems.COMPOUND_RICE_MEAL_NUGGET.get()))
                .save(consumer);

        // （清洁复合箭；清洁复合粒 清洁复合棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CLEANSING_COMPOUND_NUGGET.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_cleansing_compound_nugget", has(ModItems.CLEANSING_COMPOUND_NUGGET.get()))
                .save(consumer);

        // ---- 粒 ↔ 锭（超重结晶为 粒 ↔ 结晶）：9 粒合 1，1 拆 9 粒，同原版铁锭/铁粒，可反向 ----
        // 饭晶合金
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.RICE_CRYSTAL_ALLOY_NUGGET.get())
                .unlockedBy("has_rice_crystal_alloy_nugget", has(ModItems.RICE_CRYSTAL_ALLOY_NUGGET.get()))
                .save(consumer, MimiWorld.MODID + ":rice_crystal_alloy_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_NUGGET.get(), 9)
                .requires(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // 超重结晶（无锭，以结晶为互换单位）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.SUPER_HEAVY_CRYSTAL_NUGGET.get())
                .unlockedBy("has_super_heavy_crystal_nugget", has(ModItems.SUPER_HEAVY_CRYSTAL_NUGGET.get()))
                .save(consumer, MimiWorld.MODID + ":super_heavy_crystal_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_NUGGET.get(), 9)
                .requires(ModItems.SUPER_HEAVY_CRYSTAL.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // 复合米饭
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.COMPOUND_RICE_MEAL_NUGGET.get())
                .unlockedBy("has_compound_rice_meal_nugget", has(ModItems.COMPOUND_RICE_MEAL_NUGGET.get()))
                .save(consumer, MimiWorld.MODID + ":compound_rice_meal_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_NUGGET.get(), 9)
                .requires(ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // 清洁复合
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.CLEANSING_COMPOUND_NUGGET.get())
                .unlockedBy("has_cleansing_compound_nugget", has(ModItems.CLEANSING_COMPOUND_NUGGET.get()))
                .save(consumer, MimiWorld.MODID + ":cleansing_compound_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_NUGGET.get(), 9)
                .requires(ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // 超懒合金
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', ModItems.SUPER_LAZY_ALLOY_NUGGET.get())
                .unlockedBy("has_super_lazy_alloy_nugget", has(ModItems.SUPER_LAZY_ALLOY_NUGGET.get()))
                .save(consumer, MimiWorld.MODID + ":super_lazy_alloy_ingot_from_nuggets");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_NUGGET.get(), 9)
                .requires(ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);
    }
}
