package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.entity.CookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.CrusherBlockEntity;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ExtraLargeCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.GrindingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IngotCondensationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IonizationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IronWokBlockEntity;
import com.mimiworld.mimiworld.block.entity.JumboMegaCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.LargeCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.MixerBlockEntity;
import com.mimiworld.mimiworld.block.entity.OvenBlockEntity;
import com.mimiworld.mimiworld.block.entity.PestleRealmTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.PrecipitationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.SwordQuenchingTableBlockEntity;
import com.mimiworld.mimiworld.tags.ModItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * 工作方块（非原版工作台/熔炉）配方统一注册入口。
 * 在 FMLCommonSetupEvent 的 enqueueWork 中调用（物品注册表已冻结，Item.getId 可用）。
 *
 * 对应 ModRecipeProvider 头部《配方书写规则》第 6 条：
 * 锤米台为无序双输入、压饭机为 1..9 输入多重集合，均为即时合成；
 * 电离台/沉淀台为无序双输入定时加工（DualInputProcessor，registerRecipe 第 4 参为加工 ticks）。
 */
public final class ModMachineRecipes {

    private ModMachineRecipes() {
    }

    public static void register() {
        // 锤米台 —— 燧石 + 米原木 → 米木板（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.FLINT,
                ModItems.RICE_LOG,
                new ItemStack(ModItems.RICE_PLANKS));

