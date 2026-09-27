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
        // #6：锤米台 —— 燧石 + 5 号米原木 → 6 号米木板（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.FLINT,
                ModItems.byCode(5),
                new ItemStack(ModItems.byCode(6)));

        // #51：锤米台 —— 金锭 + 50 号粗超结晶 → 51 号超结晶（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.GOLD_INGOT,
                ModItems.byCode(50),
                new ItemStack(ModItems.byCode(51)));

        // #140：锤米台 —— 16 号结晶米 + 铁锭 → 140 号召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(16),
                Items.IRON_INGOT,
                new ItemStack(ModItems.byCode(140)));

        // #143：锤米台 —— 19 号再结晶米 + 钻石 → 143 号高级召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(19),
                Items.DIAMOND,
                new ItemStack(ModItems.byCode(143)));

        // #148：锤米台 —— 147 号熔融混合结晶 + 下界合金锭 → 148 号最懒召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(147),
                Items.NETHERITE_INGOT,
                new ItemStack(ModItems.byCode(148)));

        // #247：锤米台 —— 左上角 55 号超重结晶 + 左下角 11 号硬化米 → 247 号超重结晶棒（镐手柄）
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(55),
                ModItems.byCode(11),
                new ItemStack(ModItems.byCode(247)));

        // #248：锤米台 —— 左上角 60 号复合米饭锭 + 左下角 16 号结晶米 → 248 号复合米饭棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(60),
                ModItems.byCode(16),
                new ItemStack(ModItems.byCode(248)));

        // #249：锤米台 —— 左上角 75 号清洁复合锭 + 左下角 19 号重结晶米 → 249 号清洁复合棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(75),
                ModItems.byCode(19),
                new ItemStack(ModItems.byCode(249)));

        // #250：锤米台 —— 左上角 80 号超懒合金锭 + 左下角 147 号熔融混合结晶 → 250 号超懒合金棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(80),
                ModItems.byCode(147),
                new ItemStack(ModItems.byCode(250)));

        // #32：压饭机 —— 9 个 30 号饭桶 → 1 个 32 号压缩饭桶。
        // 压饭机仅按输入槽 shrink(1) 消耗，不触发原版合成剩余物逻辑，因此【桶不返还】。
        Item[] ninePails = new Item[RicePressBlockEntity.INPUT_SLOTS];
        Item pail = ModItems.byCode(30);
        for (int i = 0; i < ninePails.length; i++) {
            ninePails[i] = pail;
        }
        RicePressBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(32)), ninePails);

        // #77：电离台 —— 左上角 76 号粗制超懒合金锭 + 左下角 84 号清洁米沉离液，
        // 处理 2s（40t）→ 77 号超懒合金离子态（输入无序，两槽各消耗 1 个）
        IonizationTableBlockEntity.registerRecipe(
                ModItems.byCode(76),
                ModItems.byCode(84),
                new ItemStack(ModItems.byCode(77)),
                40);

        // #78：沉淀台 —— 左上角 77 号超懒合金离子态 + 左下角 84 号清洁米沉离液，
        // 处理 2s（40t）→ 78 号超懒合金粉（两槽各消耗 1 个）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.byCode(77),
                ModItems.byCode(84),
                new ItemStack(ModItems.byCode(78)),
                40);

        // #62：沉淀台 —— 单槽 61 号米果渣（另一槽留空）→ 一次产出 3 个 62 号米果汁
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.byCode(61),
                new ItemStack(ModItems.byCode(62), 3));

        // #71：沉淀台 —— 单槽 70 号米果混合渣（另一槽留空）→ 一次产出 3 个 71 号米果清洁液
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.byCode(70),
                new ItemStack(ModItems.byCode(71), 3));

        // #67：离解台 —— 上槽 63 号米果混合液 + 下槽 1 号米（离解介质）→ 67 号米果解析液
        DissociationTableBlockEntity.registerRecipe(
                ModItems.byCode(63),
                ModItems.byCode(1),
                new ItemStack(ModItems.byCode(67)));

        // #165：淬剑台 —— 左上 124 号超懒合金剑 + 右上 162 号上古剑意 + 中间 168 号淬剑液
        // → 165 号超懒神剑·残缺·贰（需按"开始"按钮加工一轮）
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(165)),
                ModItems.byCode(124),
                ModItems.byCode(162),
                ModItems.byCode(168));

        // #170：淬剑台 —— 左上 165 残缺·贰 + 右上 169 远古剑魄 + 中间 168 淬剑液 → 170 残次
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(170)),
                ModItems.byCode(165),
                ModItems.byCode(169),
                ModItems.byCode(168));

        // #172：淬剑台 —— 左上 170 残次 + 右上 171 太古剑魄 + 中间 168 淬剑液 → 172 赝品
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(172)),
                ModItems.byCode(170),
                ModItems.byCode(171),
                ModItems.byCode(168));

        // #176：淬剑台 —— 左上 172 赝品 + 右上 175 神剑鉴玉 + 中间 168 淬剑液 → 176 瑕疵
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(176)),
                ModItems.byCode(172),
                ModItems.byCode(175),
                ModItems.byCode(168));

        // #179：淬剑台 —— 左上 178 超懒剑法 + 右上 176 瑕疵 + 中间 168 淬剑液 → 179 完整
        SwordQuenchingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(179)),
                ModItems.byCode(178),
                ModItems.byCode(176),
                ModItems.byCode(168));

        // #288：碾磨台 —— 左列上 265 残堕锭 / 左列中 80 超懒合金锭，左列下与右列三格
        // 共 4 个 273 残堕深渊岩 → 288 磨制残堕深渊岩（槽序：左上/左中/左下/右上/右中/右下）
        Item abyssRock = ModItems.byCode(273);
        GrindingTableBlockEntity.registerRecipe(
                new ItemStack(ModItems.byCode(288)),
                ModItems.byCode(265),
                ModItems.byCode(80),
                abyssRock,
                abyssRock,
                abyssRock,
                abyssRock);

        // #290：杵界台 —— 2x2 四格各放 1 个 286 号粗制残堕世界精华（无序）→ 290 磨制残堕世界精华
        Item coarseEssence = ModItems.byCode(286);
        PestleRealmTableBlockEntity.registerShapeless(
                new ItemStack(ModItems.byCode(290)),
                coarseEssence, coarseEssence, coarseEssence, coarseEssence);

        // ================= 食物加工链（35 条）=================
        // 用户机器代号：i=搅碎器 ii=搅拌器 iv=煮锅(2x2) v=大煮锅(3x3) vi=超大煮锅(4x4)
        // viii=铁锅(4 槽菱形) vv=烤箱(3x3)；本批未用到 iii 小煮锅、vii 巨无霸大大大煮锅。
        // 额外槽物品：碗=316 号大碗；玻璃瓶=原版 GLASS_BOTTLE（作为配料被消耗，机器不返还空瓶）；
        // 干海带=原版 DRIED_KELP；#357 的"额外7"=7 号米木棍。未给额外槽物品时传 null（该槽必须为空）。
        Item bowl = ModItems.byCode(316);
        Item bottle = Items.GLASS_BOTTLE;
        Item driedKelp = Items.DRIED_KELP;

        // ---- i 搅碎器（有序三槽：左上 / 左下=额外槽 / 右上）----

        // #322 天脑汁：296 界天脑花（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(322)),
                ModItems.byCode(296), bottle, null);

        // #324 米堕汁：304 特色堕落米（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(324)),
                ModItems.byCode(304), bottle, null);

        // #326 凤肉泥：302 米凤肉块（左上），其余槽空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(326)),
                ModItems.byCode(302), null, null);

        // #332 油瓶：330 米蜃油脂（左上）+ 331 染鲛油脂（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(332)),
                ModItems.byCode(330), bottle, ModItems.byCode(331));

        // #337 蛟鲛肉泥：306 浅鲛尾（左上）+ 308 望米伤鲛足（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(337)),
                ModItems.byCode(306), null, ModItems.byCode(308));

        // #338 凤蜃肉泥：302 米凤肉块（左上）+ 310 半蜃肉片（右上）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(338)),
                ModItems.byCode(302), null, ModItems.byCode(310));

        // #342 花香油：300 棘花瓣（左上）+ 额外玻璃瓶（左下），右上必须空
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(342)),
                ModItems.byCode(300), bottle, null);

        // #350 米雪茶：324 米堕汁（左上）+ 84 清洁米沉离液（右上）+ 额外玻璃瓶（左下）
        CrusherBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(350)),
                ModItems.byCode(324), bottle, ModItems.byCode(84));

        // ---- ii 搅拌器（registerRecipe(result, 额外槽8物品, 槽0~7 无序配料...)；
        //      碗/瓶必须放槽 8；无额外物传 null，此时 9 槽整体无序、槽 8 可当第 9 槽）----

        // #323 酱脑：322 天脑汁 + 312 香袋，额外玻璃瓶（槽 8）
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(323)), bottle,
                ModItems.byCode(322), ModItems.byCode(312));

        // #327 凤球：326 凤肉泥×1 + 313 香粉×7，无额外槽物品
        Item fragrantPowder = ModItems.byCode(313);
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(327)), null,
                ModItems.byCode(326),
                fragrantPowder, fragrantPowder, fragrantPowder,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder);

        // #334 腌料：312 香袋 + 332 油瓶 + 300 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(334)), null,
                ModItems.byCode(312), ModItems.byCode(332), ModItems.byCode(300));

        // #335 腌鹫翅：314 噬鹫翅 + 334 腌料
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(335)), null,
                ModItems.byCode(314), ModItems.byCode(334));

        // #339 珍贵调料：323 酱脑 + 324 米堕汁 + 332 油瓶 + 334 腌料 + 312 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(339)), null,
                ModItems.byCode(323), ModItems.byCode(324), ModItems.byCode(332),
                ModItems.byCode(334), ModItems.byCode(312));

        // #341 烧烤粉：313 香粉×4 + 300 棘花瓣
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(341)), null,
                fragrantPowder, fragrantPowder, fragrantPowder, fragrantPowder,
                ModItems.byCode(300));

        // #343 复合油：330 米蜃油脂 + 322 天脑汁 + 342 花香油
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(343)), null,
                ModItems.byCode(330), ModItems.byCode(322), ModItems.byCode(342));

        // #352 天堕茶：324 米堕汁×2 + 322 天脑汁×2 + 84 清洁米沉离液×4（8 个普通槽），额外碗（槽 8）
        Item juice324 = ModItems.byCode(324);
        Item juice322 = ModItems.byCode(322);
        Item liquid84 = ModItems.byCode(84);
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(352)), bowl,
                juice324, juice324, juice322, juice322,
                liquid84, liquid84, liquid84, liquid84);

        // #354 腌翅包：326 凤肉泥 + 335 腌鹫翅 + 312 香袋
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(354)), null,
                ModItems.byCode(326), ModItems.byCode(335), ModItems.byCode(312));

        // #355 腌肉片：310 半蜃肉片×5 + 334 腌料
        Item mirageSlice = ModItems.byCode(310);
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(355)), null,
                mirageSlice, mirageSlice, mirageSlice, mirageSlice, mirageSlice,
                ModItems.byCode(334));

        // #358 饭团：305 熟特色堕落米×8 + 312 香袋（共 9 料，无额外物，槽 8 当普通槽）
        Item cookedFallenRice = ModItems.byCode(305);
        MixerBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(358)), null,
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice,
                ModItems.byCode(312),
                cookedFallenRice, cookedFallenRice, cookedFallenRice, cookedFallenRice);

        // ---- iv 煮锅（2x2 网格行优先 + 右上额外槽）----

        // #321 酱香双鲛尾：323 酱脑 + 324 米堕汁 + 306 浅鲛尾×2，额外碗
        CookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(321)), bowl,
                ModItems.byCode(323), ModItems.byCode(324),
                ModItems.byCode(306), ModItems.byCode(306));

        // ---- v 大煮锅（3x3 网格行优先 + 右上额外槽）----

        // #319 天虚棘脑汤：298 虚无触足 / 296 界天脑花 / 298 虚无触足
        //                                300 棘花瓣 / 300 / 300
        //                                313 香粉 / 300 / 313，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(319)), bowl,
                ModItems.byCode(298), ModItems.byCode(296), ModItems.byCode(298),
                ModItems.byCode(300), ModItems.byCode(300), ModItems.byCode(300),
                ModItems.byCode(313), ModItems.byCode(300), ModItems.byCode(313));

        // #325 占位产物（placeholder_325，转正前无法合法获得）：用户主动给配方，照直译注册。
        // 327 凤球 / 308 望米伤鲛足 / 327 凤球；300 / 308 / 300；313 香粉×3，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(325)), bowl,
                ModItems.byCode(327), ModItems.byCode(308), ModItems.byCode(327),
                ModItems.byCode(300), ModItems.byCode(308), ModItems.byCode(300),
                ModItems.byCode(313), ModItems.byCode(313), ModItems.byCode(313));

        // #348 海兽相交饭：337 蛟鲛肉泥×3；306 浅鲛尾×3；306 / 339 珍贵调料 / 306，额外碗
        LargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(348)), bowl,
                ModItems.byCode(337), ModItems.byCode(337), ModItems.byCode(337),
                ModItems.byCode(306), ModItems.byCode(306), ModItems.byCode(306),
                ModItems.byCode(306), ModItems.byCode(339), ModItems.byCode(306));

        // ---- vi 超大煮锅（4x4 网格行优先 + 右上额外槽；null 位该格必须为空）----

        // #336 堕米肉丸汤：337×4；338×4；339 / 298 / 298 / 300；310 / 310 / 300 / 300，额外碗
        Item paste337 = ModItems.byCode(337);
        Item paste338 = ModItems.byCode(338);
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(336)), bowl,
                paste337, paste337, paste337, paste337,
                paste338, paste338, paste338, paste338,
                ModItems.byCode(339), ModItems.byCode(298), ModItems.byCode(298), ModItems.byCode(300),
                ModItems.byCode(310), ModItems.byCode(310), ModItems.byCode(300), ModItems.byCode(300));

        // #345 龙凤花汤：308×4；302×4；339 / 342 / 342 / 339；空 / 342 / 空 / 空，额外碗
        Item mermaidLeg = ModItems.byCode(308);
        Item phoenixMeat = ModItems.byCode(302);
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(345)), bowl,
                mermaidLeg, mermaidLeg, mermaidLeg, mermaidLeg,
                phoenixMeat, phoenixMeat, phoenixMeat, phoenixMeat,
                ModItems.byCode(339), ModItems.byCode(342), ModItems.byCode(342), ModItems.byCode(339),
                null, ModItems.byCode(342), null, null);

        // #349 佛幻想：338×3+337；337×3+335×... 按原文逐格：
        // 行1 338 338 338 337；行2 337 337 335 335；行3 335 342 342 339；行4 339 空 空 空，额外碗
        Item wing335 = ModItems.byCode(335);
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(349)), bowl,
                paste338, paste338, paste338, paste337,
                paste337, paste337, wing335, wing335,
                wing335, ModItems.byCode(342), ModItems.byCode(342), ModItems.byCode(339),
                ModItems.byCode(339), null, null, null);

        // #356 肉片粥：行1 355 355 342 355；行2-4 全为 304 特色堕落米（12 个），额外碗
        Item slice355 = ModItems.byCode(355);
        Item fallenRice = ModItems.byCode(304);
        ExtraLargeCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(356)), bowl,
                slice355, slice355, ModItems.byCode(342), slice355,
                fallenRice, fallenRice, fallenRice, fallenRice,
                fallenRice, fallenRice, fallenRice, fallenRice,
                fallenRice, fallenRice, fallenRice, fallenRice);

        // ---- viii 铁锅（4 槽顺序：左上 / 右上 / 左下 / 右下 + 右上侧额外槽）----

        // #320 香混肉片：355 腌肉片×2 + 300 棘花瓣 + 313 香粉，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(320)), bowl,
                slice355, slice355, ModItems.byCode(300), ModItems.byCode(313));

        // #340 油爆虚无触：298 虚无触足×2 + 343 复合油 + 332 油瓶，额外碗
        Item tentacle = ModItems.byCode(298);
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(340)), bowl,
                tentacle, tentacle, ModItems.byCode(343), ModItems.byCode(332));

        // #344 蛟足炒饭：308 望米伤鲛足 + 343 复合油 + 304 特色堕落米×2，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(344)), bowl,
                mermaidLeg, ModItems.byCode(343), fallenRice, fallenRice);

        // #351 煎龙凤饼：327 凤球 + 308 望米伤鲛足 + 339 珍贵调料 + 343 复合油，额外碗
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(351)), bowl,
                ModItems.byCode(327), mermaidLeg, ModItems.byCode(339), ModItems.byCode(343));

        // #353 凤包翅：354 腌翅包 + 342 花香油 + 300 棘花瓣（右下空），无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(353)), null,
                ModItems.byCode(354), ModItems.byCode(342), ModItems.byCode(300), null);

        // #359 海带片：332 油瓶 + 原版干海带×3，无额外槽物品
        IronWokBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(359)), null,
                ModItems.byCode(332), driedKelp, driedKelp, driedKelp);

        // ---- vv 烤箱（3x3 网格行优先 + 右上额外槽）----

        // #329 酱烤鹫翅：335 腌鹫翅 / 332 油瓶 / 330 米蜃油脂；313 香粉×3；335 / 332 / 330，额外碗
        Item grease330 = ModItems.byCode(330);
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(329)), bowl,
                wing335, ModItems.byCode(332), grease330,
                fragrantPowder, fragrantPowder, fragrantPowder,
                wing335, ModItems.byCode(332), grease330);

        // #347 花香蛟尾：306 浅鲛尾 / 342 花香油 / 334 腌料，三行同构，额外碗
        Item mermaidTail = ModItems.byCode(306);
        Item fragranceOil = ModItems.byCode(342);
        Item marinade = ModItems.byCode(334);
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(347)), bowl,
                mermaidTail, fragranceOil, marinade,
                mermaidTail, ModItems.byCode(339), marinade,
                mermaidTail, fragranceOil, marinade);

        // #357 肢体烤串：306 / 341 烧烤粉 / 298；308 / 341 / 302；341 / 空 / 341，
        // 额外槽=7 号米木棍（用户原文"额外7"）
        Item riceStick = ModItems.byCode(7);
        Item bbqPowder = ModItems.byCode(341);
        OvenBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(357)), riceStick,
                mermaidTail, bbqPowder, tentacle,
                mermaidLeg, bbqPowder, phoenixMeat,
                bbqPowder, null, bbqPowder);

        // ---- vii 巨无霸大大大煮锅（5x5 网格行优先 25 格 + 右上额外槽）----

        // #364 超级大乱炖：5x5 摆 22 种料理 + 3 个 216 米米大杂烩，额外碗。
        // 行1 319 320 321 216 329；行2 336 340 344 345 346；行3 347 348 349 350 351；
        // 行4 352 353 356 357 360；行5 361 362 363 216 216。
        // （用户修订：原第 4 格 325、第 24 格 364 均改为 216，消除自引用与占位物依赖）
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(364)), bowl,
                ModItems.byCode(319), ModItems.byCode(320), ModItems.byCode(321),
                ModItems.byCode(216), ModItems.byCode(329),
                ModItems.byCode(336), ModItems.byCode(340), ModItems.byCode(344),
                ModItems.byCode(345), ModItems.byCode(346),
                ModItems.byCode(347), ModItems.byCode(348), ModItems.byCode(349),
                ModItems.byCode(350), ModItems.byCode(351),
                ModItems.byCode(352), ModItems.byCode(353), ModItems.byCode(356),
                ModItems.byCode(357), ModItems.byCode(360),
                ModItems.byCode(361), ModItems.byCode(362), ModItems.byCode(363),
                ModItems.byCode(216), ModItems.byCode(216));

        // #346 蜃楼幻想：5x5 边框与内点（0=该格必须空），额外碗。
        // 行1 338 334 334 334 338；行2 338 空 334 空 338；行3 338 334 空 343 338；
        // 行4 338 空 343 空 338；行5 338 334 343 343 338。
        // 共 338×10（左右两列）、334×6、343×4、空格 5。
        Item paste338b = ModItems.byCode(338);
        Item marinade334 = ModItems.byCode(334);
        Item compoundOil343 = ModItems.byCode(343);
        JumboMegaCookingPotBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(346)), bowl,
                paste338b, marinade334, marinade334, marinade334, paste338b,
                paste338b, null,       marinade334, null,         paste338b,
                paste338b, marinade334, null,       compoundOil343, paste338b,
                paste338b, null,       compoundOil343, null,      paste338b,
                paste338b, marinade334, compoundOil343, compoundOil343, paste338b);

        // ---- 汲血台（槽0=左上 / 槽1=左下，各消耗 1 个；支持上槽标签匹配）----

        // #167 血瓶：左上任意肉类（mimiworld:meats 标签，生熟皆可）+ 左下原版玻璃瓶 → 167
        BloodlettingTableBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(167)),
                ModItemTags.MEATS, Items.GLASS_BOTTLE);

        // ---- 凝锭台（5x5=25 格工作台式即时合成，有序图案；空格' '=该格必须为空）----

        // #293 残堕世界精华锭：深染十素材围边 + 6 个 256 盗米之心 + 9 个 291 磨制残堕精华液
        // 行1 276 277 278 279 280；行2-4 256 291 291 291 256；行5 281 282 283 284 285
        Map<Character, Item> keys293 = Map.ofEntries(
                Map.entry('A', ModItems.byCode(276)),
                Map.entry('B', ModItems.byCode(277)),
                Map.entry('C', ModItems.byCode(278)),
                Map.entry('D', ModItems.byCode(279)),
                Map.entry('E', ModItems.byCode(280)),
                Map.entry('F', ModItems.byCode(256)),
                Map.entry('G', ModItems.byCode(291)),
                Map.entry('H', ModItems.byCode(281)),
                Map.entry('I', ModItems.byCode(282)),
                Map.entry('J', ModItems.byCode(283)),
                Map.entry('K', ModItems.byCode(284)),
                Map.entry('L', ModItems.byCode(285)));
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.byCode(293)),
                new String[]{
                        "ABCDE",
                        "FGGGF",
                        "FGGGF",
                        "FGGGF",
                        "HIJKL"
                }, keys293);

        // #294 世界之泪：4 个 240 最懒终末龙头（四角）+ 8 个 258 米魂之泪（十字外沿）
        // + 13 个 293 残堕世界精华锭（中心菱形）
        // 行1/5 240 258 293 258 240；行2/4 258 293 293 293 258；行3 全 293
        Map<Character, Item> keys294 = Map.of(
                'A', ModItems.byCode(240),
                'B', ModItems.byCode(258),
                'X', ModItems.byCode(293));
        IngotCondensationTableBlockEntity.registerShaped(new ItemStack(ModItems.byCode(294)),
                new String[]{
                        "ABXBA",
                        "BXXXB",
                        "XXXXX",
                        "BXXXB",
                        "ABXBA"
                }, keys294);

        // ---- 凝锭台：25 个相同材料摆满即得 ----

        // #384 龙骨架构：25 个 380 龙骨
        Item dragonBone = ModItems.byCode(380);
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.byCode(384)),
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone,
                dragonBone, dragonBone, dragonBone, dragonBone, dragonBone);

        // #385 龙血精华：25 个 377 龙血
        Item dragonBlood = ModItems.byCode(377);
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.byCode(385)),
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood,
                dragonBlood, dragonBlood, dragonBlood, dragonBlood, dragonBlood);

        // #386 龙鳞块：25 个 378 龙鳞
        Item dragonScale = ModItems.byCode(378);
        IngotCondensationTableBlockEntity.registerShapeless(new ItemStack(ModItems.byCode(386)),
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale,
                dragonScale, dragonScale, dragonScale, dragonScale, dragonScale);
    }
}
