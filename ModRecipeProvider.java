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
                .smelting(Ingredient.of(ModItems.RICE), RecipeCategory.FOOD, ModItems.COOKED_RICE, 0.5F, 20)
                .unlockedBy("has_rice", has(ModItems.RICE))
                .save(consumer, MimiWorld.MODID + ":rice_smelting");

        // 米球 → 熟米球，用户指定烧制 4s（80t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_BALL), RecipeCategory.FOOD, ModItems.COOKED_RICE_BALL, 0.3F, 80)
                .unlockedBy("has_rice_ball", has(ModItems.RICE_BALL))
                .save(consumer);

        // 结晶米矿石 → 结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.CRYSTALLIZED_RICE_ORE), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE, 0.7F, 60)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.CRYSTALLIZED_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.CRYSTALLIZED_RICE_ORE), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE, 0.7F, 30)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.CRYSTALLIZED_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_blasting");

        // 重结晶米矿石 → 重结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RECRYSTALLIZED_RICE_ORE), RecipeCategory.MISC, ModItems.RECRYSTALLIZED_RICE, 0.7F, 60)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.RECRYSTALLIZED_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RECRYSTALLIZED_RICE_ORE), RecipeCategory.MISC, ModItems.RECRYSTALLIZED_RICE, 0.7F, 30)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.RECRYSTALLIZED_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_blasting");

        // 可燃米矿石烧炼成 可燃米（强燃料材料），经验 0.7，熔炉 200t / 高炉 100t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COMBUSTIBLE_RICE_ORE), RecipeCategory.MISC, ModItems.COMBUSTIBLE_RICE, 0.7F, 200)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.COMBUSTIBLE_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.COMBUSTIBLE_RICE_ORE), RecipeCategory.MISC, ModItems.COMBUSTIBLE_RICE, 0.7F, 100)
                .unlockedBy("has_combustible_rice_ore", has(ModItems.COMBUSTIBLE_RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":combustible_rice_blasting");

        // ================= 生鲜食材熔炉烧炼（统一 200t=10s，经验 0.35）=================
        // 熟界天胎花的生版物品不存在，经用户确认用界天脑花烧制熟界天胎花。
        Item[][] smeltPairs = {
                {ModItems.RAW_MIMOO_BEEF, ModItems.COOKED_MIMOO_BEEF},
                {ModItems.RAW_MIMI_MUTTON, ModItems.COOKED_MIMI_MUTTON},
                {ModItems.RAW_MIHENG_PORK, ModItems.COOKED_MIHENG_PORK},
                {ModItems.RAW_TANMI_FISH, ModItems.COOKED_TANMI_FISH},
                {ModItems.REALM_HEAVEN_BRAIN_FLOWER, ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER},
                {ModItems.VOID_TOUCH_TENTACLE, ModItems.COOKED_VOID_TOUCH_TENTACLE},
                {ModItems.THORN_PETAL, ModItems.COOKED_THORN_PETAL},
                {ModItems.RICE_PHOENIX_MEAT_CHUNK, ModItems.COOKED_RICE_PHOENIX_MEAT_CHUNK},
                {ModItems.SPECIALTY_FALLEN_RICE, ModItems.COOKED_SPECIALTY_FALLEN_RICE},
                {ModItems.PALE_MERMAID_TAIL, ModItems.COOKED_PALE_MERMAID_TAIL},
                {ModItems.WANGMI_WOUNDED_MERMAID_LEG, ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG},
                {ModItems.HALF_MIRAGE_MEAT_SLICE, ModItems.COOKED_HALF_MIRAGE_MEAT_SLICE},
                {ModItems.DEVOURER_VULTURE_WING, ModItems.COOKED_DEVOURER_VULTURE_WING}
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

        // （4 个 米在右下 2×2 → 米球）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BALL)
                .pattern("   ")
                .pattern(" RR")
                .pattern(" RR")
                .define('R', ModItems.RICE)
                .unlockedBy("has_rice", has(ModItems.RICE))
                .save(consumer);

        // （两个 米木板竖排 → 米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_STICK)
                .pattern("   ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', ModItems.RICE_PLANKS)
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS))
                .save(consumer);

        // （3 个 米木板摆倒 T 形 → 锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.POT)
                .pattern("   ")
                .pattern("P P")
                .pattern(" P ")
                .define('P', ModItems.RICE_PLANKS)
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS))
                .save(consumer);

        // （8 个 米块围桶 → 米桶；桶按原版机制返还空桶）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_BUCKET)
                .pattern("BBB")
                .pattern("BWB")
                .pattern("BBB")
                .define('B', ModItems.RICE_BLOCK)
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_block", has(ModItems.RICE_BLOCK))
                .save(consumer);

        // （米 + 任意树苗 → 米树苗）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.RICE_SAPLING)
                .pattern("   ")
                .pattern(" RS")
                .pattern("   ")
                .define('R', ModItems.RICE)
                .define('S', ItemTags.SAPLINGS)
                .unlockedBy("has_rice", has(ModItems.RICE))
                .save(consumer);

        // （米木板-米棍-米木板三层 → 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_RICE_STICK)
                .pattern("PPP")
                .pattern("QQQ")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS)
                .define('Q', ModItems.RICE_BATON)
                .unlockedBy("has_rice_baton", has(ModItems.RICE_BATON))
                .save(consumer);

        // （两个 米粉夹水桶 → 米棍；空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BATON)
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.RICE_FLOUR)
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_flour", has(ModItems.RICE_FLOUR))
                .save(consumer);

        // 一个 熟米在工作台即可合成 8 个 米粉。
        // 单材料配方原版禁止有序注册（无形状可言），按规则注释例外用无序，产出 8 个。
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_FLOUR, 8)
                .requires(ModItems.COOKED_RICE)
                .unlockedBy("has_cooked_rice", has(ModItems.COOKED_RICE))
                .save(consumer);

        // （粗米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.COARSE_RICE_BLOCK)
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
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_MAKER)
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
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_BLOCK)
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .define('R', ModItems.RICE)
                .unlockedBy("has_rice", has(ModItems.RICE))
                .save(consumer);

        // （米原木夹 饭晶锤 → 锤米台）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_POUNDING_TABLE)
                .pattern("LLL")
                .pattern("RHR")
                .pattern("LLL")
                .define('L', ModItems.RICE_LOG)
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_HAMMER)
                .unlockedBy("has_rice_crystal_hammer", has(ModItems.RICE_CRYSTAL_HAMMER))
                .save(consumer);

        // （米原木与 米块夹红石 → 压饭机）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_PRESS)
                .pattern("LLL")
                .pattern("BRB")
                .pattern("LLL")
                .define('L', ModItems.RICE_LOG)
                .define('B', ModItems.RICE_BLOCK)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_rice_block", has(ModItems.RICE_BLOCK))
                .save(consumer);

        //
        // （5 个 压缩饭桶与 4 个下界合金锭交错 → 粗制结晶饭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_CRYSTALLIZED_RICE_MEAL)
                .pattern("ANA")
                .pattern("NAN")
                .pattern("ANA")
                .define('A', ModItems.COMPRESSED_RICE_PAIL)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compressed_rice_pail", has(ModItems.COMPRESSED_RICE_PAIL))
                .save(consumer);

        // ================= 第二批（草麻/网/超结晶/机器/米果线） =================

        // ---------- 熔炉烧炼（1s=20t，时长严格按用户秒数） ----------

        // 粗制结晶饭 熔炉 6s（120t）→ 结晶饭
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_CRYSTALLIZED_RICE_MEAL), RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE_MEAL, 0.3F, 120)
                .unlockedBy("has_coarse_crystallized_rice_meal", has(ModItems.COARSE_CRYSTALLIZED_RICE_MEAL))
                .save(consumer);

        // 湿草麻 熔炉 2s（40t）→ 粗制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.WET_GRASS_FIBER), RecipeCategory.MISC, ModItems.COARSE_RICE_FIBER, 0.3F, 40)
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER))
                .save(consumer);

        // 强化湿草麻 熔炉 2s（40t）→ 精制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.REINFORCED_WET_GRASS_FIBER), RecipeCategory.MISC, ModItems.REFINED_RICE_FIBER, 0.3F, 40)
                .unlockedBy("has_reinforced_wet_grass_fiber", has(ModItems.REINFORCED_WET_GRASS_FIBER))
                .save(consumer);

        // 粗制湿草网 熔炉 2s（40t）→ 粗制米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_WET_GRASS_NET), RecipeCategory.MISC, ModItems.COARSE_RICE_NET, 0.3F, 40)
                .unlockedBy("has_coarse_wet_grass_net", has(ModItems.COARSE_WET_GRASS_NET))
                .save(consumer);

        // 精制强化湿草网 熔炉 2s（40t）→ 精制强化米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.REFINED_REINFORCED_WET_GRASS_NET), RecipeCategory.MISC, ModItems.REFINED_REINFORCED_RICE_NET, 0.3F, 40)
                .unlockedBy("has_refined_reinforced_wet_grass_net", has(ModItems.REFINED_REINFORCED_WET_GRASS_NET))
                .save(consumer);

        // 米矿石 熔炉 2s（40t）→ 米石（矿石，经验 0.7；高炉 20t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_ORE), RecipeCategory.MISC, ModItems.RICE_STONE, 0.7F, 40)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE))
                .save(consumer);
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RICE_ORE), RecipeCategory.MISC, ModItems.RICE_STONE, 0.7F, 20)
                .unlockedBy("has_rice_ore", has(ModItems.RICE_ORE))
                .save(consumer, MimiWorld.MODID + ":rice_stone_blasting");

        // 粗制复合米饭锭 熔炉 2s（40t）→ 复合米饭板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT), RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_PLATE, 0.3F, 40)
                .unlockedBy("has_coarse_compound_rice_meal_ingot", has(ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // （饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .pattern("NAN")
                .pattern("AGA")
                .pattern("NAN")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('N', Items.NETHERITE_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL))
                .save(consumer);

        // （饭晶锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_CRYSTAL_HAMMER)
                .pattern("AFA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_refined_rice_fiber", has(ModItems.REFINED_RICE_FIBER))
                .save(consumer);

        // （3 个 米草 L 形 → 湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WET_GRASS_FIBER)
                .pattern("   ")
                .pattern(" GG")
                .pattern(" G ")
                .define('G', ModItems.RICE_GRASS)
                .unlockedBy("has_rice_grass", has(ModItems.RICE_GRASS))
                .save(consumer);

        // （强化湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_WET_GRASS_FIBER)
                .pattern("   ")
                .pattern("CWC")
                .pattern("   ")
                .define('C', ModItems.CRYSTALLIZED_RICE)
                .define('W', ModItems.WET_GRASS_FIBER)
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER))
                .save(consumer);

        // （4 个湿草麻四角 → 湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WET_GRASS_NET)
                .pattern("W W")
                .pattern("   ")
                .pattern("W W")
                .define('W', ModItems.WET_GRASS_FIBER)
                .unlockedBy("has_wet_grass_fiber", has(ModItems.WET_GRASS_FIBER))
                .save(consumer);

        // （粗制湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_WET_GRASS_NET)
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.COARSE_RICE_FIBER)
                .define('W', ModItems.WET_GRASS_NET)
                .unlockedBy("has_wet_grass_net", has(ModItems.WET_GRASS_NET))
                .save(consumer);

        // （用户只给两行，第三行全空；强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_WET_GRASS_NET)
                .pattern("   ")
                .pattern("RWR")
                .pattern("   ")
                .define('R', ModItems.RECRYSTALLIZED_RICE)
                .define('W', ModItems.WET_GRASS_NET)
                .unlockedBy("has_wet_grass_net", has(ModItems.WET_GRASS_NET))
                .save(consumer);

        // （精制强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REFINED_REINFORCED_WET_GRASS_NET)
                .pattern("   ")
                .pattern("FOF")
                .pattern("   ")
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('O', ModItems.REINFORCED_WET_GRASS_NET)
                .unlockedBy("has_reinforced_wet_grass_net", has(ModItems.REINFORCED_WET_GRASS_NET))
                .save(consumer);

        // （饭晶合金锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RICE_CRYSTAL_ALLOY_HAMMER)
                .pattern("AHA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('H', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （高级锤米台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.ADVANCED_RICE_POUNDING_TABLE)
                .pattern("PPP")
                .pattern("RHR")
                .pattern("PPP")
                .define('P', ModItems.RICE_PLANKS)
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_ALLOY_HAMMER)
                .unlockedBy("has_rice_crystal_alloy_hammer", has(ModItems.RICE_CRYSTAL_ALLOY_HAMMER))
                .save(consumer);

        // （粗超结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUPER_CRYSTAL)
                .pattern("SRS")
                .pattern("GSG")
                .pattern("SRS")
                .define('S', ModItems.RICE_STONE)
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE))
                .save(consumer);

        // （超结晶块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.SUPER_CRYSTAL_BLOCK)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', ModItems.SUPER_CRYSTAL)
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL))
                .save(consumer);

        // （超重结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL)
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('D', Items.DIAMOND)
                .define('B', ModItems.SUPER_CRYSTAL_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_super_crystal_block", has(ModItems.SUPER_CRYSTAL_BLOCK))
                .save(consumer);

        // （电离台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.IONIZATION_TABLE)
                .pattern("TTT")
                .pattern("RHR")
                .pattern("LLL")
                .define('T', ModItems.RICE_STONE_BLOCK)
                .define('R', Items.REDSTONE)
                .define('H', ModItems.RICE_CRYSTAL_ALLOY_HAMMER)
                .define('L', ModItems.RICE_LOG)
                .unlockedBy("has_rice_stone_block", has(ModItems.RICE_STONE_BLOCK))
                .save(consumer);

        // （沉淀台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.PRECIPITATION_TABLE)
                .pattern("NNN")
                .pattern(" G ")
                .pattern("TTT")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('G', Items.GOLD_INGOT)
                .define('T', ModItems.RICE_STONE_BLOCK)
                .unlockedBy("has_refined_reinforced_rice_net", has(ModItems.REFINED_REINFORCED_RICE_NET))
                .save(consumer);

        // （粗制复合米饭锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT)
                .pattern("AXA")
                .pattern("XAX")
                .pattern("AXA")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('X', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （复合米饭锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_INGOT)
                .pattern("PPP")
                .pattern("HDH")
                .pattern("PPP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_PLATE)
                .define('H', ModItems.RICE_CRYSTAL_HAMMER)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_compound_rice_meal_plate", has(ModItems.COMPOUND_RICE_MEAL_PLATE))
                .save(consumer);

        // （8 个 米果围 饭晶锤 → 米果渣）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_POMACE)
                .pattern("FFF")
                .pattern("FHF")
                .pattern("FFF")
                .define('F', ModItems.RICE_FRUIT)
                .define('H', ModItems.RICE_CRYSTAL_HAMMER)
                .unlockedBy("has_rice_fruit", has(ModItems.RICE_FRUIT))
                .save(consumer);

        // （米果混合液）
        // 六种原版药水用 potionIngredient 按效果区分；合成后原版返还 6 个玻璃瓶。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_MIXTURE)
                .pattern("HWL")
                .pattern("KJP")
                .pattern("TSF")
                .define('H', potionIngredient(Potions.HARMING))
                .define('W', potionIngredient(Potions.WEAKNESS))
                .define('L', potionIngredient(Potions.LEAPING))
                .define('K', ModItems.RICE_BUCKET)
                .define('J', ModItems.RICE_FRUIT_JUICE)
                .define('P', ModItems.RICE_PAIL)
                .define('T', potionIngredient(Potions.HEALING))
                .define('S', potionIngredient(Potions.STRENGTH))
                .define('F', potionIngredient(Potions.SWIFTNESS))
                .unlockedBy("has_rice_fruit_juice", has(ModItems.RICE_FRUIT_JUICE))
                .save(consumer);

        // （离解台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.DISSOCIATION_TABLE)
                .pattern(" A ")
                .pattern("BHB")
                .pattern("PPP")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('B', ModItems.RICE_BOOK)
                .define('H', ModItems.RICE_CRYSTAL_HAMMER)
                .define('P', ModItems.RICE_PLANKS)
                .unlockedBy("has_rice_book", has(ModItems.RICE_BOOK))
                .save(consumer);

        // ================= 第三批（装备/清洁/超懒合金/可燃线） =================

        // ---------- 熔炉烧炼（均 2s=40t，经验 0.3） ----------

        // 米果复合液 熔炉 2s（40t）→ 米果混合渣
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RICE_FRUIT_COMPOUND_SOLUTION), RecipeCategory.MISC, ModItems.RICE_FRUIT_MIXED_POMACE, 0.3F, 40)
                .unlockedBy("has_rice_fruit_compound_solution", has(ModItems.RICE_FRUIT_COMPOUND_SOLUTION))
                .save(consumer);

        // 粗制清洁复合锭 熔炉 2s（40t）→ 清洁复合碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COARSE_CLEANSING_COMPOUND_INGOT), RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_CHUNK, 0.3F, 40)
                .unlockedBy("has_coarse_cleansing_compound_ingot", has(ModItems.COARSE_CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // 清洁复合碎块 熔炉 2s（40t）→ 清洁复合板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.CLEANSING_COMPOUND_CHUNK), RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_PLATE, 0.3F, 40)
                .unlockedBy("has_cleansing_compound_chunk", has(ModItems.CLEANSING_COMPOUND_CHUNK))
                .save(consumer);

        // 超懒合金粉 熔炉 2s（40t）→ 超懒合金碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.SUPER_LAZY_ALLOY_DUST), RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_CHUNK, 0.3F, 40)
                .unlockedBy("has_super_lazy_alloy_dust", has(ModItems.SUPER_LAZY_ALLOY_DUST))
                .save(consumer);

        // 可燃混合液 熔炉 2s（40t）→ 可燃固态物
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.COMBUSTIBLE_MIXTURE), RecipeCategory.MISC, ModItems.COMBUSTIBLE_SOLID, 0.3F, 40)
                .unlockedBy("has_combustible_mixture", has(ModItems.COMBUSTIBLE_MIXTURE))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // （饭晶弓；A=结晶饭 F=粗制米麻 S=米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_BOW)
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('F', ModItems.COARSE_RICE_FIBER)
                .define('S', ModItems.RICE_BATON)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL))
                .save(consumer);

        // （配方一）米书：精制强化米网 + 3 张米纸
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BOOK)
                .pattern("   ")
                .pattern(" N ")
                .pattern("PPP")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('P', ModItems.RICE_PAPER)
                .unlockedBy("has_rice_paper", has(ModItems.RICE_PAPER))
                .save(consumer);

        // （修订版）3 个米果渣横排 → 米纸
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_PAPER)
                .pattern("   ")
                .pattern("FFF")
                .pattern("   ")
                .define('F', ModItems.RICE_POMACE)
                .unlockedBy("has_rice_pomace", has(ModItems.RICE_POMACE))
                .save(consumer);

        // （米果复合液；M=米饭混合液 A=米果解析液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_COMPOUND_SOLUTION)
                .pattern("   ")
                .pattern("MAM")
                .pattern("   ")
                .define('M', ModItems.RICE_MEAL_MIXTURE)
                .define('A', ModItems.RICE_FRUIT_ANALYZING_SOLUTION)
                .unlockedBy("has_rice_meal_mixture", has(ModItems.RICE_MEAL_MIXTURE))
                .save(consumer);

        // （米饭混合液；饭桶+水桶+米桶，空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_MEAL_MIXTURE)
                .pattern("   ")
                .pattern("PWB")
                .pattern("   ")
                .define('P', ModItems.RICE_PAIL)
                .define('W', Items.WATER_BUCKET)
                .define('B', ModItems.RICE_BUCKET)
                .unlockedBy("has_rice_pail", has(ModItems.RICE_PAIL))
                .save(consumer);

        // （粗制清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_CLEANSING_COMPOUND_INGOT)
                .pattern("PPP")
                .pattern("NNN")
                .pattern("PPP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_INGOT)
                .pattern("ACA")
                .pattern("CPC")
                .pattern("ACA")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .define('C', ModItems.CLEANSING_COMPOUND_PLATE)
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .unlockedBy("has_cleansing_compound_plate", has(ModItems.CLEANSING_COMPOUND_PLATE))
                .save(consumer);

        // （8 个清洁复合锭围米果清洁液 → 粗制超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUPER_LAZY_ALLOY_INGOT)
                .pattern("AAA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('C', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （超懒合金锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_INGOT)
                .pattern("NAN")
                .pattern("ACA")
                .pattern("NAN")
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('A', ModItems.SUPER_LAZY_ALLOY_CHUNK)
                .define('C', ModItems.CLEANSING_COMPOUND_INGOT)
                .unlockedBy("has_super_lazy_alloy_chunk", has(ModItems.SUPER_LAZY_ALLOY_CHUNK))
                .save(consumer);

        // （米石块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RICE_STONE_BLOCK)
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.RICE_STONE)
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE))
                .save(consumer);

        // （8 个可燃米围米饭混合液 → 可燃混合液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMBUSTIBLE_MIXTURE)
                .pattern("FFF")
                .pattern("FMF")
                .pattern("FFF")
                .define('F', ModItems.COMBUSTIBLE_RICE)
                .define('M', ModItems.RICE_MEAL_MIXTURE)
                .unlockedBy("has_combustible_rice", has(ModItems.COMBUSTIBLE_RICE))
                .save(consumer);

        // （清洁米沉离液；A=米果清洁液 C=可燃固态物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_RICE_SEPARATION_SOLUTION)
                .pattern("   ")
                .pattern(" AC")
                .pattern("   ")
                .define('A', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .define('C', ModItems.COMBUSTIBLE_SOLID)
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.RICE_FRUIT_CLEANSING_SOLUTION))
                .save(consumer);

        // （饭晶剑）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_SWORD)
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('F', ModItems.COARSE_RICE_FIBER)
                .define('S', ModItems.RICE_BATON)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL))
                .save(consumer);

        // （超结晶剑；X=超结晶 F=精制米麻 S=米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_CRYSTAL_SWORD)
                .pattern("  X")
                .pattern("FX ")
                .pattern("SF ")
                .define('X', ModItems.SUPER_CRYSTAL)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('S', ModItems.RICE_BATON)
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL))
                .save(consumer);

        // （超结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_CRYSTAL_BOW)
                .pattern(" XS")
                .pattern("XFS")
                .pattern(" XS")
                .define('X', ModItems.SUPER_CRYSTAL)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('S', ModItems.RICE_BATON)
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL))
                .save(consumer);

        // （饭晶合金剑；A=饭晶合金锭 F=S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_SWORD)
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （饭晶合金弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_BOW)
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （饭晶合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_HELMET)
                .pattern("AAA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （饭晶合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_CHESTPLATE)
                .pattern("A A")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （饭晶合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_LEGGINGS)
                .pattern("AAA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （饭晶合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RICE_CRYSTAL_ALLOY_BOOTS)
                .pattern("   ")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （超重结晶剑；F=粗制米网 H=超重结晶 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_SWORD)
                .pattern(" FH")
                .pattern("SHF")
                .pattern("SS ")
                .define('F', ModItems.COARSE_RICE_NET)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （超重结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOW)
                .pattern("FHS")
                .pattern("HSF")
                .pattern("FHS")
                .define('F', ModItems.COARSE_RICE_NET)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_TEMPER_INGOT)
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // ================= 第四批（锻造三锭/胚/套装） =================

        // （弓弦锭；4 个 清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOWSTRING_INGOT)
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （含甲锭；3 个清洁复合锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARMOR_CORE_INGOT)
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （高级磨剑锭；5 个 超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_SWORD_TEMPER_INGOT)
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // （高级弓弦锭；4 个超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_BOWSTRING_INGOT)
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // （高级含甲锭；3 个超懒合金锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_ARMOR_CORE_INGOT)
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // （超重结晶头盔；H=F=粗制米网 M=精制米麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_HELMET)
                .pattern("HHH")
                .pattern("HFH")
                .pattern("M M")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('F', ModItems.COARSE_RICE_NET)
                .define('M', ModItems.REFINED_RICE_FIBER)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （超重结晶胸甲；A=饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_CHESTPLATE)
                .pattern("HFH")
                .pattern("HHH")
                .pattern("AHA")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('F', ModItems.COARSE_RICE_NET)
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （超重结晶护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_LEGGINGS)
                .pattern("HAH")
                .pattern("HFH")
                .pattern("HFH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('F', ModItems.COARSE_RICE_NET)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （超重结晶靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_HEAVY_CRYSTAL_BOOTS)
                .pattern("   ")
                .pattern("HMH")
                .pattern("HMH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('M', ModItems.REFINED_RICE_FIBER)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （复合米饭剑；F=P=复合米饭锭 S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_SWORD)
                .pattern(" FP")
                .pattern("SPF")
                .pattern("SS ")
                .define('F', ModItems.COARSE_RICE_NET)
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （复合米饭弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOW)
                .pattern("FPS")
                .pattern("PSF")
                .pattern("FPS")
                .define('F', ModItems.COARSE_RICE_NET)
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （复合米饭头盔；N=精制强化米网）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_HELMET)
                .pattern("PPP")
                .pattern("PNP")
                .pattern("M M")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('M', ModItems.REFINED_RICE_FIBER)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （复合米饭胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_CHESTPLATE)
                .pattern("PNP")
                .pattern("PPP")
                .pattern("HPH")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （复合米饭护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_LEGGINGS)
                .pattern("PHP")
                .pattern("PNP")
                .pattern("PNP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （复合米饭靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMPOUND_RICE_MEAL_BOOTS)
                .pattern("   ")
                .pattern("PMP")
                .pattern("PMP")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('M', ModItems.REFINED_RICE_FIBER)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （清洁复合剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_SWORD_BLANK)
                .pattern(" A ")
                .pattern("ANA")
                .pattern("HNH")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （清洁复合弓胚；S=强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_BOW_BLANK)
                .pattern("SAS")
                .pattern("ASA")
                .pattern("HHH")
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （清洁复合剑；B=剑胚 A=磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_SWORD)
                .pattern("   ")
                .pattern(" B ")
                .pattern("AAA")
                .define('B', ModItems.CLEANSING_COMPOUND_SWORD_BLANK)
                .define('A', ModItems.SWORD_TEMPER_INGOT)
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.CLEANSING_COMPOUND_SWORD_BLANK))
                .save(consumer);

        // （清洁复合弓；S=弓弦锭 B=弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOW)
                .pattern(" S ")
                .pattern("SBS")
                .pattern("   ")
                .define('S', ModItems.BOWSTRING_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_BOW_BLANK)
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.CLEANSING_COMPOUND_BOW_BLANK))
                .save(consumer);

        // （清洁复合甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .pattern("HAH")
                .pattern("ANA")
                .pattern("HAH")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （清洁复合头盔；A=含甲锭 B=甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_HELMET)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("   ")
                .define('A', ModItems.ARMOR_CORE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK))
                .save(consumer);

        // （清洁复合胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_CHESTPLATE)
                .pattern("ABA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ARMOR_CORE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK))
                .save(consumer);

        // （清洁复合护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_LEGGINGS)
                .pattern("ABA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.ARMOR_CORE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK))
                .save(consumer);

        // （清洁复合靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CLEANSING_COMPOUND_BOOTS)
                .pattern("ABA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.ARMOR_CORE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK))
                .save(consumer);

        // （超懒合金剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK)
                .pattern("NBN")
                .pattern("BAB")
                .pattern("NBN")
                .define('N', Items.NETHERITE_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_SWORD_BLANK)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.CLEANSING_COMPOUND_SWORD_BLANK))
                .save(consumer);

        // （超懒合金弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_BOW_BLANK)
                .pattern("BAB")
                .pattern("BBB")
                .pattern("NNN")
                .define('B', ModItems.CLEANSING_COMPOUND_BOW_BLANK)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.CLEANSING_COMPOUND_BOW_BLANK))
                .save(consumer);

        // （超懒合金甲胚，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .pattern("BAB")
                .pattern("ABA")
                .pattern("BAB")
                .define('B', ModItems.CLEANSING_COMPOUND_ARMOR_BLANK)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.CLEANSING_COMPOUND_ARMOR_BLANK))
                .save(consumer);

        // （超懒合金剑；A=高级磨剑锭 B=剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_SWORD)
                .pattern("ABA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.ADVANCED_SWORD_TEMPER_INGOT)
                .define('B', ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK)
                .unlockedBy("has_super_lazy_alloy_sword_blank", has(ModItems.SUPER_LAZY_ALLOY_SWORD_BLANK))
                .save(consumer);

        // （修订版）超懒合金弓：高级弓弦锭围边 + 弓胚
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOW)
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.ADVANCED_BOWSTRING_INGOT)
                .define('B', ModItems.SUPER_LAZY_ALLOY_BOW_BLANK)
                .unlockedBy("has_super_lazy_alloy_bow_blank", has(ModItems.SUPER_LAZY_ALLOY_BOW_BLANK))
                .save(consumer);

        // （超懒合金头盔；B=甲胚 A=高级含甲锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_HELMET)
                .pattern("BBB")
                .pattern("ANA")
                .pattern("   ")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK))
                .save(consumer);

        // （超懒合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_CHESTPLATE)
                .pattern("BNB")
                .pattern("BBB")
                .pattern("AAA")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT)
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK))
                .save(consumer);

        // 超懒合金护腿：甲胚满行；高级含甲锭与精制强化米网交错；甲胚与米网交错
        // （用户未写等号，按行首条目直译产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_LEGGINGS)
                .pattern("BBB")
                .pattern("ANA")
                .pattern("BNB")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK))
                .save(consumer);

        // ================= 第五批（靴子/铸造台/召唤器链/Boss 生物蛋/神翼） =================

        // （超懒合金靴子；B=甲胚 A=高级含甲锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_ALLOY_BOOTS)
                .pattern("BAB")
                .pattern("ANA")
                .pattern("N N")
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('A', ModItems.ADVANCED_ARMOR_CORE_INGOT)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK))
                .save(consumer);

        // （铸剑台；P=复合米饭锭 A=饭晶合金锭 W=米木板）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_FORGING_TABLE)
                .pattern(" P ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('W', ModItems.RICE_PLANKS)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （铸甲台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARMOR_FORGING_TABLE)
                .pattern("P  ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('W', ModItems.RICE_PLANKS)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （铸弓台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOW_FORGING_TABLE)
                .pattern("  P")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('W', ModItems.RICE_PLANKS)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （米米之眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_EYE)
                .pattern("RER")
                .pattern("ERE")
                .pattern("RER")
                .define('R', ModItems.RICE)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        // （米米传送器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_TELEPORTER)
                .pattern("RSR")
                .pattern("SDS")
                .pattern("RSR")
                .define('R', ModItems.RICE)
                .define('S', Items.END_STONE)
                .define('D', Items.REINFORCED_DEEPSLATE)
                .unlockedBy("has_end_stone", has(Items.END_STONE))
                .save(consumer);

        // （小型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMALL_SUMMONER)
                .pattern("RDR")
                .pattern("DRD")
                .pattern("RGR")
                .define('R', ModItems.RICE)
                .define('D', Items.REDSTONE)
                .define('G', Items.GLOWSTONE_DUST)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);

        // （中型召唤器）
        // 注意：用户原文行尾产物写成了占位物，但占位物不开放获取，本行产物才是中型召唤器，
        // 且配方含召唤核心（对应中级），故按行首条目以中型召唤器注册
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEDIUM_SUMMONER)
                .pattern("LBL")
                .pattern("BLB")
                .pattern("LCL")
                .define('L', ModItems.RICE_LOG)
                .define('B', Items.REDSTONE_BLOCK)
                .define('C', ModItems.SUMMON_CORE)
                .unlockedBy("has_summon_core", has(ModItems.SUMMON_CORE))
                .save(consumer);

        // （大型召唤器；粗米块 米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LARGE_SUMMONER)
                .pattern("ADA")
                .pattern("SRS")
                .pattern("ACA")
                .define('A', ModItems.COARSE_RICE_BLOCK)
                .define('D', Items.REDSTONE)
                .define('S', Items.SLIME_BALL)
                .define('R', ModItems.RICE_BLOCK)
                .define('C', ModItems.ADVANCED_SUMMON_CORE)
                .unlockedBy("has_advanced_summon_core", has(ModItems.ADVANCED_SUMMON_CORE))
                .save(consumer);

        // （最懒召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_SUMMONER)
                .pattern("RBR")
                .pattern("KRK")
                .pattern("RCR")
                .define('R', ModItems.RICE_BLOCK)
                .define('B', Items.REDSTONE_BLOCK)
                .define('K', Items.SLIME_BLOCK)
                .define('C', ModItems.LAZIEST_SUMMON_CORE)
                .unlockedBy("has_laziest_summon_core", has(ModItems.LAZIEST_SUMMON_CORE))
                .save(consumer);

        // （粗制召唤核心胚；F=可燃米矿石 B=召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_SUMMON_CORE_BLANK)
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_RICE)
                .define('B', ModItems.SUMMON_CORE_BLANK)
                .unlockedBy("has_summon_core_blank", has(ModItems.SUMMON_CORE_BLANK))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_SUMMON_CORE_BLANK), RecipeCategory.MISC,
                        ModItems.SUMMON_CORE, 0.3F, 40)
                .unlockedBy("has_coarse_summon_core_blank", has(ModItems.COARSE_SUMMON_CORE_BLANK))
                .save(consumer);

        // （粗制高级召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK)
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_MIXTURE)
                .define('B', ModItems.ADVANCED_SUMMON_CORE_BLANK)
                .unlockedBy("has_advanced_summon_core_blank", has(ModItems.ADVANCED_SUMMON_CORE_BLANK))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 高级召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK), RecipeCategory.MISC,
                        ModItems.ADVANCED_SUMMON_CORE, 0.3F, 40)
                .unlockedBy("has_coarse_advanced_summon_core_blank", has(ModItems.COARSE_ADVANCED_SUMMON_CORE_BLANK))
                .save(consumer);

        // （混合结晶；棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIXED_CRYSTAL)
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.CRYSTALLIZED_RICE)
                .define('B', ModItems.RECRYSTALLIZED_RICE)
                .unlockedBy("has_crystallized_rice", has(ModItems.CRYSTALLIZED_RICE))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 熔融混合结晶
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.MIXED_CRYSTAL), RecipeCategory.MISC,
                        ModItems.MOLTEN_MIXED_CRYSTAL, 0.3F, 40)
                .unlockedBy("has_mixed_crystal", has(ModItems.MIXED_CRYSTAL))
                .save(consumer);

        // （粗制最懒召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK)
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.COMBUSTIBLE_SOLID)
                .define('B', ModItems.LAZIEST_SUMMON_CORE_BLANK)
                .unlockedBy("has_laziest_summon_core_blank", has(ModItems.LAZIEST_SUMMON_CORE_BLANK))
                .save(consumer);

        // 在熔炉烧 2s（40t）→ 最懒召唤核心
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK), RecipeCategory.MISC,
                        ModItems.LAZIEST_SUMMON_CORE, 0.3F, 40)
                .unlockedBy("has_coarse_laziest_summon_core_blank", has(ModItems.COARSE_LAZIEST_SUMMON_CORE_BLANK))
                .save(consumer);

        // （米米猴王生物蛋；米果黑混液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MONKEY_KING_SPAWN_EGG)
                .pattern("SAS")
                .pattern("AEA")
                .pattern("SAS")
                .define('S', ModItems.REINFORCED_RICE_STICK)
                .define('A', ModItems.RICE_FRUIT_BLACK_MIXTURE)
                .define('E', Items.DRAGON_EGG)
                .unlockedBy("has_dragon_egg", has(Items.DRAGON_EGG))
                .save(consumer);

        // （饭晶虎王生物蛋；米果混黏液 猴王如意棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_TIGER_KING_SPAWN_EGG)
                .pattern("IVI")
                .pattern("VTV")
                .pattern("IVI")
                .define('I', Items.IRON_INGOT)
                .define('V', ModItems.RICE_FRUIT_VISCOUS_MIXTURE)
                .define('T', ModItems.MONKEY_KING_RUYI_STAFF)
                .unlockedBy("has_monkey_king_ruyi_staff", has(ModItems.MONKEY_KING_RUYI_STAFF))
                .save(consumer);

        // （超晶牛王生物蛋；米石 爆米绽放液 虎王利爪）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_COW_KING_SPAWN_EGG)
                .pattern("RLR")
                .pattern("LCL")
                .pattern("RLR")
                .define('R', ModItems.RICE_STONE)
                .define('L', ModItems.POPRICE_BLOOM_LIQUID)
                .define('C', ModItems.TIGER_KING_RAZOR_CLAW)
                .unlockedBy("has_tiger_king_razor_claw", has(ModItems.TIGER_KING_RAZOR_CLAW))
                .save(consumer);

        // （重晶兔王生物蛋；爆米嘬心液 牛王晶角）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG)
                .pattern("RHR")
                .pattern("HOH")
                .pattern("RHR")
                .define('R', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('H', ModItems.POPRICE_HEART_SIP_LIQUID)
                .define('O', ModItems.OX_KING_CRYSTAL_HORN)
                .unlockedBy("has_ox_king_crystal_horn", has(ModItems.OX_KING_CRYSTAL_HORN))
                .save(consumer);

        // （复晶天青蟒生物蛋；眼米嘬心液 兔王腥红眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_AZURE_PYTHON_SPAWN_EGG)
                .pattern("RYR")
                .pattern("YEY")
                .pattern("RYR")
                .define('R', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('Y', ModItems.YANMI_HEART_SIP_LIQUID)
                .define('E', ModItems.RABBIT_KING_BLOODY_RED_EYE)
                .unlockedBy("has_rabbit_king_bloody_red_eye", has(ModItems.RABBIT_KING_BLOODY_RED_EYE))
                .save(consumer);

        // 清洁长命夔生物蛋：龙息四角，米米杂烩液围边，中心极毒青蟒牙
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_LONGEVITY_KUI_SPAWN_EGG)
                .pattern("BMB")
                .pattern("MFM")
                .pattern("BMB")
                .define('B', Items.DRAGON_BREATH)
                .define('M', ModItems.MIMI_MEDLEY_LIQUID)
                .define('F', ModItems.VENOM_AZURE_PYTHON_FANG)
                .unlockedBy("has_venom_azure_python_fang", has(ModItems.VENOM_AZURE_PYTHON_FANG))
                .save(consumer);

        // （最懒终末龙生物蛋）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_FINAL_DRAGON_SPAWN_EGG)
                .pattern("RNR")
                .pattern("NRN")
                .pattern("RNR")
                .define('R', ModItems.SUPER_CRYSTAL)
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(consumer);

        // （米米神翼；米米幻翼膜）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MIMI_DIVINE_WINGS)
                .pattern("AMA")
                .pattern("PEP")
                .pattern("AMA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('M', ModItems.MIMI_MEDLEY_LIQUID)
                .define('P', ModItems.MIMI_PHANTOM_MEMBRANE)
                .define('E', Items.ELYTRA)
                .unlockedBy("has_mimi_phantom_membrane", has(ModItems.MIMI_PHANTOM_MEMBRANE))
                .save(consumer);

        // （米米幻翼膜；超重结晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_PHANTOM_MEMBRANE)
                .pattern("RHR")
                .pattern("SMS")
                .pattern("RHR")
                .define('R', ModItems.RICE)
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL)
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
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SUPER_LAZY_WINGS_CHESTPLATE)
                .pattern(" A ")
                .pattern(" W ")
                .pattern("   ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_CHESTPLATE)
                .define('W', ModItems.MIMI_DIVINE_WINGS)
                .unlockedBy("has_mimi_divine_wings", has(ModItems.MIMI_DIVINE_WINGS))
                .save(consumer);

        // （上古剑意）
        // 判定：mod 无此物品，"紫晶幽匿感应器"=原版校准幽匿感测体 CALIBRATED_SCULK_SENSOR（紫水晶合成的校准版）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCIENT_SWORD_INTENT)
                .pattern("HEH")
                .pattern("ACA")
                .pattern("HEH")
                .define('H', Items.DRAGON_HEAD)
                .define('E', Items.END_CRYSTAL)
                .define('A', ModItems.SUPER_LAZY_ALLOY_ARMOR_BLANK)
                .define('C', Items.CALIBRATED_SCULK_SENSOR)
                .unlockedBy("has_dragon_head", has(Items.DRAGON_HEAD))
                .save(consumer);

        // （超懒神剑·残缺·壹）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_1)
                .pattern("   ")
                .pattern(" SW")
                .pattern("   ")
                .define('S', ModItems.SUPER_LAZY_ALLOY_SWORD)
                .define('W', ModItems.ANCIENT_SWORD_INTENT)
                .unlockedBy("has_ancient_sword_intent", has(ModItems.ANCIENT_SWORD_INTENT))
                .save(consumer);

        // （淬剑台，机器方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_QUENCHING_TABLE)
                .pattern("AAA")
                .pattern("BCB")
                .pattern("BBB")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.RECRYSTALLIZED_RICE)
                .define('C', ModItems.COMBUSTIBLE_SOLID)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // （汲血台，机器方块；T=米棍 W=米木板 L=米原木）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLOODLETTING_TABLE)
                .pattern("STS")
                .pattern("WTW")
                .pattern("LLL")
                .define('S', ModItems.COMBUSTIBLE_SOLID)
                .define('T', ModItems.RICE_STICK)
                .define('W', ModItems.RICE_PLANKS)
                .define('L', ModItems.RICE_LOG)
                .unlockedBy("has_combustible_solid", has(ModItems.COMBUSTIBLE_SOLID))
                .save(consumer);

        // （淬剑液；9=米桶 B=血瓶 L=米果清洁液 R=清洁米沉离液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD_QUENCHING_LIQUID)
                .pattern("VBV")
                .pattern("LBR")
                .pattern("VBV")
                .define('V', ModItems.RICE_BUCKET)
                .define('B', ModItems.BLOOD_VIAL)
                .define('L', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .define('R', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION)
                .unlockedBy("has_blood_vial", has(ModItems.BLOOD_VIAL))
                .save(consumer);

        // （远古剑魄）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCIENT_SWORD_SOUL)
                .pattern("DSD")
                .pattern("SWS")
                .pattern("DSD")
                .define('D', Items.ANCIENT_DEBRIS)
                .define('S', ModItems.COMBUSTIBLE_SOLID)
                .define('W', ModItems.ANCIENT_SWORD_INTENT)
                .unlockedBy("has_ancient_sword_intent", has(ModItems.ANCIENT_SWORD_INTENT))
                .save(consumer);

        // （太古剑魄）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRIMORDIAL_SWORD_SOUL)
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.COMBUSTIBLE_SOLID)
                .define('C', ModItems.ANCIENT_SWORD_SOUL)
                .unlockedBy("has_ancient_sword_soul", has(ModItems.ANCIENT_SWORD_SOUL))
                .save(consumer);

        // （神玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIVINE_JADE)
                .pattern("DDD")
                .pattern("DAD")
                .pattern("DDD")
                .define('D', Items.DRAGON_BREATH)
                .define('A', Items.AMETHYST_SHARD)
                .unlockedBy("has_dragon_breath", has(Items.DRAGON_BREATH))
                .save(consumer);

        // （金米玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_RICE_JADE)
                .pattern("SJS")
                .pattern("RGR")
                .pattern("SJS")
                .define('S', ModItems.CLEANSING_COMPOUND_SWORD_BLANK)
                .define('J', ModItems.DIVINE_JADE)
                .define('R', ModItems.RECRYSTALLIZED_RICE)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_divine_jade", has(ModItems.DIVINE_JADE))
                .save(consumer);

        // （神剑鉴玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIVINE_SWORD_APPRAISAL_JADE)
                .pattern("SJJ")
                .pattern("JBJ")
                .pattern("JJS")
                .define('S', ModItems.COMBUSTIBLE_SOLID)
                .define('J', ModItems.GOLDEN_RICE_JADE)
                .define('B', ModItems.PRIMORDIAL_SWORD_SOUL)
                .unlockedBy("has_golden_rice_jade", has(ModItems.GOLDEN_RICE_JADE))
                .save(consumer);

        // （剑法禁卷；精制强化米网 米书）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FORBIDDEN_SWORD_MANUAL)
                .pattern("TNT")
                .pattern("BBB")
                .pattern("TNT")
                .define('T', Items.GHAST_TEAR)
                .define('N', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('B', ModItems.RICE_BOOK)
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(consumer);

        // （超懒剑法）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_SWORD_ART)
                .pattern("ABA")
                .pattern("ACD")
                .pattern("DDD")
                .define('A', ModItems.GOLDEN_RICE_JADE)
                .define('B', ModItems.FORBIDDEN_SWORD_MANUAL)
                .define('C', ModItems.DIVINE_SWORD_APPRAISAL_JADE)
                .define('D', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_forbidden_sword_manual", has(ModItems.FORBIDDEN_SWORD_MANUAL))
                .save(consumer);

        // （结晶米玉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRYSTALLIZED_RICE_JADE)
                .pattern("SSS")
                .pattern("LJL")
                .pattern("SSS")
                .define('S', ModItems.COMBUSTIBLE_SOLID)
                .define('L', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .define('J', ModItems.GOLDEN_RICE_JADE)
                .unlockedBy("has_golden_rice_jade", has(ModItems.GOLDEN_RICE_JADE))
                .save(consumer);

        // （终末剑诀）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SWORD_CODEX)
                .pattern("HAA")
                .pattern("CBC")
                .pattern("AAH")
                .define('H', Items.DRAGON_HEAD)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('C', ModItems.CRYSTALLIZED_RICE_JADE)
                .define('B', ModItems.SUPER_LAZY_SWORD_ART)
                .unlockedBy("has_crystallized_rice_jade", has(ModItems.CRYSTALLIZED_RICE_JADE))
                .save(consumer);

        // （最懒天堕剑·赝品；超懒神剑·完整）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.SWORD_QUENCHING_TABLE)
                .define('B', ModItems.SUPER_LAZY_DIVINE_SWORD_COMPLETE)
                .define('C', ModItems.FINAL_SWORD_CODEX)
                .unlockedBy("has_final_sword_codex", has(ModItems.FINAL_SWORD_CODEX))
                .save(consumer);

        // （破败之心）
        // 判定：mod 无此物品，"幽匿感应器"=原版幽匿感测体 SCULK_SENSOR；饭晶合金锤
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RUINED_HEART)
                .pattern("ENE")
                .pattern("DSD")
                .pattern("END")
                .define('E', Items.ENDER_EYE)
                .define('N', ModItems.RICE_CRYSTAL_ALLOY_HAMMER)
                .define('D', Items.DRAGON_BREATH)
                .define('S', Items.SCULK_SENSOR)
                .unlockedBy("has_sculk_sensor", has(Items.SCULK_SENSOR))
                .save(consumer);

        // （堕落之心，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_HEART)
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION)
                .define('B', ModItems.RUINED_HEART)
                .unlockedBy("has_ruined_heart", has(ModItems.RUINED_HEART))
                .save(consumer);

        // （堕落王冠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_CROWN)
                .pattern("RGR")
                .pattern("RBR")
                .pattern("AAA")
                .define('R', ModItems.RECRYSTALLIZED_RICE)
                .define('G', Items.EMERALD)
                .define('B', ModItems.RUINED_HEART)
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_ruined_heart", has(ModItems.RUINED_HEART))
                .save(consumer);

        // （七宗堕落）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SEVEN_DEADLY_FALLS)
                .pattern("CBC")
                .pattern("AWA")
                .pattern("NTN")
                .define('C', ModItems.RICE_STEALER_HEART)
                .define('B', ModItems.FALLEN_CROWN)
                .define('A', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION)
                .define('W', ModItems.FINAL_SWORD_CODEX)
                .define('N', Items.NETHER_STAR)
                .define('T', ModItems.FALLEN_HEART)
                .unlockedBy("has_fallen_crown", has(ModItems.FALLEN_CROWN))
                .save(consumer);

        // （最懒天堕剑·原罪）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN)
                .pattern("ACA")
                .pattern("NBN")
                .pattern("ADA")
                .define('A', ModItems.SWORD_QUENCHING_TABLE)
                .define('C', ModItems.FALLEN_CROWN)
                .define('N', Items.NETHER_STAR)
                .define('B', ModItems.SEVEN_DEADLY_FALLS)
                .define('D', ModItems.LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT)
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS))
                .save(consumer);

        // （天堕翼胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HEAVENFALL_WINGS_CHESTPLATE)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ADA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.FALLEN_CROWN)
                .define('C', ModItems.SUPER_LAZY_WINGS_CHESTPLATE)
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS))
                .save(consumer);

        // （最懒天堕头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_HELMET)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.FALLEN_CROWN)
                .define('C', ModItems.SWORD_QUENCHING_TABLE)
                .define('D', ModItems.SUPER_LAZY_ALLOY_HELMET)
                .define('E', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS))
                .save(consumer);

        // （最懒天堕护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_LEGGINGS)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.FALLEN_CROWN)
                .define('C', ModItems.SWORD_QUENCHING_TABLE)
                .define('D', ModItems.SUPER_LAZY_ALLOY_LEGGINGS)
                .define('E', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS))
                .save(consumer);

        // （最懒天堕靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LAZIEST_HEAVENFALL_BOOTS)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.FALLEN_CROWN)
                .define('C', ModItems.SWORD_QUENCHING_TABLE)
                .define('D', ModItems.SUPER_LAZY_ALLOY_BOOTS)
                .define('E', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_seven_deadly_falls", has(ModItems.SEVEN_DEADLY_FALLS))
                .save(consumer);

        // 咧米鸡肉（生）在熔炉烧 2s（40t）→ 熟咧米鸡肉
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.LIEMI_CHICKEN), RecipeCategory.FOOD,
                        ModItems.COOKED_LIEMI_CHICKEN, 0.3F, 40)
                .unlockedBy("has_liemi_chicken", has(ModItems.LIEMI_CHICKEN))
                .save(consumer);

        // ================= 第七批（大杂烩/液体链 + 四系镐） =================

        // （米米大杂烩；五格熟肉+米果+清洁液+腐米肉）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MEDLEY)
                .pattern("RFA")
                .pattern("BCD")
                .pattern("EGH")
                .define('R', ModItems.RICE)
                .define('F', ModItems.RICE_FRUIT)
                .define('A', ModItems.COOKED_LIEMI_CHICKEN)
                .define('B', ModItems.COOKED_MIMOO_BEEF)
                .define('C', ModItems.COOKED_MIMI_MUTTON)
                .define('D', ModItems.COOKED_MIHENG_PORK)
                .define('E', ModItems.COOKED_TANMI_FISH)
                .define('G', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .define('H', ModItems.ROTTEN_RICE_FLESH)
                .unlockedBy("has_rotten_rice_flesh", has(ModItems.ROTTEN_RICE_FLESH))
                .save(consumer);

        // 1 个 米骨 → 3 个 米骨粉（单格物品转多件，必须无序合成）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RICE_BONE_MEAL, 3)
                .requires(ModItems.RICE_BONE)
                .unlockedBy("has_rice_bone", has(ModItems.RICE_BONE))
                .save(consumer);

        // （米精：米骨粉围火米药）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_ESSENCE)
                .pattern(" B ")
                .pattern("BPB")
                .pattern(" B ")
                .define('B', ModItems.RICE_BONE_MEAL)
                .define('P', ModItems.HUOMI_POWDER)
                .unlockedBy("has_rice_bone_meal", has(ModItems.RICE_BONE_MEAL))
                .save(consumer);

        // （粗米；任意肉类=mimiworld:meats 标签）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_RICE)
                .pattern("BMB")
                .define('B', ModItems.RICE_ESSENCE)
                .define('M', ModItemTags.MEATS)
                .unlockedBy("has_rice_essence", has(ModItems.RICE_ESSENCE))
                .save(consumer);

        // （米笔：羽毛/米骨/鱼囊各一排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_BRUSH_PEN)
                .pattern("FFF")
                .pattern("BBB")
                .pattern("CCC")
                .define('F', ModItems.ZHUOMI_FEATHER)
                .define('B', ModItems.RICE_BONE)
                .define('C', ModItems.PENMI_FISH_SAC)
                .unlockedBy("has_rice_bone", has(ModItems.RICE_BONE))
                .save(consumer);

        // （书米笔：丝线+米书+米笔）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BOOK_RICE_PEN)
                .pattern("S  ")
                .pattern("KB ")
                .pattern("   ")
                .define('S', ModItems.NIANMI_SILK_THREAD)
                .define('K', ModItems.RICE_BOOK)
                .define('B', ModItems.RICE_BRUSH_PEN)
                .unlockedBy("has_rice_brush_pen", has(ModItems.RICE_BRUSH_PEN))
                .save(consumer);

        // （米果黑混液：鱼囊围清洁液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_BLACK_MIXTURE)
                .pattern(" C ")
                .pattern("CFC")
                .pattern(" C ")
                .define('C', ModItems.PENMI_FISH_SAC)
                .define('F', ModItems.RICE_FRUIT_CLEANSING_SOLUTION)
                .unlockedBy("has_penmi_fish_sac", has(ModItems.PENMI_FISH_SAC))
                .save(consumer);

        // （米果混黏液：丝线围黑混液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_FRUIT_VISCOUS_MIXTURE)
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.NIANMI_SILK_THREAD)
                .define('B', ModItems.RICE_FRUIT_BLACK_MIXTURE)
                .unlockedBy("has_black_mixture", has(ModItems.RICE_FRUIT_BLACK_MIXTURE))
                .save(consumer);

        // （爆米绽放液：棋盘）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_BLOOM_LIQUID)
                .pattern("PFP")
                .pattern("FBF")
                .pattern("PFP")
                .define('P', ModItems.BAOLIEMI_ROD)
                .define('F', ModItems.HUOMI_POWDER)
                .define('B', ModItems.RICE_FRUIT_VISCOUS_MIXTURE)
                .unlockedBy("has_viscous_mixture", has(ModItems.RICE_FRUIT_VISCOUS_MIXTURE))
                .save(consumer);

        // （爆米嘬心液：狼心围绽放液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPRICE_HEART_SIP_LIQUID)
                .pattern(" W ")
                .pattern("WBW")
                .pattern(" W ")
                .define('W', ModItems.HAOMI_WOLF_HEART)
                .define('B', ModItems.POPRICE_BLOOM_LIQUID)
                .unlockedBy("has_bloom_liquid", has(ModItems.POPRICE_BLOOM_LIQUID))
                .save(consumer);

        // （眼米嘬心液：珍珠围爆米嘬心液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.YANMI_HEART_SIP_LIQUID)
                .pattern(" E ")
                .pattern("EBE")
                .pattern(" E ")
                .define('E', ModItems.YANMI_PEARL)
                .define('B', ModItems.POPRICE_HEART_SIP_LIQUID)
                .unlockedBy("has_poprice_heart_sip_liquid", has(ModItems.POPRICE_HEART_SIP_LIQUID))
                .save(consumer);

        // （米米杂烩液：大杂烩围嘬心液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIMI_MEDLEY_LIQUID)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.MIMI_MEDLEY)
                .define('B', ModItems.YANMI_HEART_SIP_LIQUID)
                .unlockedBy("has_mimi_medley", has(ModItems.MIMI_MEDLEY))
                .save(consumer);

        // （超重结晶镐头：米石+硬化米围超重结晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD)
                .pattern("SAS")
                .pattern("AFA")
                .pattern("SAS")
                .define('S', ModItems.RICE_STONE)
                .define('A', ModItems.HARDENED_RICE)
                .define('F', ModItems.SUPER_HEAVY_CRYSTAL)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （复合米饭镐头：米石+结晶米围复合米饭锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD)
                .pattern("SCS")
                .pattern("CFC")
                .pattern("SCS")
                .define('S', ModItems.RICE_STONE)
                .define('C', ModItems.CRYSTALLIZED_RICE)
                .define('F', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （清洁复合镐头：米石+重结晶米围清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD)
                .pattern("SRS")
                .pattern("RFR")
                .pattern("SRS")
                .define('S', ModItems.RICE_STONE)
                .define('R', ModItems.RECRYSTALLIZED_RICE)
                .define('F', ModItems.CLEANSING_COMPOUND_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
                .save(consumer);

        // （超懒合金镐头：米石+熔融混合结晶围超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD)
                .pattern("SMS")
                .pattern("MFM")
                .pattern("SMS")
                .define('S', ModItems.RICE_STONE)
                .define('M', ModItems.MOLTEN_MIXED_CRYSTAL)
                .define('F', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // 注：-四个手柄为锤米台机器配方，见 ModMachineRecipes（非数据包配方）

        // （超重结晶镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE)
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
                .define('H', ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD)
                .define('F', ModItems.COARSE_RICE_FIBER)
                .define('R', ModItems.SUPER_HEAVY_CRYSTAL_ROD)
                .unlockedBy("has_super_heavy_crystal_pickaxe_head", has(ModItems.SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD))
                .save(consumer);

        // （复合米饭镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COMPOUND_RICE_MEAL_PICKAXE)
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
                .define('H', ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD)
                .define('F', ModItems.REFINED_RICE_FIBER)
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD)
                .unlockedBy("has_compound_rice_meal_pickaxe_head", has(ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD))
                .save(consumer);

        // （清洁复合镐）
        // 注意：用户原文第三行材质与同系列不一致（左下角放复合米饭棒、右下角放复合米饭镐头），
        // 此处严格照原文直译，待用户确认。
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLEANSING_COMPOUND_PICKAXE)
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R E")
                .define('H', ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD)
                .define('F', ModItems.COARSE_RICE_NET)
                .define('R', ModItems.COMPOUND_RICE_MEAL_ROD)
                .define('E', ModItems.COMPOUND_RICE_MEAL_PICKAXE_HEAD)
                .unlockedBy("has_cleansing_compound_pickaxe_head", has(ModItems.CLEANSING_COMPOUND_PICKAXE_HEAD))
                .save(consumer);

        // （超懒合金镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUPER_LAZY_ALLOY_PICKAXE)
                .pattern("HHH")
                .pattern(" FH")
                .pattern("R H")
                .define('H', ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD)
                .define('F', ModItems.REFINED_REINFORCED_RICE_NET)
                .define('R', ModItems.SUPER_LAZY_ALLOY_ROD)
                .unlockedBy("has_super_lazy_alloy_pickaxe_head", has(ModItems.SUPER_LAZY_ALLOY_PICKAXE_HEAD))
                .save(consumer);

        // （最懒天堕镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LAZIEST_HEAVENFALL_PICKAXE)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.FALLEN_CROWN)
                .define('C', ModItems.SWORD_QUENCHING_TABLE)
                .define('D', ModItems.SUPER_LAZY_ALLOY_PICKAXE)
                .unlockedBy("has_super_lazy_alloy_pickaxe", has(ModItems.SUPER_LAZY_ALLOY_PICKAXE))
                .save(consumer);

        // ================= 第八批（盗米者蛋/堕落台/深染链/世界精华机器） =================

        // （盗米者生物蛋）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_STEALER_SPAWN_EGG)
                .pattern("ESE")
                .pattern("RSR")
                .pattern("ESE")
                .define('E', Items.ECHO_SHARD)
                .define('S', Items.SCULK_SENSOR)
                .define('R', ModItems.RICE_BUCKET)
                .unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
                .save(consumer);

        // （堕落台）
        // 注意：目前仍是 placeholder_占位，本配方在 实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_ALTAR)
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.MIMI_MEDLEY_LIQUID)
                .define('D', Items.ANCIENT_DEBRIS)
                .define('B', ModItems.SUPER_LAZY_ALLOY_ARROW)
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(consumer);

        // （堕落十字架）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FALLEN_CROSS)
                .pattern(" C ")
                .pattern("ABA")
                .pattern(" A ")
                .define('C', ModItems.FALLEN_CROWN)
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('B', Items.NETHERITE_SCRAP)
                .unlockedBy("has_fallen_crown", has(ModItems.FALLEN_CROWN))
                .save(consumer);

        // （浸染液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DYEING_SOLUTION)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', ModItems.REMNANT_FALLEN_RICE_ESSENCE)
                .define('B', ModItems.MIMI_MEDLEY_LIQUID)
                .define('C', ModItems.CLEANSING_RICE_SEPARATION_SOLUTION)
                .define('D', ModItems.REMNANT_FALLEN_INGOT)
                .unlockedBy("has_remnant_fallen_ingot", has(ModItems.REMNANT_FALLEN_INGOT))
                .save(consumer);

        // 本系列九个深染部件：染液与残堕锭棋盘围，中心放对应 Boss 材料（为鞘翅类装备→COMBAT）
        // 中心 残天翼
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DEEP_DYED_REMNANT_HEAVEN_WING)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.REMNANT_HEAVEN_WING)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 虚无之触（深染虚无之触）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_VOID_TOUCH)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.VOID_TOUCH)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 残花枝
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_FADED_FLOWER_BRANCH)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.FADED_FLOWER_BRANCH)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 天凤之喙
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.CELESTIAL_PHOENIX_BEAK)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 残堕米精
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.REMNANT_FALLEN_RICE_ESSENCE)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 鲛泪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MERMAID_TEAR)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.MERMAID_TEAR)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 蛟龙爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_JIAOLONG_TALON)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.JIAOLONG_TALON)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 蜃楼骨
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MIRAGE_BONE)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.MIRAGE_BONE)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 精灵帽
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_ELF_HAT)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.ELF_HAT)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // 中心 噬月狼爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.DYEING_SOLUTION)
                .define('B', ModItems.REMNANT_FALLEN_INGOT)
                .define('C', ModItems.MOON_DEVOURER_WOLF_CLAW)
                .unlockedBy("has_dyeing_solution", has(ModItems.DYEING_SOLUTION))
                .save(consumer);

        // （粗制残堕世界精华）
        // 注意：目前仍是 placeholder_占位，本配方在 实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COARSE_REMNANT_FALLEN_WORLD_ESSENCE)
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE)
                .define('B', ModItems.REMNANT_FALLEN_ABYSS_ROCK)
                .unlockedBy("has_deep_dyed_rice_essence", has(ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE))
                .save(consumer);

        // （碾磨台）
        // 注意：为占位，同上。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GRINDING_TABLE)
                .pattern("AAA")
                .pattern("ACA")
                .pattern("BBB")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('C', ModItems.REMNANT_FALLEN_INGOT)
                .define('B', ModItems.REMNANT_FALLEN_ABYSS_ROCK)
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.SUPER_LAZY_ALLOY_INGOT))
                .save(consumer);

        // （杵界台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PESTLE_REALM_TABLE)
                .pattern("AAA")
                .pattern("BCD")
                .pattern("EFE")
                .define('A', ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK)
                .define('B', ModItems.DEEP_DYED_JIAOLONG_TALON)
                .define('C', ModItems.REMNANT_FALLEN_INGOT)
                .define('D', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW)
                .define('E', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('F', ModItems.DEEP_DYED_VOID_TOUCH)
                .unlockedBy("has_polished_abyss_rock", has(ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK))
                .save(consumer);

        // （磨制残堕精华液）
        // 注意：目前仍是 placeholder_占位，本配方在 实装前无法合法合成（按原文直译）。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION)
                .pattern("XYZ")
                .pattern("WWW")
                .pattern("UYV")
                .define('X', ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK)
                .define('Y', ModItems.DEEP_DYED_FADED_FLOWER_BRANCH)
                .define('Z', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE)
                .define('W', ModItems.POLISHED_REMNANT_FALLEN_WORLD_ESSENCE)
                .define('U', ModItems.DEEP_DYED_MIRAGE_BONE)
                .define('V', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW)
                .unlockedBy("has_deep_dyed_phoenix_beak", has(ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK))
                .save(consumer);

        // （凝锭台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INGOT_CONDENSATION_TABLE)
                .pattern("ABA")
                .pattern("BBB")
                .pattern("CCC")
                .define('A', ModItems.DEEP_DYED_REMNANT_HEAVEN_WING)
                .define('B', ModItems.DEEP_DYED_MERMAID_TEAR)
                .define('C', ModItems.REMNANT_FALLEN_INGOT)
                .unlockedBy("has_deep_dyed_heaven_wing", has(ModItems.DEEP_DYED_REMNANT_HEAVEN_WING))
                .save(consumer);

        // （界主令牌）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REALM_LORD_TOKEN)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.LAZIEST_END_DRAGON_HEAD)
                .define('B', ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK)
                .define('C', ModItems.WORLD_TEAR)
                .unlockedBy("has_world_tear", has(ModItems.WORLD_TEAR))
                .save(consumer);

        // ================= 第十批（箭/餐具/饭团/烹饪机器/龙骨装备/世界净化器） =================
        // 注意：本批 8 个产物（大碗、大锅、米纸盘、小煮锅、煮锅、锅盖、终罪骊珠、终罪逆鳞）
        // 目前仍是 placeholder 占位物品，按"严格直译"照原文注册；占位实装前产物无贴图/名称且不进创造栏。
        // 其中大碗是烹饪机器链的公共配料，米纸盘是烤箱链配料——占位实装后整条链自动生效。

        // （超懒合金箭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_LAZY_ALLOY_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_LAZY_ALLOY_INGOT)
                .define('B', ModItems.SUPER_LAZY_ALLOY_ROD)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_super_lazy_alloy_rod", has(ModItems.SUPER_LAZY_ALLOY_ROD))
                .save(consumer);

        // （占位产物；6=米木板）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BIG_BOWL)
                .pattern("   ")
                .pattern("A A")
                .pattern(" A ")
                .define('A', ModItems.RICE_PLANKS)
                .unlockedBy("has_rice_planks", has(ModItems.RICE_PLANKS))
                .save(consumer);

        // （占位产物；米石）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BIG_POT)
                .pattern("   ")
                .pattern("A A")
                .pattern("AAA")
                .define('A', ModItems.RICE_STONE)
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE))
                .save(consumer);

        // （盘子）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PLATE)
                .pattern("   ")
                .pattern("AAA")
                .pattern("   ")
                .define('A', ModItems.RICE_STONE)
                .unlockedBy("has_rice_stone", has(ModItems.RICE_STONE))
                .save(consumer);

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_PAPER_PLATE)
                .pattern("   ")
                .pattern("APA")
                .pattern(" A ")
                .define('A', ModItems.RICE_PLANKS)
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);

        // （天脑饭团；海带片 饭团 熟界天胎花）
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.CELESTIAL_BRAIN_RICE_BALL)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE)
                .define('B', ModItems.ONIGIRI)
                .define('C', ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER)
                .unlockedBy("has_cooked_realm_heaven_embryo_flower", has(ModItems.COOKED_REALM_HEAVEN_EMBRYO_FLOWER))
                .save(consumer);

        // 同上，中心 熟浅鲛尾 = 浅鲛饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.PALE_MERMAID_RICE_BALL)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE)
                .define('B', ModItems.ONIGIRI)
                .define('C', ModItems.COOKED_PALE_MERMAID_TAIL)
                .unlockedBy("has_cooked_pale_mermaid_tail", has(ModItems.COOKED_PALE_MERMAID_TAIL))
                .save(consumer);

        // 中心 熟望米伤鲛足 = 蛟足饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.MERMAID_LEG_RICE_BALL)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE)
                .define('B', ModItems.ONIGIRI)
                .define('C', ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG)
                .unlockedBy("has_cooked_wangmi_wounded_mermaid_leg", has(ModItems.COOKED_WANGMI_WOUNDED_MERMAID_LEG))
                .save(consumer);

        // 中心 熟噬鹫翅 = 鹫翅饭团
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.VULTURE_WING_RICE_BALL)
                .pattern("ABA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', ModItems.KELP_SLICE)
                .define('B', ModItems.ONIGIRI)
                .define('C', ModItems.COOKED_DEVOURER_VULTURE_WING)
                .unlockedBy("has_cooked_devourer_vulture_wing", has(ModItems.COOKED_DEVOURER_VULTURE_WING))
                .save(consumer);

        // ---- 烹饪机器链：R=占位旋钮/部件，H=勺子，P=重型加权压力板，I=铁锭 ----
        // （搅碎器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRUSHER)
                .pattern(" R ")
                .pattern("ISI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('S', Items.IRON_SWORD)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_sword", has(Items.IRON_SWORD))
                .save(consumer);

        // （搅拌器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIXER)
                .pattern(" R ")
                .pattern("ISI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('S', Items.IRON_SHOVEL)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
                .save(consumer);

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMALL_COOKING_POT)
                .pattern(" R ")
                .pattern(" H ")
                .pattern(" P ")
                .define('R', ModItems.POT_LID)
                .define('H', ModItems.SPOON)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON))
                .save(consumer);

        // （占位产物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COOKING_POT)
                .pattern(" R ")
                .pattern("IHI")
                .pattern(" P ")
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON))
                .save(consumer);

        // （大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LARGE_COOKING_POT)
                .pattern(" R ")
                .pattern("IHI")
                .pattern("IPI")
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_spoon", has(ModItems.SPOON))
                .save(consumer);

        // （超大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTRA_LARGE_COOKING_POT)
                .pattern("IRI")
                .pattern("IHI")
                .pattern("IPI")
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('H', ModItems.SPOON)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_large_cooking_pot", has(ModItems.LARGE_COOKING_POT))
                .save(consumer);

        // （巨无霸大大大煮锅）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JUMBO_MEGA_COOKING_POT)
                .pattern("BRB")
                .pattern("BHB")
                .pattern("BPB")
                .define('R', ModItems.POT_LID)
                .define('B', Items.IRON_BLOCK)
                .define('H', ModItems.SPOON)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_extra_large_cooking_pot", has(ModItems.EXTRA_LARGE_COOKING_POT))
                .save(consumer);

        // （铁锅；7=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_WOK)
                .pattern("SR ")
                .pattern("IHI")
                .pattern(" P ")
                .define('S', ModItems.RICE_STICK)
                .define('R', ModItems.POT_LID)
                .define('I', Items.IRON_INGOT)
                .define('H', Items.IRON_SHOVEL)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_iron_shovel", has(Items.IRON_SHOVEL))
                .save(consumer);

        // （烤箱；占位）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OVEN)
                .pattern("PRP")
                .pattern("PRP")
                .pattern("CRC")
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('R', ModItems.RICE_PAPER_PLATE)
                .define('C', Items.CHARCOAL)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL))
                .save(consumer);

        // （占位产物；机器链公共部件）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POT_LID)
                .pattern(" W ")
                .pattern(" P ")
                .pattern("   ")
                .define('W', ItemTags.WOODEN_BUTTONS)
                .define('P', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("has_heavy_weighted_pressure_plate", has(Items.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer);

        // （勺子）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPOON)
                .pattern("S  ")
                .pattern("S I")
                .pattern("II ")
                .define('S', ModItems.RICE_STICK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_rice_stick", has(ModItems.RICE_STICK))
                .save(consumer);

        // ---- 龙骨系列（龙血 / 龙鳞 / 龙骨 / 七宗堕落） ----
        // （占位产物；骊珠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_LI_PEARL)
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD)
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('X', ModItems.LI_PEARL)
                .unlockedBy("has_li_pearl", has(ModItems.LI_PEARL))
                .save(consumer);

        // 同上，中心 天虚棘脑汤 = 占位 mmjm 产物
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_REVERSE_SCALE)
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD)
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('X', ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP)
                .unlockedBy("has_celestial_void_thorn_brain_soup", has(ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP))
                .save(consumer);

        // 同上，中心 龙鳞 = 终罪龙心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_DRAGON_HEART)
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD)
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('X', ModItems.DRAGON_SCALE)
                .unlockedBy("has_dragon_scale", has(ModItems.DRAGON_SCALE))
                .save(consumer);

        // 同上，中心 宇罪龙头 = 终罪龙头
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_DRAGON_HEAD)
                .pattern("ABC")
                .pattern("DXD")
                .pattern("ABC")
                .define('A', ModItems.DRAGON_BLOOD)
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('X', ModItems.YU_SIN_DRAGON_HEAD)
                .unlockedBy("has_yu_sin_dragon_head", has(ModItems.YU_SIN_DRAGON_HEAD))
                .save(consumer);

        // （残堕宗罪头盔；COMBAT）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_HELMET)
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('A', ModItems.DRAGON_BLOOD)
                .define('X', ModItems.LAZIEST_HEAVENFALL_HELMET)
                .unlockedBy("has_laziest_heavenfall_helmet", has(ModItems.LAZIEST_HEAVENFALL_HELMET))
                .save(consumer);

        // 中心 天堕翼胸甲 = 残堕宗罪胸甲；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_CHESTPLATE)
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('A', ModItems.DRAGON_BLOOD)
                .define('X', ModItems.HEAVENFALL_WINGS_CHESTPLATE)
                .unlockedBy("has_heavenfall_wings_chestplate", has(ModItems.HEAVENFALL_WINGS_CHESTPLATE))
                .save(consumer);

        // 中心 天堕护腿 = 残堕宗罪护腿；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_LEGGINGS)
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('A', ModItems.DRAGON_BLOOD)
                .define('X', ModItems.LAZIEST_HEAVENFALL_LEGGINGS)
                .unlockedBy("has_laziest_heavenfall_leggings", has(ModItems.LAZIEST_HEAVENFALL_LEGGINGS))
                .save(consumer);

        // 中心 天堕靴子 = 残堕宗罪靴子；COMBAT
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_BOOTS)
                .pattern("BCB")
                .pattern("AXA")
                .pattern("BCB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.DRAGON_BONE)
                .define('A', ModItems.DRAGON_BLOOD)
                .define('X', ModItems.LAZIEST_HEAVENFALL_BOOTS)
                .unlockedBy("has_laziest_heavenfall_boots", has(ModItems.LAZIEST_HEAVENFALL_BOOTS))
                .save(consumer);

        // （残堕宗罪之弓；COMBAT；超懒合金弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_CARDINAL_SIN_BOW)
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BEB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.YU_SIN_DRAGON_HEAD)
                .define('D', ModItems.SUPER_LAZY_ALLOY_BOW)
                .define('E', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_super_lazy_alloy_bow", has(ModItems.SUPER_LAZY_ALLOY_BOW))
                .save(consumer);

        // （残堕崩解之镐；COMBAT；终罪龙心 最懒天堕镐）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_DISINTEGRATION_PICKAXE)
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BEB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.FINAL_SIN_DRAGON_HEART)
                .define('D', ModItems.LAZIEST_HEAVENFALL_PICKAXE)
                .define('E', ModItems.SEVEN_DEADLY_FALLS)
                .unlockedBy("has_laziest_heavenfall_pickaxe", has(ModItems.LAZIEST_HEAVENFALL_PICKAXE))
                .save(consumer);

        // （残堕逆鳞之剑；COMBAT；宇罪龙逆鳞 最懒天堕剑·原罪）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REMNANT_FALLEN_REVERSE_SCALE_SWORD)
                .pattern("BCB")
                .pattern("BDB")
                .pattern("BBB")
                .define('B', ModItems.DRAGON_SCALE)
                .define('C', ModItems.YU_SIN_DRAGON_REVERSE_SCALE)
                .define('D', ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN)
                .unlockedBy("has_laziest_heavenfall_sword_original_sin", has(ModItems.LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN))
                .save(consumer);

        // （龙鳞之威；龙骨架构 宇罪龙逆鳞）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_SCALE_MIGHT)
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DCD")
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('C', ModItems.DRAGON_BONE_FRAMEWORK)
                .define('B', ModItems.YU_SIN_DRAGON_REVERSE_SCALE)
                .unlockedBy("has_dragon_bone_framework", has(ModItems.DRAGON_BONE_FRAMEWORK))
                .save(consumer);

        // （修订版）龙元：七罪陨围四角与上下边中点，龙血精华围边，中心骊珠，底边中心深染噬月狼爪
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_YUAN)
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DED")
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE)
                .define('B', ModItems.LI_PEARL)
                .define('E', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW)
                .unlockedBy("has_li_pearl", has(ModItems.LI_PEARL))
                .save(consumer);

        // （龙首之怒；龙鳞块 宇罪龙头）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRAGON_HEAD_WRATH)
                .pattern("DCD")
                .pattern("CBC")
                .pattern("DCD")
                .define('D', ModItems.SEVEN_DEADLY_FALLS)
                .define('C', ModItems.DRAGON_SCALE_BLOCK)
                .define('B', ModItems.YU_SIN_DRAGON_HEAD)
                .unlockedBy("has_yu_sin_dragon_head", has(ModItems.YU_SIN_DRAGON_HEAD))
                .save(consumer);

        // （终罪界心；界主龙心）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FINAL_SIN_REALM_HEART)
                .pattern(" A ")
                .pattern("CBD")
                .pattern("   ")
                .define('A', ModItems.DRAGON_BONE_FRAMEWORK)
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE)
                .define('B', ModItems.REALM_LORD_DRAGON_HEART)
                .define('D', ModItems.DRAGON_SCALE_BLOCK)
                .unlockedBy("has_realm_lord_dragon_heart", has(ModItems.REALM_LORD_DRAGON_HEART))
                .save(consumer);

        // （净化架构）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PURIFICATION_FRAMEWORK)
                .pattern("A C")
                .pattern("   ")
                .pattern("   ")
                .define('A', ModItems.DRAGON_BONE_FRAMEWORK)
                .define('C', ModItems.DRAGON_BLOOD_ESSENCE)
                .unlockedBy("has_dragon_blood_essence", has(ModItems.DRAGON_BLOOD_ESSENCE))
                .save(consumer);

        // （世界净化器；宗罪龙头冠）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WORLD_PURIFIER)
                .pattern("ABA")
                .pattern("CDE")
                .pattern("AFA")
                .define('A', ModItems.PURIFICATION_FRAMEWORK)
                .define('B', ModItems.DRAGON_SCALE_MIGHT)
                .define('C', ModItems.DRAGON_YUAN)
                .define('D', ModItems.CARDINAL_SIN_DRAGON_CROWN)
                .define('E', ModItems.DRAGON_HEAD_WRATH)
                .define('F', ModItems.FINAL_SIN_REALM_HEART)
                .unlockedBy("has_cardinal_sin_dragon_crown", has(ModItems.CARDINAL_SIN_DRAGON_CROWN))
                .save(consumer);

        // ---- 箭系列：竖列 材料 / 棒 / 米石 ----
        // （饭晶箭；结晶饭 米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CRYSTALLIZED_RICE_MEAL)
                .define('B', ModItems.RICE_BATON)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.CRYSTALLIZED_RICE_MEAL))
                .save(consumer);

        // （超结晶箭；超结晶 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_CRYSTAL_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_CRYSTAL)
                .define('B', ModItems.REINFORCED_RICE_STICK)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_super_crystal", has(ModItems.SUPER_CRYSTAL))
                .save(consumer);

        // （饭晶合金箭；饭晶合金锭 7=米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RICE_CRYSTAL_ALLOY_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.RICE_CRYSTAL_ALLOY_INGOT)
                .define('B', ModItems.RICE_STICK)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.RICE_CRYSTAL_ALLOY_INGOT))
                .save(consumer);

        // （超重结晶箭；超重结晶 超重结晶棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SUPER_HEAVY_CRYSTAL_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.SUPER_HEAVY_CRYSTAL)
                .define('B', ModItems.SUPER_HEAVY_CRYSTAL_ROD)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_super_heavy_crystal", has(ModItems.SUPER_HEAVY_CRYSTAL))
                .save(consumer);

        // （复合米饭箭；复合米饭锭 复合米饭棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOUND_RICE_MEAL_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.COMPOUND_RICE_MEAL_INGOT)
                .define('B', ModItems.COMPOUND_RICE_MEAL_ROD)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.COMPOUND_RICE_MEAL_INGOT))
                .save(consumer);

        // （清洁复合箭；清洁复合锭 清洁复合棒）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLEANSING_COMPOUND_ARROW)
                .pattern(" A ")
                .pattern(" B ")
                .pattern(" C ")
                .define('A', ModItems.CLEANSING_COMPOUND_INGOT)
                .define('B', ModItems.CLEANSING_COMPOUND_ROD)
                .define('C', ModItems.RICE_STONE)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.CLEANSING_COMPOUND_INGOT))
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