        // 锤米台 —— 金锭 + 粗超结晶 → 超结晶（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.GOLD_INGOT,
                ModItems.COARSE_SUPER_CRYSTAL,
                new ItemStack(ModItems.SUPER_CRYSTAL));

        // 锤米台 —— 结晶米 + 铁锭 → 召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.CRYSTALLIZED_RICE,
                Items.IRON_INGOT,
                new ItemStack(ModItems.SUMMON_CORE_BLANK));

        // 锤米台 —— 再结晶米 + 钻石 → 高级召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.RECRYSTALLIZED_RICE,
                Items.DIAMOND,
                new ItemStack(ModItems.ADVANCED_SUMMON_CORE_BLANK));

        // 锤米台 —— 熔融混合结晶 + 下界合金锭 → 最懒召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.MOLTEN_MIXED_CRYSTAL,
                Items.NETHERITE_INGOT,
                new ItemStack(ModItems.LAZIEST_SUMMON_CORE_BLANK));

        // 锤米台 —— 左上角 超重结晶 + 左下角 硬化米 → 超重结晶棒（镐手柄）
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.SUPER_HEAVY_CRYSTAL,
                ModItems.HARDENED_RICE,
                new ItemStack(ModItems.SUPER_HEAVY_CRYSTAL_ROD));

        // 锤米台 —— 左上角 复合米饭锭 + 左下角 结晶米 → 复合米饭棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.COMPOUND_RICE_MEAL_INGOT,
                ModItems.CRYSTALLIZED_RICE,
                new ItemStack(ModItems.COMPOUND_RICE_MEAL_ROD));

        // 锤米台 —— 左上角 清洁复合锭 + 左下角 重结晶米 → 清洁复合棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.CLEANSING_COMPOUND_INGOT,
                ModItems.RECRYSTALLIZED_RICE,
                new ItemStack(ModItems.CLEANSING_COMPOUND_ROD));

        // 锤米台 —— 左上角 超懒合金锭 + 左下角 熔融混合结晶 → 超懒合金棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.SUPER_LAZY_ALLOY_INGOT,
                ModItems.MOLTEN_MIXED_CRYSTAL,
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_ROD));

        // 压饭机 —— 9 个 饭桶 → 1 个 压缩饭桶。
        // 压饭机仅按输入槽 shrink(1) 消耗，不触发原版合成剩余物逻辑，因此【桶不返还】。
        Item[] ninePails = new Item[RicePressBlockEntity.INPUT_SLOTS];
        Item pail = ModItems.RICE_PAIL;
        for (int i = 0; i < ninePails.length; i++) {
            ninePails[i] = pail;
        }
        RicePressBlockEntity.registerRecipe(new ItemStack(ModItems.COMPRESSED_RICE_PAIL), ninePails);

        // 电离台 —— 左上角 粗制超懒合金锭 + 左下角 清洁米沉离液，
        // 处理 2s（40t）→ 超懒合金离子态（输入无序，两槽各消耗 1 个）
        IonizationTableBlockEntity.registerRecipe(
                ModItems.COARSE_SUPER_LAZY_ALLOY_INGOT,
                ModItems.CLEANSING_RICE_SEPARATION_SOLUTION,
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_IONIC_FORM),
                40);

        // 沉淀台 —— 左上角 超懒合金离子态 + 左下角 清洁米沉离液，
        // 处理 2s（40t）→ 超懒合金粉（两槽各消耗 1 个）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.SUPER_LAZY_ALLOY_IONIC_FORM,
                ModItems.CLEANSING_RICE_SEPARATION_SOLUTION,
                new ItemStack(ModItems.SUPER_LAZY_ALLOY_DUST),
                40);

        // 沉淀台 —— 单槽 米果渣（另一槽留空）→ 一次产出 3 个 米果汁
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.RICE_POMACE,
                new ItemStack(ModItems.RICE_FRUIT_JUICE, 3));

        // 沉淀台 —— 单槽 米果混合渣（另一槽留空）→ 一次产出 3 个 米果清洁液
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.RICE_FRUIT_MIXED_POMACE,
                new ItemStack(ModItems.RICE_FRUIT_CLEANSING_SOLUTION, 3));

        // 离解台 —— 上槽 米果混合液 + 下槽 米（离解介质）→ 米果解析液
        DissociationTableBlockEntity.registerRecipe(
                ModItems.RICE_FRUIT_MIXTURE,
                ModItems.RICE,
                new ItemStack(ModItems.RICE_FRUIT_ANALYZING_SOLUTION));

        // 淬剑台 —— 左上 超懒合金剑 + 右上 上古剑意 + 中间 淬剑液
        // → 超懒神剑·残缺·贰（需按"开始"按钮加工一轮）
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_2),
                ModItems.SUPER_LAZY_ALLOY_SWORD,
                ModItems.ANCIENT_SWORD_INTENT,
                ModItems.SWORD_QUENCHING_LIQUID);

        // 淬剑台 —— 左上 残缺·贰 + 右上 远古剑魄 + 中间 淬剑液 → 残次
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_DEFECTIVE),
                ModItems.SUPER_LAZY_DIVINE_SWORD_BROKEN_2,
                ModItems.ANCIENT_SWORD_SOUL,
                ModItems.SWORD_QUENCHING_LIQUID);

        // 淬剑台 —— 左上 残次 + 右上 太古剑魄 + 中间 淬剑液 → 赝品
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT),
                ModItems.SUPER_LAZY_DIVINE_SWORD_DEFECTIVE,
                ModItems.PRIMORDIAL_SWORD_SOUL,
                ModItems.SWORD_QUENCHING_LIQUID);

        // 淬剑台 —— 左上 赝品 + 右上 神剑鉴玉 + 中间 淬剑液 → 瑕疵
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_FLAWED),
                ModItems.SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT,
                ModItems.DIVINE_SWORD_APPRAISAL_JADE,
                ModItems.SWORD_QUENCHING_LIQUID);

        // 淬剑台 —— 左上 超懒剑法 + 右上 瑕疵 + 中间 淬剑液 → 完整
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.SUPER_LAZY_DIVINE_SWORD_COMPLETE),
                ModItems.SUPER_LAZY_SWORD_ART,
                ModItems.SUPER_LAZY_DIVINE_SWORD_FLAWED,
                ModItems.SWORD_QUENCHING_LIQUID);

        // 碾磨台 —— 左列上 残堕锭 / 左列中 超懒合金锭，左列下与右列三格
        // 共 4 个 残堕深渊岩 → 磨制残堕深渊岩（槽序：左上/左中/左下/右上/右中/右下）
        Item abyssRock = ModItems.REMNANT_FALLEN_ABYSS_ROCK;
        GrindingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.POLISHED_REMNANT_FALLEN_ABYSS_ROCK),
                ModItems.REMNANT_FALLEN_INGOT,
                ModItems.SUPER_LAZY_ALLOY_INGOT,
                abyssRock,
                abyssRock,
                abyssRock,
                abyssRock);

        // 杵界台 —— 2x2 四格各放 1 个 粗制残堕世界精华（无序）→ 磨制残堕世界精华
        Item coarseEssence = ModItems.COARSE_REMNANT_FALLEN_WORLD_ESSENCE;
        PestleRealmTableBlockEntity.registerShapeless(
                new ItemStack(ModItems.POLISHED_REMNANT_FALLEN_WORLD_ESSENCE),
                coarseEssence, coarseEssence, coarseEssence, coarseEssence);

        // ================= 食物加工链（35 条）=================
        // 用户机器代号：i=搅碎器 ii=搅拌器 iv=煮锅(2x2) v=大煮锅(3x3) vi=超大煮锅(4x4)
        // viii=铁锅(4 槽菱形) vv=烤箱(3x3)；本批未用到 iii 小煮锅、vii 巨无霸大大大煮锅。
        // 额外槽物品：碗=大碗；玻璃瓶=原版 GLASS_BOTTLE（作为配料被消耗，机器不返还空瓶）；
        // 干海带=原版 DRIED_KELP；烤箱的额外槽物品=米木棍。未给额外槽物品时传 null（该槽必须为空）。
        Item bowl = ModItems.BIG_BOWL;
        Item bottle = Items.GLASS_BOTTLE;
        Item driedKelp = Items.DRIED_KELP;

        // ---- i 搅碎器（有序三槽：左上 / 左下=额外槽 / 右上）----

        // 天脑汁：界天脑花（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_BRAIN_JUICE),
                ModItems.REALM_HEAVEN_BRAIN_FLOWER, bottle, null);

        // 米堕汁：特色堕落米（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.FALLEN_RICE_JUICE),
                ModItems.SPECIALTY_FALLEN_RICE, bottle, null);

        // 凤肉泥：米凤肉块（左上），其余槽空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_MEAT_PASTE),
                ModItems.RICE_PHOENIX_MEAT_CHUNK, null, null);

        // 油瓶：米蜃油脂（左上）+ 染鲛油脂（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.OIL_BOTTLE),
                ModItems.RICE_MIRAGE_GREASE, bottle, ModItems.DYED_MERMAID_GREASE);

        // 蛟鲛肉泥：浅鲛尾（左上）+ 望米伤鲛足（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.JIAO_MERMAID_MEAT_PASTE),
                ModItems.PALE_MERMAID_TAIL, null, ModItems.WANGMI_WOUNDED_MERMAID_LEG);

        // 凤蜃肉泥：米凤肉块（左上）+ 半蜃肉片（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_MIRAGE_MEAT_PASTE),
                ModItems.RICE_PHOENIX_MEAT_CHUNK, null, ModItems.HALF_MIRAGE_MEAT_SLICE);

        // 花香油：棘花瓣（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.FLORAL_FRAGRANCE_OIL),
                ModItems.THORN_PETAL, bottle, null);

        // 米雪茶：米堕汁（左上）+ 清洁米沉离液（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.RICE_SNOW_TEA),
                ModItems.FALLEN_RICE_JUICE, bottle, ModItems.CLEANSING_RICE_SEPARATION_SOLUTION);

        // ---- ii 搅拌器（registerRecipe(result, 额外槽8物品, 槽0~7 无序配料...)；
        // 碗/瓶必须放槽 8；无额外物传 null，此时 9 槽整体无序、槽 8 可当第 9 槽）----

        // 酱脑：天脑汁 + 香袋，额外玻璃瓶（槽 8）
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCED_BRAIN), bottle,
                ModItems.CELESTIAL_BRAIN_JUICE, ModItems.FRAGRANT_POUCH);

        // 凤球：凤肉泥×1 + 香粉×7，无额外槽物品
        Item fragrantPowder = ModItems.FRAGRANT_POWDER;
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_BALL), null,
                ModItems.PHOENIX_MEAT_PASTE,
                fragrantPowder, fragrantPowder, fragrantPowder,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder);

        // 腌料：香袋 + 油瓶 + 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINADE), null,
                ModItems.FRAGRANT_POUCH, ModItems.OIL_BOTTLE, ModItems.THORN_PETAL);

        // 腌鹫翅：噬鹫翅 + 腌料
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_VULTURE_WING), null,
                ModItems.DEVOURER_VULTURE_WING, ModItems.MARINADE);

        // 珍贵调料：酱脑 + 米堕汁 + 油瓶 + 腌料 + 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.PRECIOUS_SEASONING), null,
                ModItems.SAUCED_BRAIN, ModItems.FALLEN_RICE_JUICE, ModItems.OIL_BOTTLE,
                ModItems.MARINADE, ModItems.FRAGRANT_POUCH);

        // 烧烤粉：香粉×4 + 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.BBQ_POWDER), null,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder,
                ModItems.THORN_PETAL);

        // 复合油：米蜃油脂 + 天脑汁 + 花香油
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.COMPOUND_OIL), null,
                ModItems.RICE_MIRAGE_GREASE, ModItems.CELESTIAL_BRAIN_JUICE, ModItems.FLORAL_FRAGRANCE_OIL);

        // 天堕茶：米堕汁×2 + 天脑汁×2 + 清洁米沉离液×4（8 个普通槽），额外碗（槽 8）
        Item fallenRiceJuice = ModItems.FALLEN_RICE_JUICE;
        Item brainJuice = ModItems.CELESTIAL_BRAIN_JUICE;
        Item cleansingLiquid = ModItems.CLEANSING_RICE_SEPARATION_SOLUTION;
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_FALLEN_TEA), bowl,
                fallenRiceJuice, fallenRiceJuice, brainJuice, brainJuice,
                cleansingLiquid, cleansingLiquid, cleansingLiquid, cleansingLiquid);

        // 腌翅包：凤肉泥 + 腌鹫翅 + 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_WING_WRAP), null,
                ModItems.PHOENIX_MEAT_PASTE, ModItems.MARINATED_VULTURE_WING, ModItems.FRAGRANT_POUCH);

        // 腌肉片：半蜃肉片×5 + 腌料
        Item mirageSlice = ModItems.HALF_MIRAGE_MEAT_SLICE;
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.MARINATED_MEAT_SLICE), null,
                mirageSlice, mirageSlice, mirageSlice, mirageSlice, mirageSlice,
                ModItems.MARINADE);

        // 饭团：熟特色堕落米×8 + 香袋（共 9 料，无额外物，槽 8 当普通槽）
        Item cookedFallenRice = ModItems.COOKED_SPECIALTY_FALLEN_RICE;
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.ONIGIRI), null,
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice,
                ModItems.FRAGRANT_POUCH,
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice);

        // ---- iv 煮锅（2x2 网格行优先 + 右上额外槽）----

        // 酱香双鲛尾：酱脑 + 米堕汁 + 浅鲛尾×2，额外碗
        CookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCE_GLAZED_TWIN_MERMAID_TAIL), bowl,
                ModItems.SAUCED_BRAIN, ModItems.FALLEN_RICE_JUICE,
                ModItems.PALE_MERMAID_TAIL, ModItems.PALE_MERMAID_TAIL);

        // ---- v 大煮锅（3x3 网格行优先 + 右上额外槽）----

        // 天虚棘脑汤：虚无触足 / 界天脑花 / 虚无触足
        // 棘花瓣 / 棘花瓣 / 棘花瓣
        // 香粉 / 棘花瓣 / 香粉，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP), bowl,
                ModItems.VOID_TOUCH_TENTACLE, ModItems.REALM_HEAVEN_BRAIN_FLOWER, ModItems.VOID_TOUCH_TENTACLE,
                ModItems.THORN_PETAL, ModItems.THORN_PETAL, ModItems.THORN_PETAL,
                ModItems.FRAGRANT_POWDER, ModItems.THORN_PETAL, ModItems.FRAGRANT_POWDER);

        // 占位产物（placeholder_325，转正前无法合法获得）：用户主动给配方，照直译注册。
        // 凤球 / 望米伤鲛足 / 凤球；棘花瓣 / 望米伤鲛足 / 棘花瓣；香粉×3，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.PLACEHOLDER_325), bowl,
                ModItems.PHOENIX_BALL, ModItems.WANGMI_WOUNDED_MERMAID_LEG, ModItems.PHOENIX_BALL,
                ModItems.THORN_PETAL, ModItems.WANGMI_WOUNDED_MERMAID_LEG, ModItems.THORN_PETAL,
                ModItems.FRAGRANT_POWDER, ModItems.FRAGRANT_POWDER, ModItems.FRAGRANT_POWDER);

        // 海兽相交饭：蛟鲛肉泥×3；浅鲛尾×3；浅鲛尾 / 珍贵调料 / 浅鲛尾，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SEA_BEAST_JIAO_RICE), bowl,
                ModItems.JIAO_MERMAID_MEAT_PASTE, ModItems.JIAO_MERMAID_MEAT_PASTE, ModItems.JIAO_MERMAID_MEAT_PASTE,
                ModItems.PALE_MERMAID_TAIL, ModItems.PALE_MERMAID_TAIL, ModItems.PALE_MERMAID_TAIL,
                ModItems.PALE_MERMAID_TAIL, ModItems.PRECIOUS_SEASONING, ModItems.PALE_MERMAID_TAIL);

        // ---- vi 超大煮锅（4x4 网格行优先 + 右上额外槽；null 位该格必须为空）----

        // 堕米肉丸汤：蛟鲛肉泥×4；凤蜃肉泥×4；珍贵调料 / 虚无触足×2 / 棘花瓣；
        // 半蜃肉片×2 / 棘花瓣×2，额外碗
        Item jiaoPaste = ModItems.JIAO_MERMAID_MEAT_PASTE;
        Item phoenixPaste = ModItems.PHOENIX_MIRAGE_MEAT_PASTE;
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.FALLEN_RICE_MEATBALL_SOUP), bowl,
                jiaoPaste, jiaoPaste, jiaoPaste, jiaoPaste,
                phoenixPaste, phoenixPaste, phoenixPaste, phoenixPaste,
                ModItems.PRECIOUS_SEASONING, ModItems.VOID_TOUCH_TENTACLE, ModItems.VOID_TOUCH_TENTACLE, ModItems.THORN_PETAL,
                ModItems.HALF_MIRAGE_MEAT_SLICE, ModItems.HALF_MIRAGE_MEAT_SLICE, ModItems.THORN_PETAL, ModItems.THORN_PETAL);

        // 龙凤花汤：望米伤鲛足×4；米凤肉块×4；珍贵调料 / 花香油×2 / 珍贵调料；
        // 空 / 花香油 / 空 / 空，额外碗
        Item mermaidLeg = ModItems.WANGMI_WOUNDED_MERMAID_LEG;
        Item phoenixMeat = ModItems.RICE_PHOENIX_MEAT_CHUNK;
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.DRAGON_PHOENIX_FLOWER_SOUP), bowl,
                mermaidLeg, mermaidLeg, mermaidLeg, mermaidLeg,
                phoenixMeat, phoenixMeat, phoenixMeat, phoenixMeat,
                ModItems.PRECIOUS_SEASONING, ModItems.FLORAL_FRAGRANCE_OIL, ModItems.FLORAL_FRAGRANCE_OIL, ModItems.PRECIOUS_SEASONING,
                null, ModItems.FLORAL_FRAGRANCE_OIL, null, null);

        // 佛幻想：凤蜃肉泥×3 + 蛟鲛肉泥；蛟鲛肉泥×2 + 腌鹫翅×2；
        // 腌鹫翅 + 花香油×2 + 珍贵调料；珍贵调料 + 三格空，额外碗
        Item marinatedWing = ModItems.MARINATED_VULTURE_WING;
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.BUDDHA_ILLUSION), bowl,
                phoenixPaste, phoenixPaste, phoenixPaste, jiaoPaste,
                jiaoPaste, jiaoPaste, marinatedWing, marinatedWing,
                marinatedWing, ModItems.FLORAL_FRAGRANCE_OIL, ModItems.FLORAL_FRAGRANCE_OIL, ModItems.PRECIOUS_SEASONING,
                ModItems.PRECIOUS_SEASONING, null, null, null);

        // 肉片粥：腌肉片×3 + 花香油 + 特色堕落米×12，额外碗
        Item marinatedSlice = ModItems.MARINATED_MEAT_SLICE;
        Item fallenRice = ModItems.SPECIALTY_FALLEN_RICE;
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.MEAT_SLICE_CONGEE), bowl,
                marinatedSlice, marinatedSlice, ModItems.FLORAL_FRAGRANCE_OIL, marinatedSlice,
                fallenRice, fallenRice, fallenRice, fallenRice,
                fallenRice, fallenRice, fallenRice, fallenRice,
                fallenRice, fallenRice, fallenRice, fallenRice);

        // ---- viii 铁锅（4 槽顺序：左上 / 右上 / 左下 / 右下 + 右上侧额外槽）----

        // 香混肉片：腌肉片×2 + 棘花瓣 + 香粉，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.FRAGRANT_MIXED_MEAT_SLICE), bowl,
                marinatedSlice, marinatedSlice, ModItems.THORN_PETAL, ModItems.FRAGRANT_POWDER);

        // 油爆虚无触：虚无触足×2 + 复合油 + 油瓶，额外碗
        Item tentacle = ModItems.VOID_TOUCH_TENTACLE;
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.OIL_SEARED_VOID_TENTACLE), bowl,
                tentacle, tentacle, ModItems.COMPOUND_OIL, ModItems.OIL_BOTTLE);

        // 蛟足炒饭：望米伤鲛足 + 复合油 + 特色堕落米×2，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.MERMAID_LEG_FRIED_RICE), bowl,
                mermaidLeg, ModItems.COMPOUND_OIL, fallenRice, fallenRice);

        // 煎龙凤饼：凤球 + 望米伤鲛足 + 珍贵调料 + 复合油，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.PAN_FRIED_DRAGON_PHOENIX_CAKE), bowl,
                ModItems.PHOENIX_BALL, mermaidLeg, ModItems.PRECIOUS_SEASONING, ModItems.COMPOUND_OIL);

        // 凤包翅：腌翅包 + 花香油 + 棘花瓣（右下空），无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.PHOENIX_WRAPPED_WING), null,
                ModItems.MARINATED_WING_WRAP, ModItems.FLORAL_FRAGRANCE_OIL, ModItems.THORN_PETAL, null);

        // 海带片：油瓶 + 原版干海带×3，无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.KELP_SLICE), null,
                ModItems.OIL_BOTTLE, driedKelp, driedKelp, driedKelp);

        // ---- vv 烤箱（3x3 网格行优先 + 右上额外槽）----

        // 酱烤鹫翅：腌鹫翅 / 油瓶 / 米蜃油脂；香粉×3；腌鹫翅 / 油瓶 / 米蜃油脂，额外碗
        Item mirageGrease = ModItems.RICE_MIRAGE_GREASE;
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.SAUCE_ROASTED_VULTURE_WING), bowl,
                marinatedWing, ModItems.OIL_BOTTLE, mirageGrease,
                fragrantPowder, fragrantPowder, fragrantPowder,
                marinatedWing, ModItems.OIL_BOTTLE, mirageGrease);

        // 花香蛟尾：浅鲛尾 / 花香油 / 腌料，三行同构，额外碗
        Item mermaidTail = ModItems.PALE_MERMAID_TAIL;
        Item fragranceOil = ModItems.FLORAL_FRAGRANCE_OIL;
        Item marinade = ModItems.MARINADE;
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.FLORAL_MERMAID_TAIL), bowl,
                mermaidTail, fragranceOil, marinade,
                mermaidTail, ModItems.PRECIOUS_SEASONING, marinade,
                mermaidTail, fragranceOil, marinade);

        // 肢体烤串：浅鲛尾 / 烧烤粉 / 虚无触足；望米伤鲛足 / 烧烤粉 / 米凤肉块；
        // 烧烤粉 / 空 / 烧烤粉，额外槽=米木棍
        Item riceStick = ModItems.RICE_STICK;
        Item bbqPowder = ModItems.BBQ_POWDER;
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.LIMB_SKEWER), riceStick,
                mermaidTail, bbqPowder, tentacle,
                mermaidLeg, bbqPowder, phoenixMeat,
                bbqPowder, null, bbqPowder);

        // ---- vii 巨无霸大大大煮锅（5x5 网格行优先 25 格 + 右上额外槽）----

        // 超级大乱炖：5x5 摆满 22 种料理 + 3 个米米大杂烩（逐格见下），额外碗。
        // （用户修订：个别格子改为米米大杂烩，消除自引用与占位物依赖）
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.SUPER_GRAND_STEW), bowl,
                ModItems.CELESTIAL_VOID_THORN_BRAIN_SOUP, ModItems.FRAGRANT_MIXED_MEAT_SLICE, ModItems.SAUCE_GLAZED_TWIN_MERMAID_TAIL,
                ModItems.MIMI_MEDLEY, ModItems.SAUCE_ROASTED_VULTURE_WING,
                ModItems.FALLEN_RICE_MEATBALL_SOUP, ModItems.OIL_SEARED_VOID_TENTACLE, ModItems.MERMAID_LEG_FRIED_RICE,
                ModItems.DRAGON_PHOENIX_FLOWER_SOUP, ModItems.MIRAGE_ILLUSION,
                ModItems.FLORAL_MERMAID_TAIL, ModItems.SEA_BEAST_JIAO_RICE, ModItems.BUDDHA_ILLUSION,
                ModItems.RICE_SNOW_TEA, ModItems.PAN_FRIED_DRAGON_PHOENIX_CAKE,
                ModItems.CELESTIAL_FALLEN_TEA, ModItems.PHOENIX_WRAPPED_WING, ModItems.MEAT_SLICE_CONGEE,
                ModItems.LIMB_SKEWER, ModItems.CELESTIAL_BRAIN_RICE_BALL,
                ModItems.PALE_MERMAID_RICE_BALL, ModItems.MERMAID_LEG_RICE_BALL, ModItems.VULTURE_WING_RICE_BALL,
                ModItems.MIMI_MEDLEY, ModItems.MIMI_MEDLEY);

        // 蜃楼幻想：5x5 边框与内点（null 格必须为空），额外碗。
        // 凤蜃肉泥×10（左右两列）、腌料×6、复合油×4，空格 5。
        Item compoundOil = ModItems.COMPOUND_OIL;
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.MIRAGE_ILLUSION), bowl,
                phoenixPaste, marinade, marinade, marinade, phoenixPaste,
                phoenixPaste, null, marinade, null, phoenixPaste,
                phoenixPaste, marinade, null, compoundOil, phoenixPaste,
                phoenixPaste, null, compoundOil, null, phoenixPaste,
                phoenixPaste, marinade, compoundOil, compoundOil, phoenixPaste);

        // ---- 汲血台（槽0=左上 / 槽1=左下，各消耗 1 个；支持上槽标签匹配）----

        // 血瓶：左上任意肉类（mimiworld:meats 标签，生熟皆可）+ 左下原版玻璃瓶
        BloodlettingTableBlockEntity.registerRecipe(new ItemStack(ModItems.BLOOD_VIAL),
                ModItemTags.MEATS, Items.GLASS_BOTTLE);

        // ---- 凝锭台（5x5=25 格工作台式即时合成，有序图案；空格' '=该格必须为空）----

        // 残堕世界精华锭：深染十素材围边 + 6 个盗米之心 + 9 个磨制残堕精华液
        // 图案见下：A-E 与 H-L 为深染十素材，F=盗米之心（6 个），G=磨制残堕精华液（9 个）
        Map<Character, Item> remnantIngotKeys = Map.ofEntries(
                Map.entry('A', ModItems.DEEP_DYED_REMNANT_HEAVEN_WING),
                Map.entry('B', ModItems.DEEP_DYED_VOID_TOUCH),
                Map.entry('C', ModItems.DEEP_DYED_FADED_FLOWER_BRANCH),
                Map.entry('D', ModItems.DEEP_DYED_CELESTIAL_PHOENIX_BEAK),
                Map.entry('E', ModItems.DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE),
                Map.entry('F', ModItems.RICE_STEALER_HEART),
                Map.entry('G', ModItems.POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION),
                Map.entry('H', ModItems.DEEP_DYED_MERMAID_TEAR),
                Map.entry('I', ModItems.DEEP_DYED_JIAOLONG_TALON),
                Map.entry('J', ModItems.DEEP_DYED_MIRAGE_BONE),
                Map.entry('K', ModItems.DEEP_DYED_ELF_HAT),
                Map.entry('L', ModItems.DEEP_DYED_MOON_DEVOURER_WOLF_CLAW));
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT),
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
                'A', ModItems.LAZIEST_END_DRAGON_HEAD,
                'B', ModItems.RICE_SOUL_TEAR,
                'X', ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT);
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.WORLD_TEAR),
                new String[]{
                        "ABXBA",
                        "BXXXB",
                        "XXXXX",
                        "BXXXB",
                        "ABXBA"
                }, worldTearKeys);

        // ---- 凝锭台：25 个相同材料摆满即得 ----

        // 龙骨架构：25 个 龙骨
        Item dragonBone = ModItems.DRAGON_BONE;
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.DRAGON_BONE_FRAMEWORK),
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone);

        // 龙血精华：25 个 龙血
        Item dragonBlood = ModItems.DRAGON_BLOOD;
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.DRAGON_BLOOD_ESSENCE),
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood);

        // 龙鳞块：25 个 龙鳞
        Item dragonScale = ModItems.DRAGON_SCALE;
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.DRAGON_SCALE_BLOCK),
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale);
    }
}
