package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.entity.CrusherBlockEntity;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.GrindingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IngotCondensationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.DualInputProcessorBlockEntity;
import com.mimiworld.mimiworld.block.entity.IonizationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IronWokBlockEntity;
import com.mimiworld.mimiworld.block.entity.JumboMegaCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.LargeCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.MixerBlockEntity;
import com.mimiworld.mimiworld.block.entity.OvenBlockEntity;
import com.mimiworld.mimiworld.block.entity.PestleRealmTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.PrecipitationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.SwordQuenchingTableBlockEntity;
import com.mimiworld.mimiworld.tags.ModItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * 工作方块（非原版工作台/熔炉）配方统一注册入口。
 * 在 FMLCommonSetupEvent 的 enqueueWork 中调用（物品注册表已冻结，可安全读取注册名）。
 *
 * 对应 ModRecipeProvider 头部《配方书写规则》第 6 条：
 * 锤米台为无序双输入，均为即时合成；
 * 电离台/沉淀台为无序双输入定时加工（DualInputProcessor，registerRecipe 第 4 参为加工 ticks）。
 */
public final class ModMachineRecipes {

    private ModMachineRecipes() {
    }

    public static void register() {
        // 锤米台 —— 燧石 + 米原木 → 米木板（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.FLINT,
                ModItems.RICE_LOG.get(),
                new ItemStack(ModItems.RICE_PLANKS.get()));

