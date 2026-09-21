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
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import java.util.function.Consumer;

/**
 * 米米世界配方数据生成器。运行 gradlew runData 后，配方 JSON 生成到
 * src/generated/resources/data/mimiworld/recipes/（该目录已在 build.gradle 中加入资源源集）。
 * 配方里的本模物品统一用 ModItems.byCode(编号) 引用。
 *
 * ============================================================================
 * 【配方书写规则（用户约定，必须严格遵守）】
 * ----------------------------------------------------------------------------
 * 1. 编号 0 = 工作台空格；其他数字 = ModItems 自动编号（与编码表一致）。
 * 2. 工作台配方一律为【有序合成（crafting_shaped）】，写法为 9 个编号用空格连接，
 *    按【先行后列】排列，对应 3×3 网格：前 3 个 = 第一行，中 3 个 = 第二行，
 *    后 3 个 = 第三行；右侧 "=输出编号"（可附产出数量，默认 1）。
 *    例：0 0 0 0 1 1 0 1 1 = 3
 *        → 第一行 "   "、第二行 " RR"、第三行 " RR"（R=1 号米）→ 产物 3 号米球。
 *    （有序图案经原版去空行空列归一化，整体平移到网格角落不影响匹配。）
 *    例外（原版硬约束，runData 实测）：当图案归一化后【只占 1×1 一格】（如单个物品转 N 个）时，
 *    ShapedRecipeBuilder 会抛 "only takes in a single item" 异常，必须改用 ShapelessRecipeBuilder；
 *    单材料但占多格（如 3 格 L 形、四角、3×3 填满）仍可正常有序注册。
 * 3. “原版XX” = 原版物品（如 原版水桶=Items.WATER_BUCKET、原版红石=Items.REDSTONE）。
 *    同名物品靠药水效果区分（瞬间伤害/虚弱/跳跃/治疗/力量/速度药水等）时不能直接用 Items.POTION
 *    （Ingredient.of(ItemStack) 会丢 NBT，任意药水都满足），必须用 potionIngredient(Potions.XXX)，
 *    内部走 Forge StrictNBTIngredient.of(...) 精确匹配；
 *    工作台消耗药水后原版会按合成剩余物返还玻璃瓶，同理水桶返还空桶。
 * 4. “任意树苗/任意木板”等“任意XX” = 原版同名物品标签（ItemTags.SAPLINGS / PLANKS）。
 * 5. 熔炉语句格式：「输入编号 使用熔炉烧制 N s 可得 输出编号」，时间换算 1s = 20 ticks
 *    （1s=20t、3s=60t、4s=80t）。高炉若保留同名烧炼，时间按惯例为熔炉一半。
 * 6. 工作台之外的工作方块（锤米台、压饭机等）不是数据包配方，不能写在本类；
 *    须在对应方块实体类的静态配方表注册，统一入口见 ModMachineRecipes。
 * 7. 配方 ID：默认等于产物注册名；只有“同一产物存在多条配方”时才必须显式指定不同 ID。
 *    【产物是原版物品时例外】：默认 ID 会落到 minecraft 命名空间（无效/冲突），
 *    必须显式 save(consumer, MimiWorld.MODID + ":产物名")（如原版龙蛋 mimiworld:dragon_egg）。
 * 8. “修改NN”=覆盖已注册的第 NN 条配方（用户给新写法即替换旧写法）；
 *    行首编号与“=输出”冲突时，结合编码表语义判定（占位物 placeholder 永不开放合成），
 *    并在注释与汇报中显式标明判定依据。
 * ============================================================================
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // ================= 熔炉烧炼 =================

        // #2：1 号 米 → 2 号 熟米，用户指定烧制 1s（20t），经验 0.5
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(1)), RecipeCategory.FOOD, ModItems.byCode(2), 0.5F, 20)
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                .save(consumer, MimiWorld.MODID + ":rice_smelting");

        // #4：3 号 米球 → 4 号 熟米球，用户指定烧制 4s（80t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(3)), RecipeCategory.FOOD, ModItems.byCode(4), 0.3F, 80)
                .unlockedBy("has_rice_ball", has(ModItems.byCode(3)))
                .save(consumer);

        // #16：17 号 结晶米矿石 → 16 号 结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(17)), RecipeCategory.MISC, ModItems.byCode(16), 0.7F, 60)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.byCode(17)))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(17)), RecipeCategory.MISC, ModItems.byCode(16), 0.7F, 30)
                .unlockedBy("has_crystallized_rice_ore", has(ModItems.byCode(17)))
                .save(consumer, MimiWorld.MODID + ":crystallized_rice_blasting");

        // #19：18 号 重结晶米矿石 → 19 号重结晶米，用户指定熔炉 3s（60t），经验 0.7；高炉 30t
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(18)), RecipeCategory.MISC, ModItems.byCode(19), 0.7F, 60)
                .unlockedBy("has_recrystallized_rice_ore", has(ModItems.byCode(18)))
                .save(consumer, MimiWorld.MODID + ":recrystallized_rice_smelting");

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(18)), RecipeCategory.MISC, ModItems.byCode(19), 0.7F, 30)
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

        // ================= 工作台有序合成 =================

        // #3：0 0 0 / 0 1 1 / 0 1 1 = 3（4 个 1 号米在右下 2×2 → 3 号米球）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(3))
                .pattern("   ")
                .pattern(" RR")
                .pattern(" RR")
                .define('R', ModItems.byCode(1))
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                .save(consumer);

        // #7：0 0 0 / 0 6 0 / 0 6 0 = 7（两个 6 号米木板竖排 → 7 号米木棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(7))
                .pattern("   ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', ModItems.byCode(6))
                .unlockedBy("has_rice_planks", has(ModItems.byCode(6)))
                .save(consumer);

        // #9：28 28 28 / 28 原版水桶 28 / 28 28 28 = 9（8 个 28 号米块围桶 → 9 号米桶；桶按原版机制返还空桶）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.byCode(9))
                .pattern("BBB")
                .pattern("BWB")
                .pattern("BBB")
                .define('B', ModItems.byCode(28))
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_block", has(ModItems.byCode(28)))
                .save(consumer);

        // #10：0 0 0 / 0 1 任意树苗 / 0 0 0 = 10（1 号米 + 任意树苗 → 10 号米树苗）
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.byCode(10))
                .pattern("   ")
                .pattern(" RS")
                .pattern("   ")
                .define('R', ModItems.byCode(1))
                .define('S', ItemTags.SAPLINGS)
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                .save(consumer);

        // #23：6 6 6 / 24 24 24 / 6 6 6 = 23（米木板-米棍-米木板三层 → 23 号强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(23))
                .pattern("PPP")
                .pattern("QQQ")
                .pattern("PPP")
                .define('P', ModItems.byCode(6))
                .define('Q', ModItems.byCode(24))
                .unlockedBy("has_rice_baton", has(ModItems.byCode(24)))
                .save(consumer);

        // #24：0 0 0 / 25 原版水桶 25 / 0 0 0 = 24（两个 25 号米粉夹水桶 → 24 号米棍；空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(24))
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.byCode(25))
                .define('W', Items.WATER_BUCKET)
                .unlockedBy("has_rice_flour", has(ModItems.byCode(25)))
                .save(consumer);

        // #25：一个 2 号熟米在工作台即可合成 8 个 25 号米粉。
        // 单材料配方原版禁止有序注册（无形状可言），按规则注释例外用无序，产出 8 个。
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.byCode(25), 8)
                .requires(ModItems.byCode(2))
                .unlockedBy("has_cooked_rice", has(ModItems.byCode(2)))
                .save(consumer);

        // #26：小麦 羊肉 小麦 / 牛肉 小麦 鸡肉 / 小麦 猪肉 小麦 = 26（26 号粗米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(26))
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

        // #27：皮革 线 皮革 / 纸 皮革 纸 / 任意木板 ×3 = 27（27 号造米机）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(27))
                .pattern("LSL")
                .pattern("RLR")
                .pattern("PPP")
                .define('L', Items.LEATHER)
                .define('S', Items.STRING)
                .define('R', Items.PAPER)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(consumer);

        // #28：1×9 填满 1 号米 = 28（28 号米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(28))
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .define('R', ModItems.byCode(1))
                .unlockedBy("has_rice", has(ModItems.byCode(1)))
                .save(consumer);

        // #29：5 5 5 / 原版红石 36 原版红石 / 5 5 5 = 29（米原木夹 36 号饭晶锤 → 29 号锤米台）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(29))
                .pattern("LLL")
                .pattern("RHR")
                .pattern("LLL")
                .define('L', ModItems.byCode(5))
                .define('R', Items.REDSTONE)
                .define('H', ModItems.byCode(36))
                .unlockedBy("has_rice_crystal_hammer", has(ModItems.byCode(36)))
                .save(consumer);

        // #31：5 5 5 / 28 原版红石 28 / 5 5 5 = 31（米原木与 28 号米块夹红石 → 31 号压饭机）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(31))
                .pattern("LLL")
                .pattern("BRB")
                .pattern("LLL")
                .define('L', ModItems.byCode(5))
                .define('B', ModItems.byCode(28))
                .define('R', Items.REDSTONE)
                .unlockedBy("has_rice_block", has(ModItems.byCode(28)))
                .save(consumer);

        // #33：32 下界合金锭 32 / 下界合金锭 32 下界合金锭 / 32 下界合金锭 32 = 33
        // （5 个 32 号压缩饭桶与 4 个下界合金锭交错 → 33 号粗制结晶饭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(33))
                .pattern("ANA")
                .pattern("NAN")
                .pattern("ANA")
                .define('A', ModItems.byCode(32))
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compressed_rice_pail", has(ModItems.byCode(32)))
                .save(consumer);

        // ================= 第二批：#34-#64（草麻/网/超结晶/机器/米果线） =================

        // ---------- 熔炉烧炼（1s=20t，时长严格按用户秒数） ----------

        // #34：33 号粗制结晶饭 熔炉 6s（120t）→ 34 号结晶饭
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(33)), RecipeCategory.MISC, ModItems.byCode(34), 0.3F, 120)
                .unlockedBy("has_coarse_crystallized_rice_meal", has(ModItems.byCode(33)))
                .save(consumer);

        // #38：37 号湿草麻 熔炉 2s（40t）→ 38 号粗制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(37)), RecipeCategory.MISC, ModItems.byCode(38), 0.3F, 40)
                .unlockedBy("has_wet_grass_fiber", has(ModItems.byCode(37)))
                .save(consumer);

        // #40：39 号强化湿草麻 熔炉 2s（40t）→ 40 号精制米麻
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(39)), RecipeCategory.MISC, ModItems.byCode(40), 0.3F, 40)
                .unlockedBy("has_reinforced_wet_grass_fiber", has(ModItems.byCode(39)))
                .save(consumer);

        // #43：42 号粗制湿草网 熔炉 2s（40t）→ 43 号粗制米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(42)), RecipeCategory.MISC, ModItems.byCode(43), 0.3F, 40)
                .unlockedBy("has_coarse_wet_grass_net", has(ModItems.byCode(42)))
                .save(consumer);

        // #46：45 号精制强化湿草网 熔炉 2s（40t）→ 46 号精制强化米网
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(45)), RecipeCategory.MISC, ModItems.byCode(46), 0.3F, 40)
                .unlockedBy("has_refined_reinforced_wet_grass_net", has(ModItems.byCode(45)))
                .save(consumer);

        // #53：52 号米矿石 熔炉 2s（40t）→ 53 号米石（矿石，经验 0.7；高炉 20t）
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(52)), RecipeCategory.MISC, ModItems.byCode(53), 0.7F, 40)
                .unlockedBy("has_rice_ore", has(ModItems.byCode(52)))
                .save(consumer);
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.byCode(52)), RecipeCategory.MISC, ModItems.byCode(53), 0.7F, 20)
                .unlockedBy("has_rice_ore", has(ModItems.byCode(52)))
                .save(consumer, MimiWorld.MODID + ":rice_stone_blasting");

        // #59：58 号粗制复合米饭锭 熔炉 2s（40t）→ 59 号复合米饭板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(58)), RecipeCategory.MISC, ModItems.byCode(59), 0.3F, 40)
                .unlockedBy("has_coarse_compound_rice_meal_ingot", has(ModItems.byCode(58)))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // #35：下界合金锭 34 下界合金锭 / 34 金锭 34 / 下界合金锭 34 下界合金锭 = 35（饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(35))
                .pattern("NAN")
                .pattern("AGA")
                .pattern("NAN")
                .define('A', ModItems.byCode(34))
                .define('N', Items.NETHERITE_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.byCode(34)))
                .save(consumer);

        // #36：34 40 34 / 34 23 34 / 0 23 0 = 36（饭晶锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.byCode(36))
                .pattern("AFA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.byCode(34))
                .define('F', ModItems.byCode(40))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_refined_rice_fiber", has(ModItems.byCode(40)))
                .save(consumer);

        // #37：0 0 0 / 0 14 14 / 0 14 0 = 37（3 个 14 号米草 L 形 → 湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(37))
                .pattern("   ")
                .pattern(" GG")
                .pattern(" G ")
                .define('G', ModItems.byCode(14))
                .unlockedBy("has_rice_grass", has(ModItems.byCode(14)))
                .save(consumer);

        // #39：0 0 0 / 16 37 16 / 0 0 0 = 39（强化湿草麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(39))
                .pattern("   ")
                .pattern("CWC")
                .pattern("   ")
                .define('C', ModItems.byCode(16))
                .define('W', ModItems.byCode(37))
                .unlockedBy("has_wet_grass_fiber", has(ModItems.byCode(37)))
                .save(consumer);

        // #41：37 0 37 / 0 0 0 / 37 0 37 = 41（4 个湿草麻四角 → 湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(41))
                .pattern("W W")
                .pattern("   ")
                .pattern("W W")
                .define('W', ModItems.byCode(37))
                .unlockedBy("has_wet_grass_fiber", has(ModItems.byCode(37)))
                .save(consumer);

        // #42：0 0 0 / 38 41 38 / 0 0 0 = 42（粗制湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(42))
                .pattern("   ")
                .pattern("FWF")
                .pattern("   ")
                .define('F', ModItems.byCode(38))
                .define('W', ModItems.byCode(41))
                .unlockedBy("has_wet_grass_net", has(ModItems.byCode(41)))
                .save(consumer);

        // #44：0 0 0 / 19 41 19 = 44（用户只给两行，第三行全空；强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(44))
                .pattern("   ")
                .pattern("RWR")
                .pattern("   ")
                .define('R', ModItems.byCode(19))
                .define('W', ModItems.byCode(41))
                .unlockedBy("has_wet_grass_net", has(ModItems.byCode(41)))
                .save(consumer);

        // #45：0 0 0 / 40 44 40 / 0 0 0 = 45（精制强化湿草网）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(45))
                .pattern("   ")
                .pattern("FOF")
                .pattern("   ")
                .define('F', ModItems.byCode(40))
                .define('O', ModItems.byCode(44))
                .unlockedBy("has_reinforced_wet_grass_net", has(ModItems.byCode(44)))
                .save(consumer);

        // #47：35 46 35 / 35 23 35 / 0 23 0 = 47（饭晶合金锤）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.byCode(47))
                .pattern("AHA")
                .pattern("ASA")
                .pattern(" S ")
                .define('A', ModItems.byCode(35))
                .define('H', ModItems.byCode(46))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #49：6 6 6 / 原版红石 47 原版红石 / 6 6 6 = 49（高级锤米台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(49))
                .pattern("PPP")
                .pattern("RHR")
                .pattern("PPP")
                .define('P', ModItems.byCode(6))
                .define('R', Items.REDSTONE)
                .define('H', ModItems.byCode(47))
                .unlockedBy("has_rice_crystal_alloy_hammer", has(ModItems.byCode(47)))
                .save(consumer);

        // #50：53 原版红石 53 / 金锭 53 金锭 / 53 原版红石 53 = 50（粗超结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(50))
                .pattern("SRS")
                .pattern("GSG")
                .pattern("SRS")
                .define('S', ModItems.byCode(53))
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_rice_stone", has(ModItems.byCode(53)))
                .save(consumer);

        // #54：9 个 51 号超结晶填满 = 54（超结晶块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(54))
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', ModItems.byCode(51))
                .unlockedBy("has_super_crystal", has(ModItems.byCode(51)))
                .save(consumer);

        // #55：35 钻石 35 / 54 下界合金锭 54 / 35 钻石 35 = 55（超重结晶；用户未写等号按条目号推断）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(55))
                .pattern("ADA")
                .pattern("BNB")
                .pattern("ADA")
                .define('A', ModItems.byCode(35))
                .define('D', Items.DIAMOND)
                .define('B', ModItems.byCode(54))
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_super_crystal_block", has(ModItems.byCode(54)))
                .save(consumer);

        // #56：81 81 81 / 原版红石 47 原版红石 / 5 5 5 = 56（电离台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(56))
                .pattern("TTT")
                .pattern("RHR")
                .pattern("LLL")
                .define('T', ModItems.byCode(81))
                .define('R', Items.REDSTONE)
                .define('H', ModItems.byCode(47))
                .define('L', ModItems.byCode(5))
                .unlockedBy("has_rice_stone_block", has(ModItems.byCode(81)))
                .save(consumer);

        // #57：46 46 46 / 0 金锭 0 / 81 81 81 = 57（沉淀台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(57))
                .pattern("NNN")
                .pattern(" G ")
                .pattern("TTT")
                .define('N', ModItems.byCode(46))
                .define('G', Items.GOLD_INGOT)
                .define('T', ModItems.byCode(81))
                .unlockedBy("has_refined_reinforced_rice_net", has(ModItems.byCode(46)))
                .save(consumer);

        // #58：55 35 55 / 35 55 35 / 55 35 55 = 58（粗制复合米饭锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(58))
                .pattern("AXA")
                .pattern("XAX")
                .pattern("AXA")
                .define('A', ModItems.byCode(55))
                .define('X', ModItems.byCode(35))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #60：59 59 59 / 36 钻石 36 / 59 59 59 = 60（复合米饭锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(60))
                .pattern("PPP")
                .pattern("HDH")
                .pattern("PPP")
                .define('P', ModItems.byCode(59))
                .define('H', ModItems.byCode(36))
                .define('D', Items.DIAMOND)
                .unlockedBy("has_compound_rice_meal_plate", has(ModItems.byCode(59)))
                .save(consumer);

        // #61：15 15 15 / 15 36 15 / 15 15 15 = 61（8 个 15 号米果围 36 号饭晶锤 → 米果渣）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(61))
                .pattern("FFF")
                .pattern("FHF")
                .pattern("FFF")
                .define('F', ModItems.byCode(15))
                .define('H', ModItems.byCode(36))
                .unlockedBy("has_rice_fruit", has(ModItems.byCode(15)))
                .save(consumer);

        // #63：瞬间伤害 虚弱 跳跃 / 9 62 30 / 治疗 力量 速度 = 63（米果混合液）
        // 六种原版药水用 potionIngredient 按效果区分；合成后原版返还 6 个玻璃瓶。
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(63))
                .pattern("HWL")
                .pattern("KJP")
                .pattern("TSF")
                .define('H', potionIngredient(Potions.HARMING))
                .define('W', potionIngredient(Potions.WEAKNESS))
                .define('L', potionIngredient(Potions.LEAPING))
                .define('K', ModItems.byCode(9))
                .define('J', ModItems.byCode(62))
                .define('P', ModItems.byCode(30))
                .define('T', potionIngredient(Potions.HEALING))
                .define('S', potionIngredient(Potions.STRENGTH))
                .define('F', potionIngredient(Potions.SWIFTNESS))
                .unlockedBy("has_rice_fruit_juice", has(ModItems.byCode(62)))
                .save(consumer);

        // #64：0 55 0 / 65 36 65 / 6 6 6 = 64（离解台方块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(64))
                .pattern(" A ")
                .pattern("BHB")
                .pattern("PPP")
                .define('A', ModItems.byCode(55))
                .define('B', ModItems.byCode(65))
                .define('H', ModItems.byCode(36))
                .define('P', ModItems.byCode(6))
                .unlockedBy("has_rice_book", has(ModItems.byCode(65)))
                .save(consumer);

        // ================= 第三批：#48-#96（装备/清洁/超懒合金/可燃线） =================

        // ---------- 熔炉烧炼（均 2s=40t，经验 0.3） ----------

        // #70：68 号米果复合液 熔炉 2s（40t）→ 70 号米果混合渣
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(68)), RecipeCategory.MISC, ModItems.byCode(70), 0.3F, 40)
                .unlockedBy("has_rice_fruit_compound_solution", has(ModItems.byCode(68)))
                .save(consumer);

        // #73：72 号粗制清洁复合锭 熔炉 2s（40t）→ 73 号清洁复合碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(72)), RecipeCategory.MISC, ModItems.byCode(73), 0.3F, 40)
                .unlockedBy("has_coarse_cleansing_compound_ingot", has(ModItems.byCode(72)))
                .save(consumer);

        // #74：73 号清洁复合碎块 熔炉 2s（40t）→ 74 号清洁复合板
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(73)), RecipeCategory.MISC, ModItems.byCode(74), 0.3F, 40)
                .unlockedBy("has_cleansing_compound_chunk", has(ModItems.byCode(73)))
                .save(consumer);

        // #79：78 号超懒合金粉 熔炉 2s（40t）→ 79 号超懒合金碎块
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(78)), RecipeCategory.MISC, ModItems.byCode(79), 0.3F, 40)
                .unlockedBy("has_super_lazy_alloy_dust", has(ModItems.byCode(78)))
                .save(consumer);

        // #83：82 号可燃混合液 熔炉 2s（40t）→ 83 号可燃固态物
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.byCode(82)), RecipeCategory.MISC, ModItems.byCode(83), 0.3F, 40)
                .unlockedBy("has_combustible_mixture", has(ModItems.byCode(82)))
                .save(consumer);

        // ---------- 工作台有序合成 ----------

        // #48：0 34 24 / 34 38 24 / 0 34 24 = 48（饭晶弓；A=34 结晶饭 F=38 粗制米麻 S=24 米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(48))
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.byCode(34))
                .define('F', ModItems.byCode(38))
                .define('S', ModItems.byCode(24))
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.byCode(34)))
                .save(consumer);

        // #65（配方一）：0 0 0 / 0 46 0 / 66 66 66 = 65（米书；精制强化米网 + 3 张米纸）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(65))
                .pattern("   ")
                .pattern(" N ")
                .pattern("PPP")
                .define('N', ModItems.byCode(46))
                .define('P', ModItems.byCode(66))
                .unlockedBy("has_rice_paper", has(ModItems.byCode(66)))
                .save(consumer);

        // #66（用户 2026-09-21 修订：产物从 65 改为 66）：0 0 0 / 61 61 61 / 0 0 0 = 66（3 个米果渣横排 → 米纸）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(66))
                .pattern("   ")
                .pattern("FFF")
                .pattern("   ")
                .define('F', ModItems.byCode(61))
                .unlockedBy("has_rice_pomace", has(ModItems.byCode(61)))
                .save(consumer);

        // #68：0 0 0 / 69 67 69 / 0 0 0 = 68（米果复合液；M=69 米饭混合液 A=67 米果解析液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(68))
                .pattern("   ")
                .pattern("MAM")
                .pattern("   ")
                .define('M', ModItems.byCode(69))
                .define('A', ModItems.byCode(67))
                .unlockedBy("has_rice_meal_mixture", has(ModItems.byCode(69)))
                .save(consumer);

        // #69：0 0 0 / 30 原版水桶 9 / 0 0 0 = 69（米饭混合液；饭桶+水桶+米桶，空桶按原版机制返还）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(69))
                .pattern("   ")
                .pattern("PWB")
                .pattern("   ")
                .define('P', ModItems.byCode(30))
                .define('W', Items.WATER_BUCKET)
                .define('B', ModItems.byCode(9))
                .unlockedBy("has_rice_pail", has(ModItems.byCode(30)))
                .save(consumer);

        // #72：60 60 60 / 下界合金锭 ×3 / 60 60 60 = 72（粗制清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(72))
                .pattern("PPP")
                .pattern("NNN")
                .pattern("PPP")
                .define('P', ModItems.byCode(60))
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #75：71 74 71 / 74 60 74 / 71 74 71 = 75（清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(75))
                .pattern("ACA")
                .pattern("CPC")
                .pattern("ACA")
                .define('A', ModItems.byCode(71))
                .define('C', ModItems.byCode(74))
                .define('P', ModItems.byCode(60))
                .unlockedBy("has_cleansing_compound_plate", has(ModItems.byCode(74)))
                .save(consumer);

        // #76：75 75 75 / 75 71 75 / 75 75 75 = 76（8 个清洁复合锭围米果清洁液 → 粗制超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(76))
                .pattern("AAA")
                .pattern("ACA")
                .pattern("AAA")
                .define('A', ModItems.byCode(75))
                .define('C', ModItems.byCode(71))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #80：46 79 46 / 79 75 79 / 46 79 46 = 80（超懒合金锭，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(80))
                .pattern("NAN")
                .pattern("ACA")
                .pattern("NAN")
                .define('N', ModItems.byCode(46))
                .define('A', ModItems.byCode(79))
                .define('C', ModItems.byCode(75))
                .unlockedBy("has_super_lazy_alloy_chunk", has(ModItems.byCode(79)))
                .save(consumer);

        // #81：9 个 53 号米石填满 = 81（米石块）
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.byCode(81))
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.byCode(53))
                .unlockedBy("has_rice_stone", has(ModItems.byCode(53)))
                .save(consumer);

        // #82：20 20 20 / 20 69 20 / 20 20 20 = 82（8 个可燃米围米饭混合液 → 可燃混合液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(82))
                .pattern("FFF")
                .pattern("FMF")
                .pattern("FFF")
                .define('F', ModItems.byCode(20))
                .define('M', ModItems.byCode(69))
                .unlockedBy("has_combustible_rice", has(ModItems.byCode(20)))
                .save(consumer);

        // #84：0 0 0 / 0 71 83 / 0 0 0 = 84（清洁米沉离液；A=71 米果清洁液 C=83 可燃固态物）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(84))
                .pattern("   ")
                .pattern(" AC")
                .pattern("   ")
                .define('A', ModItems.byCode(71))
                .define('C', ModItems.byCode(83))
                .unlockedBy("has_rice_fruit_cleansing_solution", has(ModItems.byCode(71)))
                .save(consumer);

        // #85：0 0 34 / 38 34 0 / 24 38 0 = 85（饭晶剑）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(85))
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.byCode(34))
                .define('F', ModItems.byCode(38))
                .define('S', ModItems.byCode(24))
                .unlockedBy("has_crystallized_rice_meal", has(ModItems.byCode(34)))
                .save(consumer);

        // #86：0 0 51 / 40 51 0 / 24 40 0 = 86（超结晶剑；X=51 超结晶 F=40 精制米麻 S=24 米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(86))
                .pattern("  X")
                .pattern("FX ")
                .pattern("SF ")
                .define('X', ModItems.byCode(51))
                .define('F', ModItems.byCode(40))
                .define('S', ModItems.byCode(24))
                .unlockedBy("has_super_crystal", has(ModItems.byCode(51)))
                .save(consumer);

        // #87：0 51 24 / 51 40 24 / 0 51 24 = 87（超结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(87))
                .pattern(" XS")
                .pattern("XFS")
                .pattern(" XS")
                .define('X', ModItems.byCode(51))
                .define('F', ModItems.byCode(40))
                .define('S', ModItems.byCode(24))
                .unlockedBy("has_super_crystal", has(ModItems.byCode(51)))
                .save(consumer);

        // #88：0 0 35 / 40 35 0 / 23 40 0 = 88（饭晶合金剑；A=35 饭晶合金锭 F=40 S=23 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(88))
                .pattern("  A")
                .pattern("FA ")
                .pattern("SF ")
                .define('A', ModItems.byCode(35))
                .define('F', ModItems.byCode(40))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #89：0 35 23 / 35 40 23 / 0 35 23 = 89（饭晶合金弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(89))
                .pattern(" AS")
                .pattern("AFS")
                .pattern(" AS")
                .define('A', ModItems.byCode(35))
                .define('F', ModItems.byCode(40))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #90：35 35 35 / 35 0 35 / 0 0 0 = 90（饭晶合金头盔）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(90))
                .pattern("AAA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.byCode(35))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #91：35 0 35 / 35 35 35 / 35 35 35 = 91（饭晶合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(91))
                .pattern("A A")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.byCode(35))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #92：35 35 35 / 35 0 35 / 35 0 35 = 92（饭晶合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(92))
                .pattern("AAA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.byCode(35))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #93：0 0 0 / 35 0 35 / 35 0 35 = 93（饭晶合金靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(93))
                .pattern("   ")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.byCode(35))
                .unlockedBy("has_rice_crystal_alloy_ingot", has(ModItems.byCode(35)))
                .save(consumer);

        // #94：0 43 55 / 23 55 43 / 23 23 0 = 94（超重结晶剑；F=43 粗制米网 H=55 超重结晶 S=23 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(94))
                .pattern(" FH")
                .pattern("SHF")
                .pattern("SS ")
                .define('F', ModItems.byCode(43))
                .define('H', ModItems.byCode(55))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #95：43 55 23 / 55 23 43 / 43 55 23 = 95（超重结晶弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(95))
                .pattern("FHS")
                .pattern("HSF")
                .pattern("FHS")
                .define('F', ModItems.byCode(43))
                .define('H', ModItems.byCode(55))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #96：75 0 75 / 0 0 0 / 75 75 75 = 96（磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(96))
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.byCode(75))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // ================= 第四批：#97-#128（锻造三锭/胚/套装） =================

        // #97：75 0 0 / 75 75 0 / 75 0 0 = 97（弓弦锭；4 个 75 号清洁复合锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(97))
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.byCode(75))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #98：0 75 0 / 0 75 0 / 0 75 0 = 98（含甲锭；3 个清洁复合锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(98))
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.byCode(75))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #99：80 0 80 / 0 0 0 / 80 80 80 = 99（高级磨剑锭；5 个 80 号超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(99))
                .pattern("A A")
                .pattern("   ")
                .pattern("AAA")
                .define('A', ModItems.byCode(80))
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.byCode(80)))
                .save(consumer);

        // #100：80 0 0 / 80 80 0 / 80 0 0 = 100（高级弓弦锭；4 个超懒合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(100))
                .pattern("A  ")
                .pattern("AA ")
                .pattern("A  ")
                .define('A', ModItems.byCode(80))
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.byCode(80)))
                .save(consumer);

        // #101：0 80 0 / 0 80 0 / 0 80 0 = 101（高级含甲锭；3 个超懒合金锭竖排）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(101))
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', ModItems.byCode(80))
                .unlockedBy("has_super_lazy_alloy_ingot", has(ModItems.byCode(80)))
                .save(consumer);

        // #102：55 55 55 / 55 43 55 / 40 0 40 = 102（超重结晶头盔；H=55 F=43 粗制米网 M=40 精制米麻）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(102))
                .pattern("HHH")
                .pattern("HFH")
                .pattern("M M")
                .define('H', ModItems.byCode(55))
                .define('F', ModItems.byCode(43))
                .define('M', ModItems.byCode(40))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #103：55 43 55 / 55 55 55 / 35 55 35 = 103（超重结晶胸甲；A=35 饭晶合金锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(103))
                .pattern("HFH")
                .pattern("HHH")
                .pattern("AHA")
                .define('H', ModItems.byCode(55))
                .define('F', ModItems.byCode(43))
                .define('A', ModItems.byCode(35))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #104：55 35 55 / 55 43 55 / 55 43 55 = 104（超重结晶护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(104))
                .pattern("HAH")
                .pattern("HFH")
                .pattern("HFH")
                .define('H', ModItems.byCode(55))
                .define('A', ModItems.byCode(35))
                .define('F', ModItems.byCode(43))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #105：0 0 0 / 55 40 55 / 55 40 55 = 105（超重结晶靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(105))
                .pattern("   ")
                .pattern("HMH")
                .pattern("HMH")
                .define('H', ModItems.byCode(55))
                .define('M', ModItems.byCode(40))
                .unlockedBy("has_super_heavy_crystal", has(ModItems.byCode(55)))
                .save(consumer);

        // #106：0 43 60 / 23 60 43 / 23 23 0 = 106（复合米饭剑；F=43 P=60 复合米饭锭 S=23 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(106))
                .pattern(" FP")
                .pattern("SPF")
                .pattern("SS ")
                .define('F', ModItems.byCode(43))
                .define('P', ModItems.byCode(60))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #107：43 60 23 / 60 23 43 / 43 60 23 = 107（复合米饭弓）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(107))
                .pattern("FPS")
                .pattern("PSF")
                .pattern("FPS")
                .define('F', ModItems.byCode(43))
                .define('P', ModItems.byCode(60))
                .define('S', ModItems.byCode(23))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #108：60 60 60 / 60 46 60 / 40 0 40 = 108（复合米饭头盔；N=46 精制强化米网）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(108))
                .pattern("PPP")
                .pattern("PNP")
                .pattern("M M")
                .define('P', ModItems.byCode(60))
                .define('N', ModItems.byCode(46))
                .define('M', ModItems.byCode(40))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #109：60 46 60 / 60 60 60 / 55 60 55 = 109（复合米饭胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(109))
                .pattern("PNP")
                .pattern("PPP")
                .pattern("HPH")
                .define('P', ModItems.byCode(60))
                .define('N', ModItems.byCode(46))
                .define('H', ModItems.byCode(55))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #110：60 55 60 / 60 46 60 / 60 46 60 = 110（复合米饭护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(110))
                .pattern("PHP")
                .pattern("PNP")
                .pattern("PNP")
                .define('P', ModItems.byCode(60))
                .define('H', ModItems.byCode(55))
                .define('N', ModItems.byCode(46))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #111：0 0 0 / 60 40 60 / 60 40 60 = 111（复合米饭靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(111))
                .pattern("   ")
                .pattern("PMP")
                .pattern("PMP")
                .define('P', ModItems.byCode(60))
                .define('M', ModItems.byCode(40))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #112：0 75 0 / 75 下界合金锭 75 / 55 下界合金锭 55 = 112（清洁复合剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(112))
                .pattern(" A ")
                .pattern("ANA")
                .pattern("HNH")
                .define('A', ModItems.byCode(75))
                .define('N', Items.NETHERITE_INGOT)
                .define('H', ModItems.byCode(55))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #113：23 75 23 / 75 23 75 / 55 55 55 = 113（清洁复合弓胚；S=23 强化米棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(113))
                .pattern("SAS")
                .pattern("ASA")
                .pattern("HHH")
                .define('S', ModItems.byCode(23))
                .define('A', ModItems.byCode(75))
                .define('H', ModItems.byCode(55))
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #114：0 0 0 / 0 112 0 / 96 96 96 = 114（清洁复合剑；B=112 剑胚 A=96 磨剑锭）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(114))
                .pattern("   ")
                .pattern(" B ")
                .pattern("AAA")
                .define('B', ModItems.byCode(112))
                .define('A', ModItems.byCode(96))
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.byCode(112)))
                .save(consumer);

        // #115：0 97 0 / 97 113 97 / 0 0 0 = 115（清洁复合弓；S=97 弓弦锭 B=113 弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(115))
                .pattern(" S ")
                .pattern("SBS")
                .pattern("   ")
                .define('S', ModItems.byCode(97))
                .define('B', ModItems.byCode(113))
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.byCode(113)))
                .save(consumer);

        // #116：55 75 55 / 75 下界合金锭 75 / 55 75 55 = 116（清洁复合甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(116))
                .pattern("HAH")
                .pattern("ANA")
                .pattern("HAH")
                .define('H', ModItems.byCode(55))
                .define('A', ModItems.byCode(75))
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_ingot", has(ModItems.byCode(75)))
                .save(consumer);

        // #117：98 98 98 / 98 116 98 / 0 0 0 = 117（清洁复合头盔；A=98 含甲锭 B=116 甲胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(117))
                .pattern("AAA")
                .pattern("ABA")
                .pattern("   ")
                .define('A', ModItems.byCode(98))
                .define('B', ModItems.byCode(116))
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.byCode(116)))
                .save(consumer);

        // #118：98 116 98 / 98 98 98 / 98 98 98 = 118（清洁复合胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(118))
                .pattern("ABA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.byCode(98))
                .define('B', ModItems.byCode(116))
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.byCode(116)))
                .save(consumer);

        // #119：98 116 98 / 98 0 98 / 98 0 98 = 119（清洁复合护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(119))
                .pattern("ABA")
                .pattern("A A")
                .pattern("A A")
                .define('A', ModItems.byCode(98))
                .define('B', ModItems.byCode(116))
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.byCode(116)))
                .save(consumer);

        // #120：98 116 98 / 98 0 98 / 0 0 0 = 120（清洁复合靴子）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(120))
                .pattern("ABA")
                .pattern("A A")
                .pattern("   ")
                .define('A', ModItems.byCode(98))
                .define('B', ModItems.byCode(116))
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.byCode(116)))
                .save(consumer);

        // #121：下界合金锭 112 下界合金锭 / 112 80 112 / 下界合金锭 112 下界合金锭 = 121（超懒合金剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(121))
                .pattern("NBN")
                .pattern("BAB")
                .pattern("NBN")
                .define('N', Items.NETHERITE_INGOT)
                .define('B', ModItems.byCode(112))
                .define('A', ModItems.byCode(80))
                .unlockedBy("has_cleansing_compound_sword_blank", has(ModItems.byCode(112)))
                .save(consumer);

        // #122：113 80 113 / 113 113 113 / 下界合金锭 ×3 = 122（超懒合金弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(122))
                .pattern("BAB")
                .pattern("BBB")
                .pattern("NNN")
                .define('B', ModItems.byCode(113))
                .define('A', ModItems.byCode(80))
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_cleansing_compound_bow_blank", has(ModItems.byCode(113)))
                .save(consumer);

        // #123：116 80 116 / 80 116 80 / 116 80 116 = 123（超懒合金甲胚，棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(123))
                .pattern("BAB")
                .pattern("ABA")
                .pattern("BAB")
                .define('B', ModItems.byCode(116))
                .define('A', ModItems.byCode(80))
                .unlockedBy("has_cleansing_compound_armor_blank", has(ModItems.byCode(116)))
                .save(consumer);

        // #124：99 121 99 / 99 121 99 / 99 99 99 = 124（超懒合金剑；A=99 高级磨剑锭 B=121 剑胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(124))
                .pattern("ABA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModItems.byCode(99))
                .define('B', ModItems.byCode(121))
                .unlockedBy("has_super_lazy_alloy_sword_blank", has(ModItems.byCode(121)))
                .save(consumer);

        // #125（用户 2026-09-21 修订：中心 123 甲胚改为 122 弓胚）：0 100 0 / 100 122 100 / 0 100 0 = 125（超懒合金弓；S=100 高级弓弦锭 B=122 弓胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(125))
                .pattern(" S ")
                .pattern("SBS")
                .pattern(" S ")
                .define('S', ModItems.byCode(100))
                .define('B', ModItems.byCode(122))
                .unlockedBy("has_super_lazy_alloy_bow_blank", has(ModItems.byCode(122)))
                .save(consumer);

        // #126：123 123 123 / 101 46 101 / 0 0 0 = 126（超懒合金头盔；B=123 甲胚 A=101 高级含甲锭 N=46）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(126))
                .pattern("BBB")
                .pattern("ANA")
                .pattern("   ")
                .define('B', ModItems.byCode(123))
                .define('A', ModItems.byCode(101))
                .define('N', ModItems.byCode(46))
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.byCode(123)))
                .save(consumer);

        // #127：123 46 123 / 123 123 123 / 101 101 101 = 127（超懒合金胸甲）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(127))
                .pattern("BNB")
                .pattern("BBB")
                .pattern("AAA")
                .define('B', ModItems.byCode(123))
                .define('N', ModItems.byCode(46))
                .define('A', ModItems.byCode(101))
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.byCode(123)))
                .save(consumer);

        // #128：123 123 123 / 101 46 101 / 123 46 123（用户未写等号，按行首编号推断产物=128；超懒合金护腿）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(128))
                .pattern("BBB")
                .pattern("ANA")
                .pattern("BNB")
                .define('B', ModItems.byCode(123))
                .define('A', ModItems.byCode(101))
                .define('N', ModItems.byCode(46))
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.byCode(123)))
                .save(consumer);

        // ================= 第五批：#129-#160（靴子/铸造台/召唤器链/Boss 生物蛋/神翼） =================

        // #129：123 101 123 / 101 46 101 / 46 0 46 = 129（超懒合金靴子；B=123 甲胚 A=101 高级含甲锭 N=46）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(129))
                .pattern("BAB")
                .pattern("ANA")
                .pattern("N N")
                .define('B', ModItems.byCode(123))
                .define('A', ModItems.byCode(101))
                .define('N', ModItems.byCode(46))
                .unlockedBy("has_super_lazy_alloy_armor_blank", has(ModItems.byCode(123)))
                .save(consumer);

        // #130：0 60 0 / 35 35 35 / 6 6 6 = 130（铸剑台；P=60 复合米饭锭 A=35 饭晶合金锭 W=6 米木板）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(130))
                .pattern(" P ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.byCode(60))
                .define('A', ModItems.byCode(35))
                .define('W', ModItems.byCode(6))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #131：60 0 0 / 35 35 35 / 6 6 6 = 131（铸甲台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(131))
                .pattern("P  ")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.byCode(60))
                .define('A', ModItems.byCode(35))
                .define('W', ModItems.byCode(6))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #132：0 0 60 / 35 35 35 / 6 6 6 = 132（铸弓台）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(132))
                .pattern("  P")
                .pattern("AAA")
                .pattern("WWW")
                .define('P', ModItems.byCode(60))
                .define('A', ModItems.byCode(35))
                .define('W', ModItems.byCode(6))
                .unlockedBy("has_compound_rice_meal_ingot", has(ModItems.byCode(60)))
                .save(consumer);

        // #133：1 末影之眼 1 / 末影之眼 1 末影之眼 / 1 末影之眼 1 = 133（米米之眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(133))
                .pattern("RER")
                .pattern("ERE")
                .pattern("RER")
                .define('R', ModItems.byCode(1))
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(consumer);

        // #134：1 末地石 1 / 末地石 强化深板岩 末地石 / 1 末地石 1 = 134（米米传送器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(134))
                .pattern("RSR")
                .pattern("SDS")
                .pattern("RSR")
                .define('R', ModItems.byCode(1))
                .define('S', Items.END_STONE)
                .define('D', Items.REINFORCED_DEEPSLATE)
                .unlockedBy("has_end_stone", has(Items.END_STONE))
                .save(consumer);

        // #135：1 红石 1 / 红石 1 红石 / 1 萤石粉 1 = 135（小型召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(135))
                .pattern("RDR")
                .pattern("DRD")
                .pattern("RGR")
                .define('R', ModItems.byCode(1))
                .define('D', Items.REDSTONE)
                .define('G', Items.GLOWSTONE_DUST)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);

        // #137：5 红石块 5 / 红石块 5 红石块 / 5 142 5 = 137（中型召唤器）
        // 注意：用户原文行尾写"=136"，但 136 是不开放获取的占位物，137 才是中型召唤器，且配方含 142 召唤核心（对应中级），故按行首编号产物=137 处理
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(137))
                .pattern("LBL")
                .pattern("BLB")
                .pattern("LCL")
                .define('L', ModItems.byCode(5))
                .define('B', Items.REDSTONE_BLOCK)
                .define('C', ModItems.byCode(142))
                .unlockedBy("has_summon_core", has(ModItems.byCode(142)))
                .save(consumer);

        // #138：26 红石 26 / 粘液球 28 粘液球 / 26 145 26 = 138（大型召唤器；26=粗米块 28=米块）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(138))
                .pattern("ADA")
                .pattern("SRS")
                .pattern("ACA")
                .define('A', ModItems.byCode(26))
                .define('D', Items.REDSTONE)
                .define('S', Items.SLIME_BALL)
                .define('R', ModItems.byCode(28))
                .define('C', ModItems.byCode(145))
                .unlockedBy("has_advanced_summon_core", has(ModItems.byCode(145)))
                .save(consumer);

        // #139：28 红石块 28 / 粘液块 28 粘液块 / 28 150 28 = 139（最懒召唤器）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(139))
                .pattern("RBR")
                .pattern("KRK")
                .pattern("RCR")
                .define('R', ModItems.byCode(28))
                .define('B', Items.REDSTONE_BLOCK)
                .define('K', Items.SLIME_BLOCK)
                .define('C', ModItems.byCode(150))
                .unlockedBy("has_laziest_summon_core", has(ModItems.byCode(150)))
                .save(consumer);

        // #141：0 20 0 / 20 140 20 / 0 20 0 = 141（粗制召唤核心胚；F=20 可燃米矿石 B=140 召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(141))
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.byCode(20))
                .define('B', ModItems.byCode(140))
                .unlockedBy("has_summon_core_blank", has(ModItems.byCode(140)))
                .save(consumer);

        // #142：141 在熔炉烧 2s（40t）→ 142（召唤核心）
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.byCode(141)), RecipeCategory.MISC,
                        ModItems.byCode(142), 0.3F, 40)
                .unlockedBy("has_coarse_summon_core_blank", has(ModItems.byCode(141)))
                .save(consumer);

        // #144：0 82 0 / 82 143 82 / 0 82 0 = 144（粗制高级召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(144))
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.byCode(82))
                .define('B', ModItems.byCode(143))
                .unlockedBy("has_advanced_summon_core_blank", has(ModItems.byCode(143)))
                .save(consumer);

        // #145：144 在熔炉烧 2s（40t）→ 145（高级召唤核心）
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.byCode(144)), RecipeCategory.MISC,
                        ModItems.byCode(145), 0.3F, 40)
                .unlockedBy("has_coarse_advanced_summon_core_blank", has(ModItems.byCode(144)))
                .save(consumer);

        // #146：16 19 16 / 19 16 19 / 16 19 16 = 146（混合结晶；棋盘交错）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(146))
                .pattern("ABA")
                .pattern("BAB")
                .pattern("ABA")
                .define('A', ModItems.byCode(16))
                .define('B', ModItems.byCode(19))
                .unlockedBy("has_crystallized_rice", has(ModItems.byCode(16)))
                .save(consumer);

        // #147：146 在熔炉烧 2s（40t）→ 147（熔融混合结晶）
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.byCode(146)), RecipeCategory.MISC,
                        ModItems.byCode(147), 0.3F, 40)
                .unlockedBy("has_mixed_crystal", has(ModItems.byCode(146)))
                .save(consumer);

        // #149：0 83 0 / 83 148 83 / 0 83 0 = 149（粗制最懒召唤核心胚）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(149))
                .pattern(" F ")
                .pattern("FBF")
                .pattern(" F ")
                .define('F', ModItems.byCode(83))
                .define('B', ModItems.byCode(148))
                .unlockedBy("has_laziest_summon_core_blank", has(ModItems.byCode(148)))
                .save(consumer);

        // #150：149 在熔炉烧 2s（40t）→ 150（最懒召唤核心）
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.byCode(149)), RecipeCategory.MISC,
                        ModItems.byCode(150), 0.3F, 40)
                .unlockedBy("has_coarse_laziest_summon_core_blank", has(ModItems.byCode(149)))
                .save(consumer);

        // #151：23 222 23 / 222 龙蛋 222 / 23 222 23 = 151（米米猴王生物蛋；222=米果黑混液）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(151))
                .pattern("SAS")
                .pattern("AEA")
                .pattern("SAS")
                .define('S', ModItems.byCode(23))
                .define('A', ModItems.byCode(222))
                .define('E', Items.DRAGON_EGG)
                .unlockedBy("has_dragon_egg", has(Items.DRAGON_EGG))
                .save(consumer);

        // #152：铁锭 223 铁锭 / 223 227 223 / 铁锭 223 铁锭 = 152（饭晶虎王生物蛋；223=米果混黏液 227=猴王如意棍）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(152))
                .pattern("IVI")
                .pattern("VTV")
                .pattern("IVI")
                .define('I', Items.IRON_INGOT)
                .define('V', ModItems.byCode(223))
                .define('T', ModItems.byCode(227))
                .unlockedBy("has_monkey_king_ruyi_staff", has(ModItems.byCode(227)))
                .save(consumer);

        // #153：53 224 53 / 224 229 224 / 53 224 53 = 153（超晶牛王生物蛋；53=米石 224=爆米绽放液 229=虎王利爪）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(153))
                .pattern("RLR")
                .pattern("LCL")
                .pattern("RLR")
                .define('R', ModItems.byCode(53))
                .define('L', ModItems.byCode(224))
                .define('C', ModItems.byCode(229))
                .unlockedBy("has_tiger_king_razor_claw", has(ModItems.byCode(229)))
                .save(consumer);

        // #154：34 241 34 / 241 231 241 / 34 241 34 = 154（重晶兔王生物蛋；241=爆米嘬心液 231=牛王晶角）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(154))
                .pattern("RHR")
                .pattern("HOH")
                .pattern("RHR")
                .define('R', ModItems.byCode(34))
                .define('H', ModItems.byCode(241))
                .define('O', ModItems.byCode(231))
                .unlockedBy("has_ox_king_crystal_horn", has(ModItems.byCode(231)))
                .save(consumer);

        // #155：35 225 35 / 225 233 225 / 35 225 35 = 155（复晶天青蟒生物蛋；225=眼米嘬心液 233=兔王腥红眼）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(155))
                .pattern("RYR")
                .pattern("YEY")
                .pattern("RYR")
                .define('R', ModItems.byCode(35))
                .define('Y', ModItems.byCode(225))
                .define('E', ModItems.byCode(233))
                .unlockedBy("has_rabbit_king_bloody_red_eye", has(ModItems.byCode(233)))
                .save(consumer);

        // #156（用户未写等号，按行首编号推断产物=156）：龙息 226 龙息 / 226 235 226 / 龙息 226 龙息 = 156（清洁长命夔生物蛋；226=米米杂烩液 235=极毒青蟒牙）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(156))
                .pattern("BMB")
                .pattern("MFM")
                .pattern("BMB")
                .define('B', Items.DRAGON_BREATH)
                .define('M', ModItems.byCode(226))
                .define('F', ModItems.byCode(235))
                .unlockedBy("has_venom_azure_python_fang", has(ModItems.byCode(235)))
                .save(consumer);

        // #157：51 下界合金碎片 51 / 下界合金碎片 51 下界合金碎片 / 51 下界合金碎片 51 = 157（最懒终末龙生物蛋）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(157))
                .pattern("RNR")
                .pattern("NRN")
                .pattern("RNR")
                .define('R', ModItems.byCode(51))
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(consumer);

        // #159：123 226 123 / 160 鞘翅 160 / 123 226 123 = 159（米米神翼；160=米米幻翼膜）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.byCode(159))
                .pattern("AMA")
                .pattern("PEP")
                .pattern("AMA")
                .define('A', ModItems.byCode(123))
                .define('M', ModItems.byCode(226))
                .define('P', ModItems.byCode(160))
                .define('E', Items.ELYTRA)
                .unlockedBy("has_mimi_phantom_membrane", has(ModItems.byCode(160)))
                .save(consumer);

        // #160：1 55 1 / 幽匿块 幻翼膜 幽匿块 / 1 55 1 = 160（米米幻翼膜；55=超重结晶）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.byCode(160))
                .pattern("RHR")
                .pattern("SMS")
                .pattern("RHR")
                .define('R', ModItems.byCode(1))
                .define('H', ModItems.byCode(55))
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
