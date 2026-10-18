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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
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
 * 同名物品靠药水效果区分（瞬间伤害/虚弱/跳跃/治疗/力量/速度药水等）时不能直接用 Items.POTION
 * （Ingredient.of(ItemStack) 会丢 NBT，任意药水都满足），必须用 potionIngredient(Potions.XXX)，
 * 内部走 Forge StrictNBTIngredient.of(...) 精确匹配；
 * 工作台消耗药水后原版会按合成剩余物返还玻璃瓶，同理水桶返还空桶。
 * 3. “任意树苗/任意木板”等“任意XX” = 原版同名物品标签（ItemTags.SAPLINGS / PLANKS）。
 * “任意肉类”= 本模自建标签 mimiworld:meats（ModItemTags.MEATS，生/熟/鱼/腐肉均含），
 * 内容由 ModItemTagsProvider 生成；因 Forge 1.20.1（47.4.10）Tags.Items 尚无肉类标签。
 * 4. 熔炉烧炼用 SimpleCookingRecipeBuilder.smelting/blasting，时间单位 tick，1s = 20 tick
 * （1s=20t、3s=60t、4s=80t）；高炉若保留同名烧炼，时间按惯例为熔炉一半。
 * 5. 工作台之外的工作方块（锤米台、压饭机等）不是数据包配方，不能写在本类；
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

        // （3 个米木板摆倒 T 形 → 锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.POT.get())
                .pattern("   ")
                .pattern("P P")
                .pattern(" P ")
                .define('P', ModItems.RICE_PLANKS.get())
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

        // （米木板-米棍-米木板三层 → 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_RICE_STICK.get())
                .pattern("PPP")
                .pattern("QQQ")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS.get())
                .define('Q', ModItems.RICE_BATON.get())
                .unlockedBy("has_rice_baton", has(ModItems.RICE_BATON.get()))
                .save(consumer);

        // （两个 米粉夹水桶 → 米棍；空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BATON.get())
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.RICE_FLOUR.get())
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_flour", has(ModItems.RICE_FLOUR.get()))
                .save(consumer);

        // 一个熟米在工作台即可合成 8 个米粉。
        // 单材料配方原版禁止有序注册（无形状可言），按规则注释例外用无序，产出 8 个。
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_FLOUR.get(), 8)
                .requires(ModItems.COOKED_RICE.get())
                .unlockedBy("has_cooked_rice", has(ModItems.COOKED_RICE.get()))
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

        // （米原木夹饭晶锤 → 锤米台）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_POUNDING_TABLE.get())
                .pattern("LLL")
                .pattern("RHR")
                .pattern("LLL")
                .define('L', ModItems.RICE_LOG.get())
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_HAMMER.get())
                .unlockedBy("has_rice_crystal_hammer", has(ModItems.RICE_CRYSTAL_HAMMER.get()))
                .save(consumer);

        // （米原木与米块夹红石 → 压饭机）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_PRESS.get())
                .pattern("LLL")
                .pattern("BRB")
                .pattern("LLL")
                .define('L', ModItems.RICE_LOG.get())
                .define('B', ModItems.RICE_BLOCK.get())
                .define('R', Items.REDSTONE)
                .unlockedBy("has_rice_block", has(ModItems.RICE_BLOCK.get()))
                .save(consumer);

        //
        // （5 个压缩饭桶与 4 个下界合金锭交错 → 粗制饭晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_CRYSTALLIZED_RICE_MEAL.get())
                .pattern("ANA")
                .pattern("NAN")
                .pattern("ANA")
                .define('A', ModItems.COMPRESSED_RICE_PAIL.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compressed_rice_pail", has(ModItems.COMPRESSED_RICE_PAIL.get()))
                .save(consumer);

        // ================= 第二批（草麻/网/超结晶/机器/米果线） =================

        // ---------- 熔炉烧炼（1s=20t，时长严格按用户秒数） ----------

        // 粗制饭晶 熔炉 6s（120t）→ 饭晶
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_CRYSTALLIZED_RICE_MEAL.get()), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE_MEAL.get(), 0.3F, 120)
                .unlockedBy("has_coarse_crystallized_rice_meal", has(ModItems.COARSE_CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // 湿草麻 熔炉 2s（40t）→ 粗制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.WET_GRASS_FIBER.get()), RecipeCategory.MISC, ModItems.COARSE_RICE_FIBER.get(), 0.3F, 40)
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER.get()))
                .save(consumer);

        // 强化湿草麻 熔炉 2s（40t）→ 精制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.REINFORCED_WET_GRASS_FIBER.get()), RecipeCategory.MISC, ModItems.REFINED_RICE_FIBER.get(), 0.3F, 40)
                .unlockedBy("has_reinforced_wet_grass_fiber", has(ModItems.REINFORCED_WET_GRASS_FIBER.get()))
                .save(consumer);

        // 粗制湿草网 熔炉 2s（40t）→ 粗制米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_WET_GRASS_NET.get()), RecipeCategory.MISC, ModItems.COARSE_RICE_NET.get(), 0.3F, 40)
                .unlockedBy("has_coarse_wet_grass_net", has(ModItems.COARSE_WET_GRASS_NET.get()))
                .save(consumer);

        // 精制强化湿草网 熔炉 2s（40t）→ 精制强化米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.REFINED_REINFORCED_WET_GRASS_NET.get()), RecipeCategory.MISC, ModItems.REFINED_REINFORCED_RICE_NET.get(), 0.3F, 40)
                .unlockedBy("has_refined_reinforced_wet_grass_net", has(ModItems.REFINED_REINFORCED_WET_GRASS_NET.get()))
                .save(consumer);

        // 米矿石 熔炉 2s（40t）→ 米石（矿石，经验 0.7；高炉 20t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_ORE.get()), RecipeCategory.MISC, ModItems.RICE_STONE.get(), 0.7F, 40)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE.get()))
                .save(consumer);
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RICE_ORE.get()), RecipeCategory.MISC, ModItems.RICE_STONE.get(), 0.7F, 20)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE.get()))
                .save(consumer, MimiWorld.MODID + ":rice_stone_blasting");

        // 粗制复合米饭锭 熔炉 2s（40t）→ 复合米饭板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT.get()), RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_PLATE.get(), 0.3F, 40)
                .unlockedBy("has_coarse_compound_rice_meal_ingot", has(ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // （饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .pattern("NAN")
                .pattern("AGA")
                .pattern("NAN")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('N', Items.NETHERITE_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （饭晶锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_CRYSTAL_HAMMER.get())
                .pattern("AFA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_refined_rice_fiber", has(ModItems.REFINED_RICE_FIBER.get()))
                .save(consumer);

        // （3 个米草 L 形 → 湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WET_GRASS_FIBER.get())
                .pattern("   ")
                .pattern(" GG")
                .pattern(" G ")
                .define('G', ModItems.RICE_GRASS.get())
                .unlockedBy("has_rice_grass", has(ModItems.RICE_GRASS.get()))
                .save(consumer);

        // （强化湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_WET_GRASS_FIBER.get())
                .pattern("   ")
                .pattern("CWC")
                .pattern("   ")
                .define('C', ModItems.CRYSTALLIZED_RICE.get())
                .define('W', ModItems.WET_GRASS_FIBER.get())
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER.get()))
                .save(consumer);

        // （4 个湿草麻四角 → 湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WET_GRASS_NET.get())
                .pattern("W W")
                .pattern("   ")
                .pattern("W W")
                .define('W', ModItems.WET_GRASS_FIBER.get())
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER.get()))
                .save(consumer);

        // （粗制湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_WET_GRASS_NET.get())
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.COARSE_RICE_FIBER.get())
                .define('W', ModItems.WET_GRASS_NET.get())
                .unlockedBy("has_wet_grass_net", has(ModItems.WET_GRASS_NET.get()))
                .save(consumer);

        // （用户只给两行，第三行全空；强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_WET_GRASS_NET.get())
                .pattern("   ")
                .pattern("RWR")
                .pattern("   ")
                .define('R', ModItems.RECRYSTALLIZED_RICE.get())
                .define('W', ModItems.WET_GRASS_NET.get())
                .unlockedBy("has_wet_grass_net", has(ModItems.WET_GRASS_NET.get()))
                .save(consumer);

        // （精制强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REFINED_REINFORCED_WET_GRASS_NET.get())
                .pattern("   ")
                .pattern("FOF")
                .pattern("   ")
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('O', ModItems.REINFORCED_WET_GRASS_NET.get())
                .unlockedBy("has_reinforced_wet_grass_net", has(ModItems.REINFORCED_WET_GRASS_NET.get()))
                .save(consumer);

        // （饭晶合金锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_CRYSTAL_ALLOY_HAMMER.get())
                .pattern("AHA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('H', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （高级锤米台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.ADVANCED_RICE_POUNDING_TABLE.get())
                .pattern("PPP")
                .pattern("RHR")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS.get())
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_ALLOY_HAMMER.get())
                .unlockedBy("has_rice_crystal_alloy_hammer", has(ModItems.RICE_CRYSTAL_ALLOY_HAMMER.get()))
                .save(consumer);

        // （粗超结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUPER_CRYSTAL.get())
                .pattern("SRS")
                .pattern("GSG")
                .pattern("SRS")
                .define('S', ModItems.RICE_STONE.get())
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE.get()))
                .save(consumer);

        // （超结晶块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.SUPER_CRYSTAL_BLOCK.get())
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', ModItems.SUPER_CRYSTAL.get())
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL.get())
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('D', Items.DIAMOND)
                .define('B', ModItems.SUPER_CRYSTAL_BLOCK.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_super_crystal_block", has(ModItems.SUPER_CRYSTAL_BLOCK.get()))
                .save(consumer);

        // （电离台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.IONIZATION_TABLE.get())
                .pattern("TTT")
                .pattern("RHR")
                .pattern("LLL")
                .define('T', ModItems.RICE_STONE_BLOCK.get())
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_ALLOY_HAMMER.get())
                .define('L', ModItems.RICE_LOG.get())
                .unlockedBy("has_rice_stone_block", has(ModItems.RICE_STONE_BLOCK.get()))
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

        // （粗制复合米饭锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT.get())
                .pattern("AXA")
                .pattern("XAX")
                .pattern("AXA")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('X', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （复合米饭锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .pattern("PPP")
                .pattern("HDH")
                .pattern("PPP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_PLATE.get())
                .define('H', ModItems.RICE_CRYSTAL_HAMMER.get())
                .define('D', Items.DIAMOND)
                .unlockedBy("has_compound_rice_meal_plate", has(ModItems.COMPOUND_RICE_MEAL_PLATE.get()))
                .save(consumer);

        // （8 个米果围 饭晶锤 → 米果渣）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_POMACE.get())
                .pattern("FFF")
                .pattern("FHF")
                .pattern("FFF")
                .define('F', ModItems.RICE_FRUIT.get())
                .define('H', ModItems.RICE_CRYSTAL_HAMMER.get())
                .unlockedBy("has_rice_fruit", has(ModItems.RICE_FRUIT.get()))
                .save(consumer);

        // （米果混合液）
        // 六种原版药水用 potionIngredient 按效果区分；合成后原版返还 6 个玻璃瓶。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_MIXTURE.get())
                .pattern("HWL")
                .pattern("KJP")
                .pattern("TSF")
                .define('H', potionIngredient(Potions.HARMING))
                .define('W', potionIngredient(Potions.WEAKNESS))
                .define('L', potionIngredient(Potions.LEAPING))
                .define('K', ModItems.RICE_BUCKET.get())
                .define('J', ModItems.RICE_FRUIT_JUICE.get())
                .define('P', ModItems.RICE_PAIL.get())
                .define('T', potionIngredient(Potions.HEALING))
                .define('S', potionIngredient(Potions.STRENGTH))
                .define('F', potionIngredient(Potions.SWIFTNESS))
                .unlockedBy("has_rice_fruit_juice", has(ModItems.RICE_FRUIT_JUICE.get()))
                .save(consumer);

        // （离解台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.DISSOCIATION_TABLE.get())
                .pattern(" A ")
                .pattern("BHB")
                .pattern("PPP")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('B', ModItems.RICE_BOOK.get())
                .define('H', ModItems.RICE_CRYSTAL_HAMMER.get())
                .define('P', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_rice_book", has(ModItems.RICE_BOOK.get()))
                .save(consumer);

        // ================= 第三批（装备/清洁/超懒合金/可燃线） =================

        // ---------- 熔炉烧炼（均 2s=40t，经验 0.3） ----------

        // 米果复合液 熔炉 2s（40t）→ 米果混合渣
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_FRUIT_COMPOUND_SOLUTION.get()), RecipeCategory.MISC, ModItems.RICE_FRUIT_MIXED_POMACE.get(), 0.3F, 40)
                .unlockedBy("has_rice_fruit_compound_solution", has(ModItems.RICE_FRUIT_COMPOUND_SOLUTION.get()))
                .save(consumer);

        // 粗制清洁复合锭 熔炉 2s（40t）→ 清洁复合碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_CLEANSING_COMPOUND_INGOT.get()), RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_CHUNK.get(), 0.3F, 40)
                .unlockedBy("has_coarse_cleansing_compound_ingot", has(ModItems.COARSE_CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // 清洁复合碎块 熔炉 2s（40t）→ 清洁复合板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.CLEANSING_COMPOUND_CHUNK.get()), RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_PLATE.get(), 0.3F, 40)
                .unlockedBy("has_cleansing_compound_chunk", has(ModItems.CLEANSING_COMPOUND_CHUNK.get()))
                .save(consumer);

        // 超懒合金粉 熔炉 2s（40t）→ 超懒合金碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.SUPER_LAZY_ALLOY_DUST.get()), RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_CHUNK.get(), 0.3F, 40)
                .unlockedBy("has_super_lazy_alloy_dust", has(ModItems.SUPER_LAZY_ALLOY_DUST.get()))
                .save(consumer);

        // 可燃混合液 熔炉 2s（40t）→ 可燃固态物
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COMBUSTIBLE_MIXTURE.get()), RecipeCategory.MISC, ModItems.COMBUSTIBLE_SOLID.get(), 0.3F, 40)
                .unlockedBy("has_combustible_mixture", has(ModItems.COMBUSTIBLE_MIXTURE.get()))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // （饭晶弓；A=饭晶 F=粗制米麻 S=米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_BOW.get())
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('F', ModItems.COARSE_RICE_FIBER.get())
                .define('S', ModItems.RICE_BATON.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （配方一）米书：精制强化米网 + 3 张米纸
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BOOK.get())
                .pattern("   ")
                .pattern(" N ")
                .pattern("PPP")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
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

        // （米果复合液；M=米饭混合液 A=米果解析液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_COMPOUND_SOLUTION.get())
                .pattern("   ")
                .pattern("MAM")
                .pattern("   ")
                .define('M', ModItems.RICE_MEAL_MIXTURE.get())
                .define('A', ModItems.RICE_FRUIT_ANALYZING_SOLUTION.get())
                .unlockedBy("has_rice_meal_mixture", has(ModItems.RICE_MEAL_MIXTURE.get()))
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

        // （粗制清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_CLEANSING_COMPOUND_INGOT.get())
                .pattern("PPP")
                .pattern("NNN")
                .pattern("PPP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_INGOT.get())
                .pattern("ACA")
                .pattern("CPC")
                .pattern("ACA")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('C', ModItems.CLEANSING_COMPOUND_PLATE.get())
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .unlockedBy("has_cleansing_compound_plate", has(ModItems.CLEANSING_COMPOUND_PLATE.get()))
                .save(consumer);

        // （8 个清洁复合锭围米果清洁液 → 粗制超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUPER_LAZY_ALLOY_INGOT.get())
                .pattern("AAA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('C', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （超懒合金锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .pattern("NAN")
                .pattern("ACA")
                .pattern("NAN")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('A', ModItems.SUPER_LAZY_ALLOY_CHUNK.get())
                .define('C', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_chunk", has(ModItems.SUPER_LAZY_ALLOY_CHUNK.get()))
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

        // （清洁米沉离液；A=米果清洁液 C=可燃固态物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get())
                .pattern("   ")
                .pattern(" AC")
                .pattern("   ")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get())
                .define('C', ModItems.COMBUSTIBLE_SOLID.get())
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get()))
                .save(consumer);

        // （饭晶剑）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_SWORD.get())
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('F', ModItems.COARSE_RICE_FIBER.get())
                .define('S', ModItems.RICE_BATON.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （超结晶剑；X=超结晶 F=精制米麻 S=米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_CRYSTAL_SWORD.get())
                .pattern("  X")
                .pattern("FX ")
                .pattern("SF ")
                .define('X', ModItems.SUPER_CRYSTAL.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.RICE_BATON.get())
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL.get()))
                .save(consumer);

        // （超结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_CRYSTAL_BOW.get())
                .pattern(" XS")
                .pattern("XFS")
                .pattern(" XS")
                .define('X', ModItems.SUPER_CRYSTAL.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('S', ModItems.RICE_BATON.get())
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL.get()))
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

        // （饭晶合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_HELMET.get())
                .pattern("AAA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_CHESTPLATE.get())
                .pattern("A A")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_LEGGINGS.get())
                .pattern("AAA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （饭晶合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_BOOTS.get())
                .pattern("   ")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （超重结晶剑；F=粗制米网 H=超重结晶 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_SWORD.get())
                .pattern(" FH")
                .pattern("SHF")
                .pattern("SS ")
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOW.get())
                .pattern("FHS")
                .pattern("HSF")
                .pattern("FHS")
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_TEMPER_INGOT.get())
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // ================= 第四批（锻造三锭/胚/套装） =================

        // （弓弦锭；4 个清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOWSTRING_INGOT.get())
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （含甲锭；3 个清洁复合锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARMOR_CORE_INGOT.get())
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （高级磨剑锭；5 个超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_SWORD_TEMPER_INGOT.get())
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （高级弓弦锭；4 个超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_BOWSTRING_INGOT.get())
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （高级含甲锭；3 个超懒合金锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_ARMOR_CORE_INGOT.get())
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （超重结晶头盔；H=F=粗制米网 M=精制米麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_HELMET.get())
                .pattern("HHH")
                .pattern("HFH")
                .pattern("M M")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶胸甲；A=饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_CHESTPLATE.get())
                .pattern("HFH")
                .pattern("HHH")
                .pattern("AHA")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_LEGGINGS.get())
                .pattern("HAH")
                .pattern("HFH")
                .pattern("HFH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('F', ModItems.COARSE_RICE_NET.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （超重结晶靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOOTS.get())
                .pattern("   ")
                .pattern("HMH")
                .pattern("HMH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （复合米饭剑；F=P=复合米饭锭 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_SWORD.get())
                .pattern(" FP")
                .pattern("SPF")
                .pattern("SS ")
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOW.get())
                .pattern("FPS")
                .pattern("PSF")
                .pattern("FPS")
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭头盔；N=精制强化米网）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_HELMET.get())
                .pattern("PPP")
                .pattern("PNP")
                .pattern("M M")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_CHESTPLATE.get())
                .pattern("PNP")
                .pattern("PPP")
                .pattern("HPH")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_LEGGINGS.get())
                .pattern("PHP")
                .pattern("PNP")
                .pattern("PNP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （复合米饭靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOOTS.get())
                .pattern("   ")
                .pattern("PMP")
                .pattern("PMP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('M', ModItems.REFINED_RICE_FIBER.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （清洁复合剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get())
                .pattern(" A ")
                .pattern("ANA")
                .pattern("HNH")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('N', Items.NETHERITE_INGOT)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合弓胚；S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_BOW_BLANK.get())
                .pattern("SAS")
                .pattern("ASA")
                .pattern("HHH")
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合剑；B=剑胚 A=磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_SWORD.get())
                .pattern("   ")
                .pattern(" B ")
                .pattern("AAA")
                .define('B', ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get())
                .define('A', ModItems.SWORD_TEMPER_INGOT.get())
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get()))
                .save(consumer);

        // （清洁复合弓；S=弓弦锭 B=弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOW.get())
                .pattern(" S ")
                .pattern("SBS")
                .pattern("   ")
                .define('S', ModItems.BOWSTRING_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_BOW_BLANK.get())
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.CLEANSING_COMPOUND_BOW_BLANK.get()))
                .save(consumer);

        // （清洁复合甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .pattern("HAH")
                .pattern("ANA")
                .pattern("HAH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);

        // （清洁复合头盔；A=含甲锭 B=甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_HELMET.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("   ")
                .define('A', ModItems.ARMOR_CORE_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get()))
                .save(consumer);

        // （清洁复合胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_CHESTPLATE.get())
                .pattern("ABA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ARMOR_CORE_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get()))
                .save(consumer);

        // （清洁复合护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_LEGGINGS.get())
                .pattern("ABA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.ARMOR_CORE_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get()))
                .save(consumer);

        // （清洁复合靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOOTS.get())
                .pattern("ABA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.ARMOR_CORE_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get()))
                .save(consumer);

        // （超懒合金剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK.get())
                .pattern("NBN")
                .pattern("BAB")
                .pattern("NBN")
                .define('N', Items.NETHERITE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get())
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get()))
                .save(consumer);

        // （超懒合金弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_BOW_BLANK.get())
                .pattern("BAB")
                .pattern("BBB")
                .pattern("NNN")
                .define('B', ModItems.CLEANSING_COMPOUND_BOW_BLANK.get())
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.CLEANSING_COMPOUND_BOW_BLANK.get()))
                .save(consumer);

        // （超懒合金甲胚，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .pattern("BAB")
                .pattern("ABA")
                .pattern("BAB")
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get())
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK.get()))
                .save(consumer);

        // （超懒合金剑；A=高级磨剑锭 B=剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_SWORD.get())
                .pattern("ABA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.ADVANCED_SWORD_TEMPER_INGOT.get())
                .define('B', ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK.get())
                .unlockedBy("has_super_lazy_alloy_sword_blank", has(ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK.get()))
                .save(consumer);

        // （修订版）超懒合金弓：高级弓弦锭围边 + 弓胚
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOW.get())
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.ADVANCED_BOWSTRING_INGOT.get())
                .define('B', ModItems.SUPER_LAZY_ALLOY_BOW_BLANK.get())
                .unlockedBy("has_super_lazy_alloy_bow_blank", has(ModItems.SUPER_LAZY_ALLOY_BOW_BLANK.get()))
                .save(consumer);

        // （超懒合金头盔；B=甲胚 A=高级含甲锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_HELMET.get())
                .pattern("BBB")
                .pattern("ANA")
                .pattern("   ")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get()))
                .save(consumer);

        // （超懒合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_CHESTPLATE.get())
                .pattern("BNB")
                .pattern("BBB")
                .pattern("AAA")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT.get())
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get()))
                .save(consumer);

        // 超懒合金护腿：甲胚满行；高级含甲锭与精制强化米网交错；甲胚与米网交错
        // （用户未写等号，按行首条目直译产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_LEGGINGS.get())
                .pattern("BBB")
                .pattern("ANA")
                .pattern("BNB")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get()))
                .save(consumer);

        // ================= 第五批（靴子/铸造台/召唤器链/Boss 生物蛋/神翼） =================

        // （超懒合金靴子；B=甲胚 A=高级含甲锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOOTS.get())
                .pattern("BAB")
                .pattern("ANA")
                .pattern("N N")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT.get())
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get()))
                .save(consumer);

        // （铸剑台；P=复合米饭锭 A=饭晶合金锭 W=米木板）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_FORGING_TABLE.get())
                .pattern(" P ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('W', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （铸甲台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARMOR_FORGING_TABLE.get())
                .pattern("P  ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('W', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （铸弓台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOW_FORGING_TABLE.get())
                .pattern("  P")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('W', ModItems.RICE_PLANKS.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （米米之眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_EYE.get())
                .pattern("RER")
                .pattern("ERE")
                .pattern("RER")
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

        // （粗制召唤核心胚；F=可燃米矿石 B=召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUMMON_CORE_BLANK.get())
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_RICE.get())
                .define('B', ModItems.SUMMON_CORE_BLANK.get())
                .unlockedBy("has_summon_core_blank", has(ModItems.SUMMON_CORE_BLANK.get()))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_SUMMON_CORE_BLANK.get()), RecipeCategory.MISC,
                        ModItems.SUMMON_CORE.get(), 0.3F, 40)
                .unlockedBy("has_coarse_summon_core_blank", has(ModItems.COARSE_SUMMON_CORE_BLANK.get()))
                .save(consumer);

        // （粗制高级召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK.get())
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_MIXTURE.get())
                .define('B', ModItems.ADVANCED_SUMMON_CORE_BLANK.get())
                .unlockedBy("has_advanced_summon_core_blank", has(ModItems.ADVANCED_SUMMON_CORE_BLANK.get()))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 高级召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK.get()), RecipeCategory.MISC,
                        ModItems.ADVANCED_SUMMON_CORE.get(), 0.3F, 40)
                .unlockedBy("has_coarse_advanced_summon_core_blank", has(ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK.get()))
                .save(consumer);

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

        // （粗制最懒召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK.get())
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_SOLID.get())
                .define('B', ModItems.LAZIEST_SUMMON_CORE_BLANK.get())
                .unlockedBy("has_laziest_summon_core_blank", has(ModItems.LAZIEST_SUMMON_CORE_BLANK.get()))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 最懒召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK.get()), RecipeCategory.MISC,
                        ModItems.LAZIEST_SUMMON_CORE.get(), 0.3F, 40)
                .unlockedBy("has_coarse_laziest_summon_core_blank", has(ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK.get()))
                .save(consumer);

        // （米米猴王生物蛋；米果黑混液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MONKEY_KING_SPAWN_EGG.get())
                .pattern("SAS")
                .pattern("AEA")
                .pattern("SAS")
                .define('S', ModItems.REINFORCED_RICE_STICK.get())
                .define('A', ModItems.RICE_FRUIT_BLACK_MIXTURE.get())
                .define('E', Items.DRAGON_EGG)
                .unlockedBy("has_dragon_egg", has(Items.DRAGON_EGG))
                .save(consumer);

        // （饭晶虎王生物蛋；米果混黏液 猴王如意棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_TIGER_KING_SPAWN_EGG.get())
                .pattern("IVI")
                .pattern("VTV")
                .pattern("IVI")
                .define('I', Items.IRON_INGOT)
                .define('V', ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get())
                .define('T', ModItems.MONKEY_KING_RUYI_STAFF.get())
                .unlockedBy("has_monkey_king_ruyi_staff", has(ModItems.MONKEY_KING_RUYI_STAFF.get()))
                .save(consumer);

        // （超晶牛王生物蛋；米石 爆米绽放液 虎王利爪）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_COW_KING_SPAWN_EGG.get())
                .pattern("RLR")
                .pattern("LCL")
                .pattern("RLR")
                .define('R', ModItems.RICE_STONE.get())
                .define('L', ModItems.POPRICE_BLOOM_LIQUID.get())
                .define('C', ModItems.TIGER_KING_RAZOR_CLAW.get())
                .unlockedBy("has_tiger_king_razor_claw", has(ModItems.TIGER_KING_RAZOR_CLAW.get()))
                .save(consumer);

        // （重晶兔王生物蛋；爆米嘬心液 牛王晶角）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG.get())
                .pattern("RHR")
                .pattern("HOH")
                .pattern("RHR")
                .define('R', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('H', ModItems.POPRICE_HEART_SIP_LIQUID.get())
                .define('O', ModItems.OX_KING_CRYSTAL_HORN.get())
                .unlockedBy("has_ox_king_crystal_horn", has(ModItems.OX_KING_CRYSTAL_HORN.get()))
                .save(consumer);

        // （复晶天青蟒生物蛋；眼米嘬心液 兔王腥红眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_AZURE_PYTHON_SPAWN_EGG.get())
                .pattern("RYR")
                .pattern("YEY")
                .pattern("RYR")
                .define('R', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('Y', ModItems.YANMI_HEART_SIP_LIQUID.get())
                .define('E', ModItems.RABBIT_KING_BLOODY_RED_EYE.get())
                .unlockedBy("has_rabbit_king_bloody_red_eye", has(ModItems.RABBIT_KING_BLOODY_RED_EYE.get()))
                .save(consumer);

        // 清洁长命夔生物蛋：龙息四角，米米杂烩液围边，中心极毒青蟒牙
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_LONGEVITY_KUI_SPAWN_EGG.get())
                .pattern("BMB")
                .pattern("MFM")
                .pattern("BMB")
                .define('B', Items.DRAGON_BREATH)
                .define('M', ModItems.MIMI_MEDLEY_LIQUID.get())
                .define('F', ModItems.VENOM_AZURE_PYTHON_FANG.get())
                .unlockedBy("has_venom_azure_python_fang", has(ModItems.VENOM_AZURE_PYTHON_FANG.get()))
                .save(consumer);

        // （最懒终末龙生物蛋）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_FINAL_DRAGON_SPAWN_EGG.get())
                .pattern("RNR")
                .pattern("NRN")
                .pattern("RNR")
                .define('R', ModItems.SUPER_CRYSTAL.get())
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(consumer);

        // （米米神翼；米米幻翼膜）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MIMI_DIVINE_WINGS.get())
                .pattern("AMA")
                .pattern("PEP")
                .pattern("AMA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
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
                .define('A', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK.get())
                .define('C', Items.CALIBRATED_SCULK_SENSOR)
                .unlockedBy("has_dragon_head", has(Items.DRAGON_HEAD))
                .save(consumer);

        // （超懒神剑·残缺·壹）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_1.get())
                .pattern("   ")
                .pattern(" SW")
                .pattern("   ")
                .define('S', ModItems.SUPER_LAZY_ALLOY_SWORD.get())
                .define('W', ModItems.ANCIENT_SWORD_INTENT.get())
                .unlockedBy("has_ancient_sword_intent", has(ModItems.ANCIENT_SWORD_INTENT.get()))
                .save(consumer);

        // （淬剑台，机器方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_QUENCHING_TABLE.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("BBB")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.RECRYSTALLIZED_RICE.get())
                .define('C', ModItems.COMBUSTIBLE_SOLID.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （汲血台，机器方块；T=米棍 W=米木板 L=米原木）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLOODLETTING_TABLE.get())
                .pattern("STS")
                .pattern("WTW")
                .pattern("LLL")
                .define('S', ModItems.COMBUSTIBLE_SOLID.get())
                .define('T', ModItems.RICE_STICK.get())
                .define('W', ModItems.RICE_PLANKS.get())
                .define('L', ModItems.RICE_LOG.get())
                .unlockedBy("has_combustible_solid", has(ModItems.COMBUSTIBLE_SOLID.get()))
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
                .define('S', ModItems.CLEANSING_COMPOUND_SWORD_BLANK.get())
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

        // （剑法禁卷；精制强化米网 米书）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORBIDDEN_SWORD_MANUAL.get())
                .pattern("TNT")
                .pattern("BBB")
                .pattern("TNT")
                .define('T', Items.GHAST_TEAR)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET.get())
                .define('B', ModItems.RICE_BOOK.get())
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(consumer);

        // （超懒剑法）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_SWORD_ART.get())
                .pattern("ABA")
                .pattern("ACD")
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

        // （最懒天堕剑·赝品；超懒神剑·完整）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('B', ModItems.SUPER_LAZY_DIVINE_SWORD_COMPLETE.get())
                .define('C', ModItems.FINAL_SWORD_CODEX.get())
                .unlockedBy("has_final_sword_codex", has(ModItems.FINAL_SWORD_CODEX.get()))
                .save(consumer);

        // （破败之心）
        // 判定：mod 无此物品，"幽匿感应器"=原版幽匿感测体 SCULK_SENSOR；饭晶合金锤
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RUINED_HEART.get())
                .pattern("ENE")
                .pattern("DSD")
                .pattern("END")
                .define('E', Items.ENDER_EYE)
                .define('N', ModItems.RICE_CRYSTAL_ALLOY_HAMMER.get())
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

        // （最懒天堕剑·原罪）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN.get())
                .pattern("ACA")
                .pattern("NBN")
                .pattern("ADA")
                .define('A', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('C', ModItems.FALLEN_CROWN.get())
                .define('N', Items.NETHER_STAR)
                .define('B', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT.get())
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS.get()))
                .save(consumer);

        // （天堕翼胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEAVENFALL_WINGS_CHESTPLATE.get())
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ADA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('C', ModItems.SUPER_LAZY_WINGS_CHESTPLATE.get())
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS.get()))
                .save(consumer);

        // （最懒天堕头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_HELMET.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('C', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_HELMET.get())
                .define('E', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS.get()))
                .save(consumer);

        // （最懒天堕护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_LEGGINGS.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('C', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_LEGGINGS.get())
                .define('E', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS.get()))
                .save(consumer);

        // （最懒天堕靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_BOOTS.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('B', ModItems.FALLEN_CROWN.get())
                .define('C', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_BOOTS.get())
                .define('E', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS.get()))
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

        // （米果混黏液：丝线围黑混液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get())
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.NIANMI_SILK_THREAD.get())
                .define('B', ModItems.RICE_FRUIT_BLACK_MIXTURE.get())
                .unlockedBy("has_black_mixture", has(ModItems.RICE_FRUIT_BLACK_MIXTURE.get()))
                .save(consumer);

        // （爆米绽放液：棋盘）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_BLOOM_LIQUID.get())
                .pattern("PFP")
                .pattern("FBF")
                .pattern("PFP")
                .define('P', ModItems.BAOLIEMI_ROD.get())
                .define('F', ModItems.HUOMI_POWDER.get())
                .define('B', ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get())
                .unlockedBy("has_viscous_mixture", has(ModItems.RICE_FRUIT_VISCOUS_MIXTURE.get()))
                .save(consumer);

        // （爆米嘬心液：狼心围绽放液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_HEART_SIP_LIQUID.get())
                .pattern(" W ")
                .pattern("WBW")
                .pattern(" W ")
                .define('W', ModItems.HAOMI_WOLF_HEART.get())
                .define('B', ModItems.POPRICE_BLOOM_LIQUID.get())
                .unlockedBy("has_bloom_liquid", has(ModItems.POPRICE_BLOOM_LIQUID.get()))
                .save(consumer);

        // （眼米嘬心液：珍珠围爆米嘬心液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.YANMI_HEART_SIP_LIQUID.get())
                .pattern(" E ")
                .pattern("EBE")
                .pattern(" E ")
                .define('E', ModItems.YANMI_PEARL.get())
                .define('B', ModItems.POPRICE_HEART_SIP_LIQUID.get())
                .unlockedBy("has_poprice_heart_sip_liquid", has(ModItems.POPRICE_HEART_SIP_LIQUID.get()))
                .save(consumer);

        // （米米杂烩液：大杂烩围嘬心液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MEDLEY_LIQUID.get())
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.MIMI_MEDLEY.get())
                .define('B', ModItems.YANMI_HEART_SIP_LIQUID.get())
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

        // （超重结晶镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE.get())
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD.get())
                .define('F', ModItems.COARSE_RICE_FIBER.get())
                .define('R', ModItems.SUPER_HEAVY_CRYSTAL_ROD.get())
                .unlockedBy("has_super_heavy_crystal_pickaxe_head", has(ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD.get()))
                .save(consumer);

        // （复合米饭镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COMPOUND_RICE_MEAL_PICKAXE.get())
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
                .define('H', ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get())
                .define('F', ModItems.REFINED_RICE_FIBER.get())
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .unlockedBy("has_compound_rice_meal_pickaxe_head", has(ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get()))
                .save(consumer);

        // （清洁复合镐）
        // 注意：用户原文第三行材质与同系列不一致（左下角放复合米饭棒、右下角放复合米饭镐头），
        // 此处严格照原文直译，待用户确认。
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLEANSING_COMPOUND_PICKAXE.get())
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R E")
                .define('H', ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD.get())
                .define('F', ModItems.COARSE_RICE_NET.get())
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .define('E', ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD.get())
                .unlockedBy("has_cleansing_compound_pickaxe_head", has(ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD.get()))
                .save(consumer);

        // （超懒合金镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_LAZY_ALLOY_PICKAXE.get())
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
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
                .define('C', ModItems.SWORD_QUENCHING_TABLE.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_PICKAXE.get())
                .unlockedBy("has_super_lazy_alloy_pickaxe", has(ModItems.SUPER_LAZY_ALLOY_PICKAXE.get()))
                .save(consumer);

        // ================= 第八批（盗米者蛋/堕落台/深染链/世界精华机器） =================

        // （盗米者生物蛋）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_STEALER_SPAWN_EGG.get())
                .pattern("ESE")
                .pattern("RSR")
                .pattern("ESE")
                .define('E', Items.ECHO_SHARD)
                .define('S', Items.SCULK_SENSOR)
                .define('R', ModItems.RICE_BUCKET.get())
                .unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
                .save(consumer);

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
                .define('B', ModItems.MIMI_MEDLEY_LIQUID.get())
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

        // （粗制残堕世界精华）
        // 注意：目前仍是 placeholder_占位，本配方在实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_REMNANT_FALLEN_WORLD_ESSENCE.get())
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get())
                .define('B', ModItems.REMNANT_FALLEN_ABYSS_ROCK.get())
                .unlockedBy("has_deep_dyed_rice_essence", has(ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get()))
                .save(consumer);

        // （碾磨台）
        // 注意：为占位，同上。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GRINDING_TABLE.get())
                .pattern("AAA")
                .pattern("ACA")
                .pattern("BBB")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('C', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('B', ModItems.REMNANT_FALLEN_ABYSS_ROCK.get())
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT.get()))
                .save(consumer);

        // （杵界台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PESTLE_REALM_TABLE.get())
                .pattern("AAA")
                .pattern("BCD")
                .pattern("EFE")
                .define('A', ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK.get())
                .define('B', ModItems.DEEP_DYED_JIAOLONG_TALON.get())
                .define('C', ModItems.REMNANT_FALLEN_INGOT.get())
                .define('D', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW.get())
                .define('E', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
                .define('F', ModItems.DEEP_DYED_VOID_TOUCH.get())
                .unlockedBy("has_polished_abyss_rock", has(ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK.get()))
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

        // （界主令牌）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REALM_LORD_TOKEN.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.LAZIEST_END_DRAGON_HEAD.get())
                .define('B', ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK.get())
                .define('C', ModItems.WORLD_TEAR.get())
                .unlockedBy("has_world_tear", has(ModItems.WORLD_TEAR.get()))
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
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT.get())
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

        // （占位产物；米石）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BIG_POT.get())
                .pattern("   ")
                .pattern("A A")
                .pattern("AAA")
                .define('A', ModItems.RICE_STONE.get())
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE.get()))
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

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMALL_COOKING_POT.get())
                .pattern(" R ")
                .pattern(" H ")
                .pattern(" P ")
                .define('R', ModItems.POT_LID.get())
                .define('H', ModItems.SPOON.get())
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON.get()))
                .save(consumer);

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COOKING_POT.get())
                .pattern(" R ")
                .pattern("IHI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON.get())
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON.get()))
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

        // （超大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_LARGE_COOKING_POT.get())
                .pattern("IRI")
                .pattern("IHI")
                .pattern("IPI")
                .define('R', ModItems.POT_LID.get())
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON.get())
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_large_cooking_pot", has(ModItems.LARGE_COOKING_POT.get()))
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
                .unlockedBy("has_extra_large_cooking_pot", has(ModItems.EXTRA_LARGE_COOKING_POT.get()))
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

        // ---- 龙骨系列（龙血 / 龙鳞 / 龙骨 / 七宗堕落） ----
        // （占位产物；骊珠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_LI_PEARL.get())
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('X', ModItems.LI_PEARL.get())
                .unlockedBy("has_li_pearl", has(ModItems.LI_PEARL.get()))
                .save(consumer);

        // 同上，中心天虚棘脑汤 = 占位 mmjm 产物
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_REVERSE_SCALE.get())
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('X', ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP.get())
                .unlockedBy("has_celestial_void_thorn_brain_soup", has(ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP.get()))
                .save(consumer);

        // 同上，中心龙鳞 = 终罪龙心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_DRAGON_HEART.get())
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('X', ModItems.DRAGON_SCALE.get())
                .unlockedBy("has_dragon_scale", has(ModItems.DRAGON_SCALE.get()))
                .save(consumer);

        // 同上，中心宗罪龙头 = 终罪龙头
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_DRAGON_HEAD.get())
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('X', ModItems.YU_SIN_DRAGON_HEAD.get())
                .unlockedBy("has_yu_sin_dragon_head", has(ModItems.YU_SIN_DRAGON_HEAD.get()))
                .save(consumer);

        // （残堕宗罪头盔；COMBAT）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_HELMET.get())
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('X', ModItems.LAZIEST_HEAVENFALL_HELMET.get())
                .unlockedBy("has_laziest_heavenfall_helmet", has(ModItems.LAZIEST_HEAVENFALL_HELMET.get()))
                .save(consumer);

        // 中心天堕翼胸甲 = 残堕宗罪胸甲；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_CHESTPLATE.get())
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('X', ModItems.HEAVENFALL_WINGS_CHESTPLATE.get())
                .unlockedBy("has_heavenfall_wings_chestplate", has(ModItems.HEAVENFALL_WINGS_CHESTPLATE.get()))
                .save(consumer);

        // 中心天堕护腿 = 残堕宗罪护腿；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_LEGGINGS.get())
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('X', ModItems.LAZIEST_HEAVENFALL_LEGGINGS.get())
                .unlockedBy("has_laziest_heavenfall_leggings", has(ModItems.LAZIEST_HEAVENFALL_LEGGINGS.get()))
                .save(consumer);

        // 中心天堕靴子 = 残堕宗罪靴子；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_BOOTS.get())
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.DRAGON_BONE.get())
                .define('A', ModItems.DRAGON_BLOOD.get())
                .define('X', ModItems.LAZIEST_HEAVENFALL_BOOTS.get())
                .unlockedBy("has_laziest_heavenfall_boots", has(ModItems.LAZIEST_HEAVENFALL_BOOTS.get()))
                .save(consumer);

        // （残堕宗罪之弓；COMBAT；超懒合金弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_BOW.get())
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BEB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.YU_SIN_DRAGON_HEAD.get())
                .define('D', ModItems.SUPER_LAZY_ALLOY_BOW.get())
                .define('E', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_super_lazy_alloy_bow", has(ModItems.SUPER_LAZY_ALLOY_BOW.get()))
                .save(consumer);

        // （残堕崩解之镐；COMBAT；终罪龙心 最懒天堕镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_DISINTEGRATION_PICKAXE.get())
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BEB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.FINAL_SIN_DRAGON_HEART.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_PICKAXE.get())
                .define('E', ModItems.SEVEN_DEADLY_FALLS.get())
                .unlockedBy("has_laziest_heavenfall_pickaxe", has(ModItems.LAZIEST_HEAVENFALL_PICKAXE.get()))
                .save(consumer);

        // （残堕逆鳞之剑；COMBAT；宗罪龙逆鳞 最懒天堕剑·原罪）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_REVERSE_SCALE_SWORD.get())
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BBB")
                .define('B', ModItems.DRAGON_SCALE.get())
                .define('C', ModItems.YU_SIN_DRAGON_REVERSE_SCALE.get())
                .define('D', ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN.get())
                .unlockedBy("has_laziest_heavenfall_sword_original_sin", has(ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN.get()))
                .save(consumer);

        // （龙鳞之威；龙骨架构 宗罪龙逆鳞）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_SCALE_MIGHT.get())
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DCD")
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('C', ModItems.DRAGON_BONE_FRAMEWORK.get())
                .define('B', ModItems.YU_SIN_DRAGON_REVERSE_SCALE.get())
                .unlockedBy("has_dragon_bone_framework", has(ModItems.DRAGON_BONE_FRAMEWORK.get()))
                .save(consumer);

        // （修订版）龙元：七罪陨围四角与上下边中点，龙血精华围边，中心骊珠，底边中心深染噬月狼爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_YUAN.get())
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DED")
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE.get())
                .define('B', ModItems.LI_PEARL.get())
                .define('E', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW.get())
                .unlockedBy("has_li_pearl", has(ModItems.LI_PEARL.get()))
                .save(consumer);

        // （龙首之怒；龙鳞块 宗罪龙头）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_HEAD_WRATH.get())
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DCD")
                .define('D', ModItems.SEVEN_DEADLY_FALLS.get())
                .define('C', ModItems.DRAGON_SCALE_BLOCK.get())
                .define('B', ModItems.YU_SIN_DRAGON_HEAD.get())
                .unlockedBy("has_yu_sin_dragon_head", has(ModItems.YU_SIN_DRAGON_HEAD.get()))
                .save(consumer);

        // （终罪界心；界主龙心）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_REALM_HEART.get())
                .pattern(" A ")
                .pattern("CBD")
                .pattern("   ")
                .define('A', ModItems.DRAGON_BONE_FRAMEWORK.get())
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE.get())
                .define('B', ModItems.REALM_LORD_DRAGON_HEART.get())
                .define('D', ModItems.DRAGON_SCALE_BLOCK.get())
                .unlockedBy("has_realm_lord_dragon_heart", has(ModItems.REALM_LORD_DRAGON_HEART.get()))
                .save(consumer);

        // （净化架构）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PURIFICATION_FRAMEWORK.get())
                .pattern("A C")
                .pattern("   ")
                .pattern("   ")
                .define('A', ModItems.DRAGON_BONE_FRAMEWORK.get())
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE.get())
                .unlockedBy("has_dragon_blood_essence", has(ModItems.DRAGON_BLOOD_ESSENCE.get()))
                .save(consumer);

        // （世界净化器；宗罪龙头冠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WORLD_PURIFIER.get())
                .pattern("ABA")
                .pattern("CDE")
                .pattern("AFA")
                .define('A', ModItems.PURIFICATION_FRAMEWORK.get())
                .define('B', ModItems.DRAGON_SCALE_MIGHT.get())
                .define('C', ModItems.DRAGON_YUAN.get())
                .define('D', ModItems.CARDINAL_SIN_DRAGON_CROWN.get())
                .define('E', ModItems.DRAGON_HEAD_WRATH.get())
                .define('F', ModItems.FINAL_SIN_REALM_HEART.get())
                .unlockedBy("has_cardinal_sin_dragon_crown", has(ModItems.CARDINAL_SIN_DRAGON_CROWN.get()))
                .save(consumer);

        // ---- 箭系列：竖列 材料 / 棒 / 米石 ----
        // （饭晶箭；饭晶 米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL.get())
                .define('B', ModItems.RICE_BATON.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL.get()))
                .save(consumer);

        // （超结晶箭；超结晶 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_CRYSTAL.get())
                .define('B', ModItems.REINFORCED_RICE_STICK.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL.get()))
                .save(consumer);

        // （饭晶合金箭；饭晶合金锭 7=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())
                .define('B', ModItems.RICE_STICK.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get()))
                .save(consumer);

        // （超重结晶箭；超重结晶 超重结晶棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL.get())
                .define('B', ModItems.SUPER_HEAVY_CRYSTAL_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL.get()))
                .save(consumer);

        // （复合米饭箭；复合米饭锭 复合米饭棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.COMPOUND_RICE_MEAL_INGOT.get())
                .define('B', ModItems.COMPOUND_RICE_MEAL_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT.get()))
                .save(consumer);

        // （清洁复合箭；清洁复合锭 清洁复合棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_ARROW.get())
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT.get())
                .define('B', ModItems.CLEANSING_COMPOUND_ROD.get())
                .define('C', ModItems.RICE_STONE.get())
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT.get()))
                .save(consumer);
    }

    /**
     * 构造指定效果的原版药水 Ingredient（Items.POTION + Potion NBT），
     * 用于工作台有序配方的 define(Character, Ingredient)。
     * 注意：1.20.1 的 Ingredient.of(ItemStack) 会丢弃 NBT（任意药水都满足），
     * 必须用 Forge 的 StrictNBTIngredient 精确匹配，序列化为 type=forge:nbt。
     */
    private static Ingredient potionIngredient(Potion potion) {
        return StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
    }
}