        // 锤米台 —— 金锭 + 粗超结晶 → 超结晶（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.GOLD_INGOT,
                ModItems.COARSE_SUPER_CRYSTAL.get(),
                new ItemStack(ModItems.SUPER_CRYSTAL.get()));

        // 锤米台 —— 结晶米 + 铁锭 → 召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.CRYSTALLIZED_RICE.get(),
                Items.IRON_INGOT,
                new ItemStack(ModItems.SUMMON_CORE_BLANK.get()));

        // 锤米台 —— 再结晶米 + 钻石 → 高级召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.RECRYSTALLIZED_RICE.get(),
                Items.DIAMOND,
                new ItemStack(ModItems.ADVANCED_SUMMON_CORE_BLANK.get()));

        // 锤米台 —— 熔融混合结晶 + 下界合金锭 → 最懒召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.MOLTEN_MIXED_CRYSTAL.get(),
                Items.NETHERITE_INGOT,
                new ItemStack(ModItems.LAZIEST_SUMMON_CORE_BLANK.get()));

        // 锤米台 —— 左上角超重结晶 + 左下角硬化米 → 超重结晶棒（镐手柄）
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.SUPER_HEAVY_CRYSTAL.get(),
                ModItems.HARDENED_RICE.get(),
                new ItemStack(ModItems.SUPER_HEAVY_CRYSTAL_ROD.get()));

        // 锤米台 —— 左上角复合米饭锭 + 左下角结晶米 → 复合米饭棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.COMPOUND_RICE_MEAL_INGOT.get(),
                ModItems.CRYSTALLIZED_RICE.get(),
                new ItemStack(ModItems.COMPOUND_RICE_MEAL_ROD.get()));

        // 锤米台 —— 左上角清洁复合锭 + 左下角重结晶米 → 清洁复合棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.CLEANSING_COMPOUND_INGOT.get(),
                ModItems.RECRYSTALLIZED_RICE.get(),
                new ItemStack(ModItems.CLEANSING_COMPOUND_ROD.get()));

        // 锤米台 —— 左上角超懒合金锭 + 左下角熔融混合结晶 → 超懒合金棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.SUPER_LAZY_ALLOY_INGOT.get(),
                ModItems.MOLTEN_MIXED_CRYSTAL.get(),
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_ROD.get()));

        // 电离台 —— 左上角粗制超懒合金锭 + 左下角清洁米沉离液，
        // 处理 2s（40t）→ 超懒合金离子态（输入无序，两槽各消耗 1 个）
        IonizationTableBlockEntity.registerRecipe(
                ModItems.COARSE_SUPER_LAZY_ALLOY_INGOT.get(),
                ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get(),
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_IONIC_FORM.get()),
                40);

        // 沉淀台 —— 左上角超懒合金离子态 + 左下角清洁米沉离液，
        // 处理 2s（40t）→ 超懒合金粉（两槽各消耗 1 个）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.SUPER_LAZY_ALLOY_IONIC_FORM.get(),
                ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get(),
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_DUST.get()),
                40);

        // 沉淀台 —— 米果渣 + 3 个玻璃瓶 → 一次产出 3 个米果汁（每周期消耗 1 米果渣 + 3 玻璃瓶）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.RICE_POMACE.get(), 1,
                Items.GLASS_BOTTLE, 3,
                new ItemStack(ModItems.RICE_FRUIT_JUICE.get(), 3),
                DualInputProcessorBlockEntity.DEFAULT_PROCESS_TICKS);

        // 沉淀台 —— 米果混合渣 + 3 个玻璃瓶 → 一次产出 3 个米果清洁液（每周期消耗 1 米果混合渣 + 3 玻璃瓶）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.RICE_FRUIT_MIXED_POMACE.get(), 1,
                Items.GLASS_BOTTLE, 3,
                new ItemStack(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get(), 3),
                DualInputProcessorBlockEntity.DEFAULT_PROCESS_TICKS);

        // 离解台 —— 上槽米果混合液 + 下槽米（离解介质）→ 米果解析液
        DissociationTableBlockEntity.registerRecipe(
                ModItems.RICE_FRUIT_MIXTURE.get(),
                ModItems.RICE.get(),
                new ItemStack(ModItems.RICE_FRUIT_ANALYZING_SOLUTION.get()));

        // 淬剑台 —— 左上超懒合金剑 + 右上上古剑意 + 中间淬剑液
        // → 超懒神剑·残缺·贰（需按"开始"按钮加工一轮）
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_2.get()),
                ModItems.SUPER_LAZY_ALLOY_SWORD.get(),
                ModItems.ANCIENT_SWORD_INTENT.get(),
                ModItems.SWORD_QUENCHING_LIQUID.get());

        // 淬剑台 —— 左上残缺·贰 + 右上远古剑魄 + 中间淬剑液 → 残次
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_DEFECTIVE.get()),
                ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_2.get(),
                ModItems.ANCIENT_SWORD_SOUL.get(),
                ModItems.SWORD_QUENCHING_LIQUID.get());

        // 淬剑台 —— 左上残次 + 右上太古剑魄 + 中间淬剑液 → 赝品
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT.get()),
                ModItems.SUPER_LAZY_DIVINE_SWORD_DEFECTIVE.get(),
                ModItems.PRIMORDIAL_SWORD_SOUL.get(),
                ModItems.SWORD_QUENCHING_LIQUID.get());

        // 淬剑台 —— 左上赝品 + 右上神剑鉴玉 + 中间淬剑液 → 瑕疵
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_FLAWED.get()),
                ModItems.SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT.get(),
                ModItems.DIVINE_SWORD_APPRAISAL_JADE.get(),
                ModItems.SWORD_QUENCHING_LIQUID.get());

        // 淬剑台 —— 左上超懒剑法 + 右上瑕疵 + 中间淬剑液 → 完整
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_COMPLETE.get()),
                ModItems.SUPER_LAZY_SWORD_ART.get(),
                ModItems.SUPER_LAZY_DIVINE_SWORD_FLAWED.get(),
                ModItems.SWORD_QUENCHING_LIQUID.get());

        // 碾磨台 —— 左列上 残堕锭 / 左列中 超懒合金锭，左列下与右列三格
        // 共 4 个残堕深渊岩 → 磨制残堕深渊岩（槽序：左上/左中/左下/右上/右中/右下）
        Item abyssRock = ModItems.REMNANT_FALLEN_ABYSS_ROCK.get();
        GrindingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK.get()),
                ModItems.REMNANT_FALLEN_INGOT.get(),
                ModItems.SUPER_LAZY_ALLOY_INGOT.get(),
                abyssRock,
                abyssRock,
                abyssRock,
                abyssRock);

        // 杵界台 —— 2x2 四格各放 1 个粗制残堕世界精华（无序）→ 磨制残堕世界精华
        Item coarseEssence = ModItems.COARSE_REMNANT_FALLEN_WORLD_ESSENCE.get();
        PestleRealmTableBlockEntity.registerShapeless(
                new ItemStack(ModItems.POLISHED_REMNANT_FALLEN_WORLD_ESSENCE.get()),
                coarseEssence, coarseEssence, coarseEssence, coarseEssence);

        // ================= 食物加工链 =================
        // 用户机器代号：i=搅碎器 ii=搅拌器 v=大煮锅(3x3) vii=巨无霸大大大煮锅(5x5)
        // viii=铁锅(4 槽菱形) vv=烤箱(3x3)；原煮锅/超大煮锅的配方已分别移植到 v/vii 并删除原机器。
        // 额外槽物品：碗=大碗；玻璃瓶=原版 GLASS_BOTTLE（作为配料被消耗，机器不返还空瓶）；
        // 干海带=原版 DRIED_KELP；烤箱的额外槽物品=米木棍。未给额外槽物品时传 null（该槽必须为空）。
        Item bowl = ModItems.BIG_BOWL.get();
        Item bottle = Items.GLASS_BOTTLE;
        Item driedKelp = Items.DRIED_KELP;

        // ---- i 搅碎器（有序三槽：左上 / 左下=额外槽 / 右上）----

        // 天脑汁：界天脑花（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_BRAIN_JUICE.get()),
                ModItems.REALM_HEAVEN_BRAIN_FLOWER.get(), bottle, null);

        // 米堕汁：特色堕落米（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.FALLEN_RICE_JUICE.get()),
                ModItems.SPECIALTY_FALLEN_RICE.get(), bottle, null);

        // 凤肉泥：米凤肉块（左上），其余槽空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_MEAT_PASTE.get()),
                ModItems.RICE_PHOENIX_MEAT_CHUNK.get(), null, null);

        // 油瓶：米蜃油脂（左上）+ 染鲛油脂（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.OIL_BOTTLE.get()),
                ModItems.RICE_MIRAGE_GREASE.get(), bottle, ModItems.DYED_MERMAID_GREASE.get());

        // 蛟鲛肉泥：浅鲛尾（左上）+ 望米伤鲛足（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.JIAO_MERMAID_MEAT_PASTE.get()),
                ModItems.PALE_MERMAID_TAIL.get(), null, ModItems.WANGMI_WOUNDED_MERMAID_LEG.get());

        // 凤蜃肉泥：米凤肉块（左上）+ 半蜃肉片（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_MIRAGE_MEAT_PASTE.get()),
                ModItems.RICE_PHOENIX_MEAT_CHUNK.get(), null, ModItems.HALF_MIRAGE_MEAT_SLICE.get());

        // 花香油：棘花瓣（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.FLORAL_FRAGRANCE_OIL.get()),
                ModItems.THORN_PETAL.get(), bottle, null);

        // 米雪茶：米堕汁（左上）+ 清洁米沉离液（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.RICE_SNOW_TEA.get()),
                ModItems.FALLEN_RICE_JUICE.get(), bottle, ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get());

        // ---- ii 搅拌器（registerRecipe(result, 额外槽8物品, 槽0~7 无序配料...)；
        // 碗/瓶必须放槽 8；无额外物传 null，此时 9 槽整体无序、槽 8 可当第 9 槽）----

        // 酱脑：天脑汁 + 香袋，额外玻璃瓶（槽 8）
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCED_BRAIN.get()), bottle,
                ModItems.CELESTIAL_BRAIN_JUICE.get(), ModItems.FRAGRANT_POUCH.get());

        // 凤球：凤肉泥×1 + 香粉×7，无额外槽物品
        Item fragrantPowder = ModItems.FRAGRANT_POWDER.get();
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_BALL.get()), null,
                ModItems.PHOENIX_MEAT_PASTE.get(),
                fragrantPowder, fragrantPowder, fragrantPowder,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder);

        // 腌料：香袋 + 油瓶 + 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINADE.get()), null,
                ModItems.FRAGRANT_POUCH.get(), ModItems.OIL_BOTTLE.get(), ModItems.THORN_PETAL.get());

        // 腌鹫翅：噬鹫翅 + 腌料
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_VULTURE_WING.get()), null,
                ModItems.DEVOURER_VULTURE_WING.get(), ModItems.MARINADE.get());

        // 珍贵调料：酱脑 + 米堕汁 + 油瓶 + 腌料 + 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.PRECIOUS_SEASONING.get()), null,
                ModItems.SAUCED_BRAIN.get(), ModItems.FALLEN_RICE_JUICE.get(), ModItems.OIL_BOTTLE.get(),
                ModItems.MARINADE.get(), ModItems.FRAGRANT_POUCH.get());

        // 烧烤粉：香粉×4 + 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.BBQ_POWDER.get()), null,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder,
                ModItems.THORN_PETAL.get());

        // 复合油：米蜃油脂 + 天脑汁 + 花香油
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.COMPOUND_OIL.get()), null,
                ModItems.RICE_MIRAGE_GREASE.get(), ModItems.CELESTIAL_BRAIN_JUICE.get(), ModItems.FLORAL_FRAGRANCE_OIL.get());

        // 天堕茶：米堕汁×2 + 天脑汁×2 + 清洁米沉离液×4（8 个普通槽），额外碗（槽 8）
        Item fallenRiceJuice = ModItems.FALLEN_RICE_JUICE.get();
        Item brainJuice = ModItems.CELESTIAL_BRAIN_JUICE.get();
        Item cleansingLiquid = ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get();
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_FALLEN_TEA.get()), bowl,
                fallenRiceJuice, fallenRiceJuice, brainJuice, brainJuice,
                cleansingLiquid, cleansingLiquid, cleansingLiquid, cleansingLiquid);

        // 腌翅包：凤肉泥 + 腌鹫翅 + 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_WING_WRAP.get()), null,
                ModItems.PHOENIX_MEAT_PASTE.get(), ModItems.MARINATED_VULTURE_WING.get(), ModItems.FRAGRANT_POUCH.get());

        // 腌肉片：半蜃肉片×5 + 腌料
        Item mirageSlice = ModItems.HALF_MIRAGE_MEAT_SLICE.get();
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_MEAT_SLICE.get()), null,
                mirageSlice, mirageSlice, mirageSlice, mirageSlice, mirageSlice,
                ModItems.MARINADE.get());

        // 饭团：熟特色堕落米×8 + 香袋（共 9 料，无额外物，槽 8 当普通槽）
        Item cookedFallenRice = ModItems.COOKED_SPECIALTY_FALLEN_RICE.get();
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.ONIGIRI.get()), null,
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice,
                ModItems.FRAGRANT_POUCH.get(),
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice);

        // ---- v 大煮锅（3x3 网格行优先 + 右上额外槽）----

        // 酱香双鲛尾（原煮锅 2x2 配方移植到大煮锅，置于 3x3 左上 2x2）：
        // 酱脑 + 米堕汁 / 浅鲛尾 + 浅鲛尾，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCE_GLAZED_TWIN_MERMAID_TAIL.get()), bowl,
                ModItems.SAUCED_BRAIN.get(), ModItems.FALLEN_RICE_JUICE.get(), null,
                ModItems.PALE_MERMAID_TAIL.get(), ModItems.PALE_MERMAID_TAIL.get());

        // 天虚棘脑汤：虚无触足 / 界天脑花 / 虚无触足
        // 棘花瓣 / 棘花瓣 / 棘花瓣
        // 香粉 / 棘花瓣 / 香粉，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP.get()), bowl,
                ModItems.VOID_TOUCH_TENTACLE.get(), ModItems.REALM_HEAVEN_BRAIN_FLOWER.get(), ModItems.VOID_TOUCH_TENTACLE.get(),
                ModItems.THORN_PETAL.get(), ModItems.THORN_PETAL.get(), ModItems.THORN_PETAL.get(),
                ModItems.FRAGRANT_POWDER.get(), ModItems.THORN_PETAL.get(), ModItems.FRAGRANT_POWDER.get());

        // 海兽相交饭：蛟鲛肉泥×3；浅鲛尾×3；浅鲛尾 / 珍贵调料 / 浅鲛尾，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SEA_BEAST_JIAO_RICE.get()), bowl,
                ModItems.JIAO_MERMAID_MEAT_PASTE.get(), ModItems.JIAO_MERMAID_MEAT_PASTE.get(), ModItems.JIAO_MERMAID_MEAT_PASTE.get(),
                ModItems.PALE_MERMAID_TAIL.get(), ModItems.PALE_MERMAID_TAIL.get(), ModItems.PALE_MERMAID_TAIL.get(),
                ModItems.PALE_MERMAID_TAIL.get(), ModItems.PRECIOUS_SEASONING.get(), ModItems.PALE_MERMAID_TAIL.get());

        // 后续铁锅/烤箱/巨无霸配方共用的局部物品（原超大煮锅配方已移植到巨无霸大大大煮锅）
        Item jiaoPaste = ModItems.JIAO_MERMAID_MEAT_PASTE.get();
        Item phoenixPaste = ModItems.PHOENIX_MIRAGE_MEAT_PASTE.get();
        Item mermaidLeg = ModItems.WANGMI_WOUNDED_MERMAID_LEG.get();
        Item phoenixMeat = ModItems.RICE_PHOENIX_MEAT_CHUNK.get();
        Item marinatedWing = ModItems.MARINATED_VULTURE_WING.get();
        Item marinatedSlice = ModItems.MARINATED_MEAT_SLICE.get();
        Item fallenRice = ModItems.SPECIALTY_FALLEN_RICE.get();

        // ---- viii 铁锅（4 槽顺序：左上 / 右上 / 左下 / 右下 + 右上侧额外槽）----

        // 香混肉片：腌肉片×2 + 棘花瓣 + 香粉，无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.FRAGRANT_MIXED_MEAT_SLICE.get()), null,
                marinatedSlice, marinatedSlice, ModItems.THORN_PETAL.get(), ModItems.FRAGRANT_POWDER.get());

        // 油爆虚无触：虚无触足×2 + 复合油 + 油瓶，额外碗
        Item tentacle = ModItems.VOID_TOUCH_TENTACLE.get();
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.OIL_SEARED_VOID_TENTACLE.get()), bowl,
                tentacle, tentacle, ModItems.COMPOUND_OIL.get(), ModItems.OIL_BOTTLE.get());

        // 蛟足炒饭：望米伤鲛足 + 复合油 + 特色堕落米×2，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.MERMAID_LEG_FRIED_RICE.get()), bowl,
                mermaidLeg, ModItems.COMPOUND_OIL.get(), fallenRice, fallenRice);

        // 煎龙凤饼：凤球 + 望米伤鲛足 + 珍贵调料 + 复合油，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.PAN_FRIED_DRAGON_PHOENIX_CAKE.get()), bowl,
                ModItems.PHOENIX_BALL.get(), mermaidLeg, ModItems.PRECIOUS_SEASONING.get(), ModItems.COMPOUND_OIL.get());

        // 凤包翅：腌翅包 + 花香油 + 棘花瓣（右下空），无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_WRAPPED_WING.get()), null,
                ModItems.MARINATED_WING_WRAP.get(), ModItems.FLORAL_FRAGRANCE_OIL.get(), ModItems.THORN_PETAL.get(), null);

        // 海带片：油瓶 + 原版干海带×3，无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.KELP_SLICE.get()), null,
                ModItems.OIL_BOTTLE.get(), driedKelp, driedKelp, driedKelp);

        // ---- vv 烤箱（3x3 网格行优先 + 右上额外槽）----

        // 酱烤鹫翅：腌鹫翅 / 油瓶 / 米蜃油脂；香粉×3；腌鹫翅 / 油瓶 / 米蜃油脂，额外碗
        Item mirageGrease = ModItems.RICE_MIRAGE_GREASE.get();
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCE_ROASTED_VULTURE_WING.get()), bowl,
                marinatedWing, ModItems.OIL_BOTTLE.get(), mirageGrease,
                fragrantPowder, fragrantPowder, fragrantPowder,
                marinatedWing, ModItems.OIL_BOTTLE.get(), mirageGrease);

        // 花香蛟尾：浅鲛尾 / 花香油 / 腌料，三行同构，额外碗
        Item mermaidTail = ModItems.PALE_MERMAID_TAIL.get();
        Item fragranceOil = ModItems.FLORAL_FRAGRANCE_OIL.get();
        Item marinade = ModItems.MARINADE.get();
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.FLORAL_MERMAID_TAIL.get()), bowl,
                mermaidTail, fragranceOil, marinade,
                mermaidTail, ModItems.PRECIOUS_SEASONING.get(), marinade,
                mermaidTail, fragranceOil, marinade);

        // 肢体烤串：浅鲛尾 / 烧烤粉 / 虚无触足；望米伤鲛足 / 烧烤粉 / 米凤肉块；
        // 烧烤粉 / 空 / 烧烤粉，额外槽=米木棍
        Item riceStick = ModItems.RICE_STICK.get();
        Item bbqPowder = ModItems.BBQ_POWDER.get();
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.LIMB_SKEWER.get()), riceStick,
                mermaidTail, bbqPowder, tentacle,
                mermaidLeg, bbqPowder, phoenixMeat,
                bbqPowder, null, bbqPowder);

        // ---- vii 巨无霸大大大煮锅（5x5 网格行优先 25 格 + 右上额外槽）----

        // 超级大乱炖：5x5 摆满 22 种料理 + 3 个米米大杂烩（逐格见下），额外碗。
        // （用户修订：个别格子改为米米大杂烩，消除自引用与占位物依赖）
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SUPER_GRAND_STEW.get()), bowl,
                ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP.get(), ModItems.FRAGRANT_MIXED_MEAT_SLICE.get(), ModItems.SAUCE_GLAZED_TWIN_MERMAID_TAIL.get(),
                ModItems.MIMI_MEDLEY.get(), ModItems.SAUCE_ROASTED_VULTURE_WING.get(),
                ModItems.FALLEN_RICE_MEATBALL_SOUP.get(), ModItems.OIL_SEARED_VOID_TENTACLE.get(), ModItems.MERMAID_LEG_FRIED_RICE.get(),
                ModItems.DRAGON_PHOENIX_FLOWER_SOUP.get(), ModItems.MIRAGE_ILLUSION.get(),
                ModItems.FLORAL_MERMAID_TAIL.get(), ModItems.SEA_BEAST_JIAO_RICE.get(), ModItems.BUDDHA_ILLUSION.get(),
                ModItems.RICE_SNOW_TEA.get(), ModItems.PAN_FRIED_DRAGON_PHOENIX_CAKE.get(),
                ModItems.CELESTIAL_FALLEN_TEA.get(), ModItems.PHOENIX_WRAPPED_WING.get(), ModItems.MEAT_SLICE_CONGEE.get(),
                ModItems.LIMB_SKEWER.get(), ModItems.CELESTIAL_BRAIN_RICE_BALL.get(),
                ModItems.PALE_MERMAID_RICE_BALL.get(), ModItems.MERMAID_LEG_RICE_BALL.get(), ModItems.VULTURE_WING_RICE_BALL.get(),
                ModItems.MIMI_MEDLEY.get(), ModItems.MIMI_MEDLEY.get());

        // 蜃楼幻想：5x5 边框与内点（null 格必须为空），额外碗。
        // 凤蜃肉泥×10（左右两列）、腌料×6、复合油×4，空格 5。
        Item compoundOil = ModItems.COMPOUND_OIL.get();
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.MIRAGE_ILLUSION.get()), bowl,
                phoenixPaste, marinade, marinade, marinade, phoenixPaste,
                phoenixPaste, null, marinade, null, phoenixPaste,
                phoenixPaste, marinade, null, compoundOil, phoenixPaste,
                phoenixPaste, null, compoundOil, null, phoenixPaste,
                phoenixPaste, marinade, compoundOil, compoundOil, phoenixPaste);

        // ---- 原超大煮锅（4x4）四条配方移植到 5x5：原样保留逐格图案并置于左上 4x4，
        // 第 5 列/第 5 行（null 位）必须为空，额外槽仍为碗 ----

        // 堕米肉丸汤：蛟鲛肉泥×4；凤蜃肉泥×4；珍贵调料 / 虚无触足×2 / 棘花瓣；
        // 半蜃肉片×2 / 棘花瓣×2，额外碗
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.FALLEN_RICE_MEATBALL_SOUP.get()), bowl,
                jiaoPaste, jiaoPaste, jiaoPaste, jiaoPaste, null,
                phoenixPaste, phoenixPaste, phoenixPaste, phoenixPaste, null,
                ModItems.PRECIOUS_SEASONING.get(), ModItems.VOID_TOUCH_TENTACLE.get(), ModItems.VOID_TOUCH_TENTACLE.get(), ModItems.THORN_PETAL.get(), null,
                ModItems.HALF_MIRAGE_MEAT_SLICE.get(), ModItems.HALF_MIRAGE_MEAT_SLICE.get(), ModItems.THORN_PETAL.get(), ModItems.THORN_PETAL.get(), null);

        // 龙凤花汤：望米伤鲛足×4；米凤肉块×4；珍贵调料 / 花香油×2 / 珍贵调料；
        // 空 / 花香油 / 空 / 空，额外碗
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.DRAGON_PHOENIX_FLOWER_SOUP.get()), bowl,
                mermaidLeg, mermaidLeg, mermaidLeg, mermaidLeg, null,
                phoenixMeat, phoenixMeat, phoenixMeat, phoenixMeat, null,
                ModItems.PRECIOUS_SEASONING.get(), ModItems.FLORAL_FRAGRANCE_OIL.get(), ModItems.FLORAL_FRAGRANCE_OIL.get(), ModItems.PRECIOUS_SEASONING.get(), null,
                null, ModItems.FLORAL_FRAGRANCE_OIL.get(), null, null, null);

        // 佛幻想：凤蜃肉泥×3 + 蛟鲛肉泥；蛟鲛肉泥×2 + 腌鹫翅×2；
        // 腌鹫翅 + 花香油×2 + 珍贵调料；珍贵调料 + 三格空，额外碗
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.BUDDHA_ILLUSION.get()), bowl,
                phoenixPaste, phoenixPaste, phoenixPaste, jiaoPaste, null,
                jiaoPaste, jiaoPaste, marinatedWing, marinatedWing, null,
                marinatedWing, ModItems.FLORAL_FRAGRANCE_OIL.get(), ModItems.FLORAL_FRAGRANCE_OIL.get(), ModItems.PRECIOUS_SEASONING.get(), null,
                ModItems.PRECIOUS_SEASONING.get(), null, null, null, null);

        // 肉片粥：腌肉片×3 + 花香油 + 特色堕落米×12，额外碗
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.MEAT_SLICE_CONGEE.get()), bowl,
                marinatedSlice, marinatedSlice, ModItems.FLORAL_FRAGRANCE_OIL.get(), marinatedSlice, null,
                fallenRice, fallenRice, fallenRice, fallenRice, null,
                fallenRice, fallenRice, fallenRice, fallenRice, null,
                fallenRice, fallenRice, fallenRice, fallenRice, null);

        // ---- 汲血台（槽0=左上 / 槽1=左下，各消耗 1 个；支持上槽标签匹配）----

        // 血瓶：左上任意肉类（mimiworld:meats 标签，生熟皆可）+ 左下原版玻璃瓶
        BloodlettingTableBlockEntity.registerRecipe(new ItemStack(ModItems.BLOOD_VIAL.get()),
                ModItemTags.MEATS, Items.GLASS_BOTTLE);

        // ---- 凝锭台（5x5=25 格工作台式即时合成，有序图案；空格' '=该格必须为空）----

        // 残堕世界精华锭：深染十素材围边 + 6 个盗米之心 + 9 个磨制残堕精华液
        // 图案见下：A-E 与 H-L 为深染十素材，F=盗米之心（6 个），G=磨制残堕精华液（9 个）
        Map<Character, Item> remnantIngotKeys = Map.ofEntries(
                Map.entry('A', ModItems.DEEP_DYED_REMNANT_HEAVEN_WING.get()),
                Map.entry('B', ModItems.DEEP_DYED_VOID_TOUCH.get()),
                Map.entry('C', ModItems.DEEP_DYED_FADED_FLOWER_BRANCH.get()),
                Map.entry('D', ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK.get()),
                Map.entry('E', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE.get()),
                Map.entry('F', ModItems.RICE_STEALER_HEART.get()),
                Map.entry('G', ModItems.POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION.get()),
                Map.entry('H', ModItems.DEEP_DYED_MERMAID_TEAR.get()),
                Map.entry('I', ModItems.DEEP_DYED_JIAOLONG_TALON.get()),
                Map.entry('J', ModItems.DEEP_DYED_MIRAGE_BONE.get()),
                Map.entry('K', ModItems.DEEP_DYED_ELF_HAT.get()),
                Map.entry('L', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW.get()));
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get()),
                new String[]{
                        "ABCDE",
                        "FGGGF",
                        "FGGGF",
                        "FGGGF",
                        "HIJKL"
                }, remnantIngotKeys);

        // 世界之泪：4 个最懒终末龙头（四角）+ 8 个米魂之泪（十字外沿）
        // + 13 个残堕世界精华锭（中心菱形），图案见下
        Map<Character, Item> worldTearKeys = Map.of(
                'A', ModItems.LAZIEST_END_DRAGON_HEAD.get(),
                'B', ModItems.RICE_SOUL_TEAR.get(),
                'X', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get());
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.WORLD_TEAR.get()),
                new String[]{
                        "ABXBA",
                        "BXXXB",
                        "XXXXX",
                        "BXXXB",
                        "ABXBA"
                }, worldTearKeys);

    }
}
