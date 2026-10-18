package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.item.CleansingCompoundArmorItem;
import com.mimiworld.mimiworld.item.CompoundRiceMealArmorItem;
import com.mimiworld.mimiworld.item.EffectFoodItem;
import com.mimiworld.mimiworld.item.MimiArrowItem;
import com.mimiworld.mimiworld.item.MimiTotemItem;
import com.mimiworld.mimiworld.item.MonkeyKingRuyiStaffItem;
import com.mimiworld.mimiworld.item.RainbowNameItem;
import com.mimiworld.mimiworld.item.SuperLazyAlloyArmorItem;
import com.mimiworld.mimiworld.item.SuperLazyWingsChestplateItem;
import com.mimiworld.mimiworld.item.RemnantFallenCardinalSinArmorItem;
import com.mimiworld.mimiworld.item.RiceCrystalAlloyArmorItem;
import com.mimiworld.mimiworld.item.SuperHeavyCrystalArmorItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import com.mimiworld.mimiworld.item.MimiBowItem;
import com.mimiworld.mimiworld.item.MubangongBlankItem;
import com.mimiworld.mimiworld.item.MubangongBowItem;
import com.mimiworld.mimiworld.item.RiceCrystalBowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

/**
 * 米米世界物品注册类。所有物品类新内容统一在此注册。
 * 注册的物品会被 ModCreativeTabs 按命名空间自动收录进「米米世界」创造标签页。
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MimiWorld.MODID);

    /** 注册物品（所有物品统一经此入口注册到 ITEMS 延迟注册表）。 */
    private static RegistryObject<Item> registerItem(String name, Supplier<Item> factory) {
        return ITEMS.register(name, factory);
    }

    /**
     * 熟食食物属性：仅饱食度/饱和度，无负面效果（后续批次的各「熟」食材使用）。
     *
     * @param nutrition  饱食度（鸡腿数，1=半个鸡腿）
     * @param saturation 饱和度系数
     */
    private static FoodProperties cookedFood(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation)
                .build();
    }

    /**
     * 生食食物属性：模仿原版生肉，除饱食度/饱和度外按概率附加「饥饿 I 30 秒」（600 tick）。
     *
     * @param hungerChance 附加饥饿效果的概率（0.0~1.0），0 表示无负面效果
     */
    private static FoodProperties rawFood(int nutrition, float saturation, float hungerChance) {
        FoodProperties.Builder builder = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation);
        if (hungerChance > 0.0F) {
            builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), hungerChance);
        }
        return builder.build();
    }

    // 食用效果持续时间常量（tick）
    private static final int SEC_10 = 200;
    private static final int SEC_30 = 600;
    private static final int MIN_1 = 1200;
    private static final int MIN_2 = 2400;
    private static final int MIN_3 = 3600;
    private static final int MIN_5 = 6000;
    private static final int MIN_15 = 18000;
    private static final int MIN_20 = 24000;
    private static final int MIN_30 = 36000;
    private static final int HOUR_1 = 72000;

    /**
     * 带食用效果的食物属性：只设饱食度，饱和度取与饱食度同值。
     * （饱和度换算为 nutrition × saturationMod × 2，故 saturationMod = 0.5 时两者相等。）
     */
    private static FoodProperties effectFood(int hunger) {
        return new FoodProperties.Builder().nutrition(hunger).saturationMod(0.5F).build();
    }

    /** 声明一条食用效果：等级 N → 放大器 N-1（一级 = 0），durationTicks 为持续时间（tick）。 */
    private static EffectFoodItem.AppliedEffect effect(Supplier<MobEffect> effect, int level, int durationTicks) {
        return new EffectFoodItem.AppliedEffect(effect, level - 1, durationTicks);
    }

    /** 注册「饱食度 + 若干食用效果」的食物。 */
    private static RegistryObject<Item> registerEffectFood(String name, int hunger,
                                                           EffectFoodItem.AppliedEffect... effects) {
        return registerItem(name, () -> new EffectFoodItem(
                new Item.Properties().stacksTo(64).food(effectFood(hunger)), List.of(effects)));
    }


    // ============================ 物品注册区 ============================

    // 米（Rice）：可食用，最大堆叠 64；食用后 100% 触发四种负面效果（20 tick = 1 秒，amplifier 从 0 起算）
    public static final FoodProperties RICE_FOOD = new FoodProperties.Builder()
            .nutrition(1)            // 饱食度回复（饥饿栏鸡腿数，后续可调）
            .saturationMod(0.1F)     // 饱和度，后续可调
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 2), 1.0F)   // 反胃 III 10s
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)       // 中毒 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F) // 缓慢 I 10s
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)       // 饥饿 I 30s
            .build();

    // 米
    public static final RegistryObject<Item> RICE = registerItem("rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FOOD)));

    // 熟米（Cooked Rice）：食用回复 2.5 个鸡腿（nutrition 单位=半个鸡腿 → 5）；
    // 实际饱和度 = 2 × nutrition × saturationMod = 2×5×0.3 = 3；100% 反胃 I 5s；食用耗时 1s（默认食物为 32 tick）
    public static final FoodProperties COOKED_RICE_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F) // 反胃 I 5s
            .build();

    // 熟米
    public static final RegistryObject<Item> COOKED_RICE = registerItem("cooked_rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_RICE_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 20; // 食用时间 1 秒
                }
            });

    // 米团：可投掷，命中造成 1 伤害 + 反胃 I/缓慢 I 各 10s，投掷冷却 2s（逻辑见 RiceBallItem / RiceBallProjectile）
    public static final RegistryObject<Item> RICE_BALL = registerItem("rice_ball",
            () -> new RiceBallItem(new Item.Properties().stacksTo(64)));

    // 熟米团：可投掷，命中 2.5 伤害 + 缓慢 III/失明 II 各 10s，投掷冷却 2s（逻辑见 CookedRiceBallItem / CookedRiceBallProjectile）
    public static final RegistryObject<Item> COOKED_RICE_BALL = registerItem("cooked_rice_ball",
            () -> new CookedRiceBallItem(new Item.Properties().stacksTo(64)));

    // 米原木：方块物品，放置后对应 ModBlocks.RICE_LOG；破坏掉落自身（loot table），斧/挖掘等级 1 由方块标签控制
    public static final RegistryObject<Item> RICE_LOG = registerItem("rice_log",
            () -> new BlockItem(ModBlocks.RICE_LOG.get(), new Item.Properties()));

    // 米木板：方块物品，放置后对应 ModBlocks.RICE_PLANKS；破坏掉落自身，斧可挖、挖掘等级 0（无 needs_*_tool 标签）
    public static final RegistryObject<Item> RICE_PLANKS = registerItem("rice_planks",
            () -> new BlockItem(ModBlocks.RICE_PLANKS.get(), new Item.Properties()));

    // 米楼梯：方块物品，放置后对应 ModBlocks.RICE_STAIRS；破坏掉落自身，斧可挖、挖掘等级 0
    public static final RegistryObject<Item> RICE_STAIRS = registerItem("rice_stairs",
            () -> new BlockItem(ModBlocks.RICE_STAIRS.get(), new Item.Properties()));

    // 米台阶：方块物品，放置后对应 ModBlocks.RICE_SLAB；破坏掉落自身，斧可挖、挖掘等级 0
    public static final RegistryObject<Item> RICE_SLAB = registerItem("rice_slab",
            () -> new BlockItem(ModBlocks.RICE_SLAB.get(), new Item.Properties()));

    // 米栅栏：方块物品，放置后对应 ModBlocks.RICE_FENCE；破坏掉落自身，斧可挖、挖掘等级 0
    public static final RegistryObject<Item> RICE_FENCE = registerItem("rice_fence",
            () -> new BlockItem(ModBlocks.RICE_FENCE.get(), new Item.Properties()));

    // 米木棍：普通材料物品，最大堆叠 64；熔炉燃料 300t（可烧 1.5 个物品，见 event.ModFuelEvents）
    public static final RegistryObject<Item> RICE_STICK = registerItem("rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米树叶：方块物品，放置后对应 ModBlocks.RICE_LEAVES；破坏掉落自身，剪刀可快速采集、挖掘等级 0，燃料 100t
    public static final RegistryObject<Item> RICE_LEAVES = registerItem("rice_leaves",
            () -> new BlockItem(ModBlocks.RICE_LEAVES.get(), new Item.Properties()));

    // 米桶：米汤桶，最大堆叠 1；RiceBucketItem 继承 BucketItem，
    // 构造器内部绑定 fluid.ModFluids.RICE_SOUP（米汤源流体），行为同原版水桶
    public static final RegistryObject<Item> RICE_BUCKET = registerItem("rice_bucket",
            () -> new RiceBucketItem(new Item.Properties().stacksTo(1)));

    // 米树苗：方块物品，放置后对应 ModBlocks.RICE_SAPLING；可骨粉催熟/随机刻长成米树（白桦形态，米原木+米树叶）
    public static final RegistryObject<Item> RICE_SAPLING = registerItem("rice_sapling",
            () -> new BlockItem(ModBlocks.RICE_SAPLING.get(), new Item.Properties()));

    // 硬化米：石头类方块物品，放置后对应 ModBlocks.HARDENED_RICE
    public static final RegistryObject<Item> HARDENED_RICE = registerItem("hardened_rice",
            () -> new BlockItem(ModBlocks.HARDENED_RICE.get(), new Item.Properties()));

    // 深层硬化米：石头类方块物品，放置后对应 ModBlocks.DEEPSLATE_HARDENED_RICE
    public static final RegistryObject<Item> DEEPSLATE_HARDENED_RICE = registerItem("deepslate_hardened_rice",
            () -> new BlockItem(ModBlocks.DEEPSLATE_HARDENED_RICE.get(), new Item.Properties()));

    // 基米：基岩类方块物品，放置后对应 ModBlocks.RICE_BEDROCK；仅创造模式获取
    public static final RegistryObject<Item> RICE_BEDROCK = registerItem("rice_bedrock",
            () -> new BlockItem(ModBlocks.RICE_BEDROCK.get(), new Item.Properties()));

    // 米草食物属性：nutrition=1 = 0.5 个鸡腿（nutrition 单位为半个鸡腿）；saturationMod 0.5；
    // 100% 反胃 I 5s + 缓慢 II（amplifier=1）5s；食用耗时 0.5s 在 RiceGrassItem#getUseDuration 中固定为 10t
    public static final FoodProperties RICE_GRASS_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F)       // 反胃 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), 1.0F) // 缓慢 II 5s
            .build();

    // 米草：可放置的草类植物方块物品（自定义 RiceGrassItem，右键可种草也可食用）；
    // 放置后对应 ModBlocks.RICE_GRASS，该植物空手破坏不掉落，仅剪刀采集掉本物品
    public static final RegistryObject<Item> RICE_GRASS = registerItem("rice_grass",
            () -> new RiceGrassItem(ModBlocks.RICE_GRASS.get(),
                    new Item.Properties().stacksTo(64).food(RICE_GRASS_FOOD)));

    // 米果食物属性（定位类似原版苹果）：nutrition=5 = 2.5 个鸡腿；saturationMod=2.0F
    // （实际饱和度 = 2×5×2 = 20）；100% 漂浮 I 30s + 反胃 I 5s + 虚弱 I 30s
    public static final FoodProperties RICE_FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(2.0F)
            .effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 600, 0), 1.0F) // 漂浮 I 30s
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F)  // 反胃 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), 1.0F)  // 虚弱 I 30s
            .build();

    // 米果：普通食物物品，最大堆叠 64；食用耗时 2s（默认食物 32t ≈ 1.6s，这里覆盖为 40t）
    public static final RegistryObject<Item> RICE_FRUIT = registerItem("rice_fruit",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FRUIT_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 40; // 食用时间 2 秒
                }
            });

    // 结晶米：类似原版锭的普通矿石材料，最大堆叠 64
    public static final RegistryObject<Item> CRYSTALLIZED_RICE = registerItem("crystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 结晶米矿石：石头类矿石方块物品，放置后对应 ModBlocks.CRYSTALLIZED_RICE_ORE；
    // 下界合金镐才能采集。普通开采掉结晶米（时运可增产），精准采集才掉矿石本身；
    // 非精准开采另掉 3~7 经验球；矿石也可熔炉/高炉烧炼成结晶米
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_ORE = registerItem("crystallized_rice_ore",
            () -> new BlockItem(ModBlocks.CRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 重结晶米矿石：深板岩质地矿石，
    // 放置后对应 ModBlocks.RECRYSTALLIZED_RICE_ORE；下界合金镐采集。普通开采掉重结晶米（时运可增产），
    // 精准采集才掉矿石本身；非精准开采另掉 3~7 经验球；矿石也可烧炼得重结晶米
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE_ORE = registerItem("recrystallized_rice_ore",
            () -> new BlockItem(ModBlocks.RECRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 重结晶米：精炼锭类普通材料，最大堆叠 64；由重结晶米矿石熔炉/高炉烧炼得到
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE = registerItem("recrystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 可燃米：普通材料，最大堆叠 64；
    // 作为熔炉燃料可烧 32 个物品（6400 tick），燃料值在 event.ModFuelEvents 中赋予
    public static final RegistryObject<Item> COMBUSTIBLE_RICE = registerItem("combustible_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 可燃米矿石：石头类矿石方块物品，放置后对应 ModBlocks.COMBUSTIBLE_RICE_ORE；
    // 下界合金镐采集。普通开采掉可燃米（时运可增产），精准采集才掉矿石本身；
    // 非精准开采另掉 3~7 经验球；矿石也可熔炉/高炉烧炼得可燃米
    public static final RegistryObject<Item> COMBUSTIBLE_RICE_ORE = registerItem("combustible_rice_ore",
            () -> new BlockItem(ModBlocks.COMBUSTIBLE_RICE_ORE.get(), new Item.Properties()));

    // 泥米：泥巴质地方块物品，放置后对应 ModBlocks.MUD_RICE；铁铲（挖掘等级2）采集
    public static final RegistryObject<Item> MUD_RICE = registerItem("mud_rice",
            () -> new BlockItem(ModBlocks.MUD_RICE.get(), new Item.Properties()));

    // 强化米棍，Reinforced Rice Stick：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> REINFORCED_RICE_STICK = registerItem("reinforced_rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米棍，Rice Baton：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_BATON = registerItem("rice_baton",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米粉，Rice Flour：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_FLOUR = registerItem("rice_flour",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗米块，Coarse Rice Block：石头类方块物品，放置后对应 ModBlocks.COARSE_RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> COARSE_RICE_BLOCK = registerItem("coarse_rice_block",
            () -> new BlockItem(ModBlocks.COARSE_RICE_BLOCK.get(), new Item.Properties()));

    // 造米机，Rice Maker：石头类方块物品，放置后对应 ModBlocks.RICE_MAKER；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_MAKER = registerItem("rice_maker",
            () -> new BlockItem(ModBlocks.RICE_MAKER.get(), new Item.Properties()));

    // 米块，Rice Block：石头类方块物品，放置后对应 ModBlocks.RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_BLOCK = registerItem("rice_block",
            () -> new BlockItem(ModBlocks.RICE_BLOCK.get(), new Item.Properties()));

    // 锤米台，Rice Pounding Table：石头类方块物品，放置后对应 ModBlocks.RICE_POUNDING_TABLE；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_POUNDING_TABLE = registerItem("rice_pounding_table",
            () -> new BlockItem(ModBlocks.RICE_POUNDING_TABLE.get(), new Item.Properties()));

    // 饭桶，Rice Pail：饭汤桶，最大堆叠 1；RicePailItem 继承 BucketItem，
    // 构造器内部绑定 fluid.ModFluids.RICE_BROTH（饭汤源流体），行为同原版水桶
    public static final RegistryObject<Item> RICE_PAIL = registerItem("rice_pail",
            () -> new RicePailItem(new Item.Properties().stacksTo(1)));

    // 压饭机，Rice Press：石头类方块物品，放置后对应 ModBlocks.RICE_PRESS；铁镐（挖掘等级2）采集
    public static final RegistryObject<Item> RICE_PRESS = registerItem("rice_press",
            () -> new BlockItem(ModBlocks.RICE_PRESS.get(), new Item.Properties()));

    // 压缩饭桶，Compressed Rice Pail：普通物品，最大堆叠 64
    public static final RegistryObject<Item> COMPRESSED_RICE_PAIL = registerItem("compressed_rice_pail",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制饭晶，Coarse Crystallized Rice Meal：普通物品，最大堆叠 64
    public static final RegistryObject<Item> COARSE_CRYSTALLIZED_RICE_MEAL = registerItem("coarse_crystallized_rice_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 饭晶，Crystallized Rice Meal：普通物品，最大堆叠 64；注意区别于结晶米 CRYSTALLIZED_RICE
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_MEAL = registerItem("crystallized_rice_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 饭晶合金锭，Rice Crystal Alloy Ingot：普通物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_INGOT = registerItem("rice_crystal_alloy_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 饭晶锤，Rice Crystal Hammer：自定义工具类，镐类挖掘（钻石级 tier），近战 +7 伤害/-3.2 攻速；
    // 直接继承 Item（非 DiggerItem），stacksTo(1) 与普通工具一致，逻辑见 RiceCrystalHammerItem
    public static final RegistryObject<Item> RICE_CRYSTAL_HAMMER = registerItem("rice_crystal_hammer",
            RiceCrystalHammerItem::new);

    // 湿草麻，Wet Grass Fiber：纤维束材料
    public static final RegistryObject<Item> WET_GRASS_FIBER = registerItem("wet_grass_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制米麻，Coarse Rice Fiber：纤维束材料
    public static final RegistryObject<Item> COARSE_RICE_FIBER = registerItem("coarse_rice_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 强化湿草麻，Reinforced Wet Grass Fiber：纤维束材料
    public static final RegistryObject<Item> REINFORCED_WET_GRASS_FIBER = registerItem("reinforced_wet_grass_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 精制米麻，Refined Rice Fiber：纤维束材料
    public static final RegistryObject<Item> REFINED_RICE_FIBER = registerItem("refined_rice_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 湿草网，Wet Grass Net：编织网材料
    public static final RegistryObject<Item> WET_GRASS_NET = registerItem("wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制湿草网，Coarse Wet Grass Net：编织网材料
    public static final RegistryObject<Item> COARSE_WET_GRASS_NET = registerItem("coarse_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制米网，Coarse Rice Net：编织网材料
    public static final RegistryObject<Item> COARSE_RICE_NET = registerItem("coarse_rice_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 强化湿草网，Reinforced Wet Grass Net：编织网材料
    public static final RegistryObject<Item> REINFORCED_WET_GRASS_NET = registerItem("reinforced_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 精制强化湿草网，Refined Reinforced Wet Grass Net：编织网材料
    public static final RegistryObject<Item> REFINED_REINFORCED_WET_GRASS_NET = registerItem("refined_reinforced_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 精制强化米网，Refined Reinforced Rice Net：编织网材料
    public static final RegistryObject<Item> REFINED_REINFORCED_RICE_NET = registerItem("refined_reinforced_rice_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 饭晶合金锤，Rice Crystal Alloy Hammer：自定义工具类，
    // 饭晶锤的合金强化版；镐类挖掘（下界合金级 tier，speed 9.0），近战 +9 伤害/-3.1 攻速；
    // 直接继承 Item（非 DiggerItem），stacksTo(1) 与普通工具一致，逻辑见 RiceCrystalAlloyHammerItem
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_HAMMER = registerItem("rice_crystal_alloy_hammer",
            RiceCrystalAlloyHammerItem::new);

    // 饭晶弓，Rice Crystal Bow：GeckoLib 3D 模型的弓（RiceCrystalBowItem，伤害倍率 ×1.2），
    // 资源 geo/fanjinggong.geo.json + textures/item/fanjinggong.png + animations/fanjinggong.animation.json，
    // 最大堆叠 1，耐久 384（与原版弓一致）
    public static final RegistryObject<Item> RICE_CRYSTAL_BOW = registerItem("rice_crystal_bow",
            () -> new RiceCrystalBowItem(1.2D, new Item.Properties().durability(384)));

    // 高级锤米台，Advanced Rice Pounding Table：石头类方块物品，
    // 放置后对应 ModBlocks.ADVANCED_RICE_POUNDING_TABLE；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> ADVANCED_RICE_POUNDING_TABLE = registerItem("advanced_rice_pounding_table",
            () -> new BlockItem(ModBlocks.ADVANCED_RICE_POUNDING_TABLE.get(), new Item.Properties()));

    // 粗超结晶，Coarse Super Crystal：结晶材料，粗糙灰蓝晶簇含金色杂质
    public static final RegistryObject<Item> COARSE_SUPER_CRYSTAL = registerItem("coarse_super_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超结晶，Super Crystal：结晶材料，高纯度亮蓝晶簇含金核
    public static final RegistryObject<Item> SUPER_CRYSTAL = registerItem("super_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米矿石，Rice Ore：铁矿石类方块物品，放置后对应 ModBlocks.RICE_ORE；
    // 钻石镐（挖掘等级3）采集。普通开采掉米石（时运可增产），精准采集才掉矿石本身；
    // 非精准开采另掉 3~7 经验球；矿石也可熔炉/高炉烧炼成米石
    public static final RegistryObject<Item> RICE_ORE = registerItem("rice_ore",
            () -> new BlockItem(ModBlocks.RICE_ORE.get(), new Item.Properties()));

    // 米石，Rice Stone：嵌有奶白米纹的小石块，普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_STONE = registerItem("rice_stone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超结晶块，Block of Super Crystal：超结晶的压缩块，
    // 放置后对应 ModBlocks.SUPER_CRYSTAL_BLOCK；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> SUPER_CRYSTAL_BLOCK = registerItem("super_crystal_block",
            () -> new BlockItem(ModBlocks.SUPER_CRYSTAL_BLOCK.get(), new Item.Properties()));

    // 超重结晶，Super Heavy Crystal：高密度重晶材料，普通物品，最大堆叠 64
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL = registerItem("super_heavy_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 电离台，Ionization Table：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.IONIZATION_TABLE；钻石镐（挖掘等级3）采集，破坏掉落本身
    public static final RegistryObject<Item> IONIZATION_TABLE = registerItem("ionization_table",
            () -> new BlockItem(ModBlocks.IONIZATION_TABLE.get(), new Item.Properties()));

    // 沉淀台，Precipitation Table：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.PRECIPITATION_TABLE；钻石镐（挖掘等级3）采集，破坏掉落本身
    public static final RegistryObject<Item> PRECIPITATION_TABLE = registerItem("precipitation_table",
            () -> new BlockItem(ModBlocks.PRECIPITATION_TABLE.get(), new Item.Properties()));

    // 粗制复合米饭锭，Coarse Compound Rice Meal Ingot：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_COMPOUND_RICE_MEAL_INGOT =
            registerItem("coarse_compound_rice_meal_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 复合米饭板，Compound Rice Meal Plate：压制薄板半成品
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PLATE =
            registerItem("compound_rice_meal_plate",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 复合米饭锭，Compound Rice Meal Ingot：精制光滑成品锭
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_INGOT =
            registerItem("compound_rice_meal_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果渣，Rice Pomace：榨汁后的米果残渣
    public static final RegistryObject<Item> RICE_POMACE = registerItem("rice_pomace",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果汁，Rice Fruit Juice：药水瓶造型的普通物品（非流体、不可倒出）
    public static final RegistryObject<Item> RICE_FRUIT_JUICE = registerItem("rice_fruit_juice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果混合液，Rice Fruit Mixture：药水瓶造型的普通物品（非流体、不可倒出）
    public static final RegistryObject<Item> RICE_FRUIT_MIXTURE = registerItem("rice_fruit_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 离解台，Dissociation Table：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.DISSOCIATION_TABLE；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> DISSOCIATION_TABLE = registerItem("dissociation_table",
            () -> new BlockItem(ModBlocks.DISSOCIATION_TABLE.get(), new Item.Properties()));

    // ===== 米纸文书、米果加工液/渣、清洁复合与超懒合金材料链（均为普通物品，最大堆叠 64） =====

    // 米书，Rice Book
    public static final RegistryObject<Item> RICE_BOOK = registerItem("rice_book",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米纸，Rice Paper
    public static final RegistryObject<Item> RICE_PAPER = registerItem("rice_paper",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果解析液，Rice Fruit Analyzing Solution：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_ANALYZING_SOLUTION =
            registerItem("rice_fruit_analyzing_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果复合液，Rice Fruit Compound Solution：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_COMPOUND_SOLUTION =
            registerItem("rice_fruit_compound_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 米饭混合液，Rice Meal Mixture：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_MEAL_MIXTURE = registerItem("rice_meal_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果混合渣，Rice Fruit Mixed Pomace
    public static final RegistryObject<Item> RICE_FRUIT_MIXED_POMACE =
            registerItem("rice_fruit_mixed_pomace",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果清洁液，Rice Fruit Cleansing Solution：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_CLEANSING_SOLUTION =
            registerItem("rice_fruit_cleansing_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制清洁复合锭，Coarse Cleansing Compound Ingot：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_CLEANSING_COMPOUND_INGOT =
            registerItem("coarse_cleansing_compound_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁复合碎块，Cleansing Compound Chunk
    public static final RegistryObject<Item> CLEANSING_COMPOUND_CHUNK =
            registerItem("cleansing_compound_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁复合板，Cleansing Compound Plate
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PLATE =
            registerItem("cleansing_compound_plate",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁复合锭，Cleansing Compound Ingot：精制光滑成品锭
    public static final RegistryObject<Item> CLEANSING_COMPOUND_INGOT =
            registerItem("cleansing_compound_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制超懒合金锭，Coarse Super Lazy Alloy Ingot：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_SUPER_LAZY_ALLOY_INGOT =
            registerItem("coarse_super_lazy_alloy_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金离子态，Super Lazy Alloy Ionic Form：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_IONIC_FORM =
            registerItem("super_lazy_alloy_ionic_form",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金粉，Super Lazy Alloy Dust：粉末状普通物品
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_DUST =
            registerItem("super_lazy_alloy_dust",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金碎块，Super Lazy Alloy Chunk
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_CHUNK =
            registerItem("super_lazy_alloy_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金锭，Super Lazy Alloy Ingot：精制光滑成品锭
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_INGOT =
            registerItem("super_lazy_alloy_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 米石块，Block of Rice Stone：米石的压缩存储块，
    // 放置后对应 ModBlocks.RICE_STONE_BLOCK；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> RICE_STONE_BLOCK = registerItem("rice_stone_block",
            () -> new BlockItem(ModBlocks.RICE_STONE_BLOCK.get(), new Item.Properties()));

    // 可燃混合液，Combustible Mixture：药水瓶造型普通物品（非流体），橙红易燃液
    public static final RegistryObject<Item> COMBUSTIBLE_MIXTURE = registerItem("combustible_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 可燃固态物，Combustible Solid：带余烬裂纹的可燃硬块
    public static final RegistryObject<Item> COMBUSTIBLE_SOLID = registerItem("combustible_solid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁米沉离液，Cleansing Rice Separation Solution：
    // 药水瓶造型普通物品（非流体），瓶内上层清洁清液、下层米质沉离
    public static final RegistryObject<Item> CLEANSING_RICE_SEPARATION_SOLUTION =
            registerItem("cleansing_rice_separation_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ================== 装备材料链（米木<饭晶<饭晶合金<超重结晶<复合米饭<清洁复合<超懒合金） ==================

    // ---- 饭晶 / 超结晶基础武器 ----
    // 饭晶剑：珍珠米白晶质剑，ModItemTiers.RICE_CRYSTAL（修复材料结晶米）
    public static final RegistryObject<Item> RICE_CRYSTAL_SWORD = registerItem("rice_crystal_sword",
            () -> new SwordItem(ModItemTiers.RICE_CRYSTAL, 3, -2.4F, new Item.Properties().stacksTo(1)));

    // 超结晶剑：蓝晶金核剑，SUPER_CRYSTAL（修复材料超结晶）
    public static final RegistryObject<Item> SUPER_CRYSTAL_SWORD = registerItem("super_crystal_sword",
            () -> new SwordItem(ModItemTiers.SUPER_CRYSTAL, 3, -2.4F, new Item.Properties().stacksTo(1)));

    // 超结晶弓：耐久 640 的弓，伤害倍率 ×1.5；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> SUPER_CRYSTAL_BOW = registerItem("super_crystal_bow",
            () -> new MubangongBowItem(1.5D, "chaojiejinggong", new Item.Properties().durability(640)));

    // ---- 饭晶合金武器/防具（修复材料饭晶合金锭） ----
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_SWORD = registerItem("rice_crystal_alloy_sword",
            () -> new SwordItem(ModItemTiers.RICE_CRYSTAL_ALLOY, 3, -2.4F, new Item.Properties().stacksTo(1)));

    // 饭晶合金弓：耐久 896 的弓，伤害倍率 ×2；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_BOW = registerItem("rice_crystal_alloy_bow",
            () -> new MubangongBowItem(2.0D, "fanjinghejingong", new Item.Properties().durability(896)));

    // 饭晶合金四件：GeckoLib 3D 盔甲（RiceCrystalAlloyArmorItem，模型/贴图见 client/model 与 geo/textures 资源）
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_HELMET = registerItem("rice_crystal_alloy_helmet",
            () -> new RiceCrystalAlloyArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_CHESTPLATE = registerItem("rice_crystal_alloy_chestplate",
            () -> new RiceCrystalAlloyArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_LEGGINGS = registerItem("rice_crystal_alloy_leggings",
            () -> new RiceCrystalAlloyArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_BOOTS = registerItem("rice_crystal_alloy_boots",
            () -> new RiceCrystalAlloyArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 超重结晶武器（慢速重武器，修复材料超重结晶） ----
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_SWORD = registerItem("super_heavy_crystal_sword",
            () -> new SwordItem(ModItemTiers.SUPER_HEAVY_CRYSTAL, 5, -2.8F, new Item.Properties().stacksTo(1)));

    // 超重结晶弓：耐久 1024 的弓，伤害倍率 ×5；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_BOW = registerItem("super_heavy_crystal_bow",
            () -> new MubangongBowItem(5.0D, "chaozhongjiejinggong", new Item.Properties().durability(1024)));

    // ---- 装备锻打专用锭（普通材料，堆叠64） ----
    // 磨剑锭：锻打剑刃用
    public static final RegistryObject<Item> SWORD_TEMPER_INGOT = registerItem("sword_temper_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 弓弦锭：锻打弓弦用
    public static final RegistryObject<Item> BOWSTRING_INGOT = registerItem("bowstring_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 含甲锭：锻打甲片用
    public static final RegistryObject<Item> ARMOR_CORE_INGOT = registerItem("armor_core_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 高级磨剑锭
    public static final RegistryObject<Item> ADVANCED_SWORD_TEMPER_INGOT = registerItem("advanced_sword_temper_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 高级弓弦锭
    public static final RegistryObject<Item> ADVANCED_BOWSTRING_INGOT = registerItem("advanced_bowstring_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 高级含甲锭
    public static final RegistryObject<Item> ADVANCED_ARMOR_CORE_INGOT = registerItem("advanced_armor_core_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- 超重结晶防具（高防重甲，修复材料超重结晶）：GeckoLib 3D 重甲（SuperHeavyCrystalArmorItem） ----
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_HELMET = registerItem("super_heavy_crystal_helmet",
            () -> new SuperHeavyCrystalArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_CHESTPLATE = registerItem("super_heavy_crystal_chestplate",
            () -> new SuperHeavyCrystalArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_LEGGINGS = registerItem("super_heavy_crystal_leggings",
            () -> new SuperHeavyCrystalArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_BOOTS = registerItem("super_heavy_crystal_boots",
            () -> new SuperHeavyCrystalArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 复合米饭武器/防具（修复材料复合米饭锭） ----
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_SWORD = registerItem("compound_rice_meal_sword",
            () -> new SwordItem(ModItemTiers.COMPOUND_RICE_MEAL, 4, -2.4F, new Item.Properties().stacksTo(1)));

    // 复合米饭弓：耐久 1280 的弓，伤害倍率 ×10；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_BOW = registerItem("compound_rice_meal_bow",
            () -> new MubangongBowItem(10.0D, "fuhemifangong", new Item.Properties().durability(1280)));

    // ---- 复合米饭防具（钻石同级高附魔，修复材料复合米饭锭）：GeckoLib 3D 附魔甲（CompoundRiceMealArmorItem） ----
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_HELMET = registerItem("compound_rice_meal_helmet",
            () -> new CompoundRiceMealArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_CHESTPLATE = registerItem("compound_rice_meal_chestplate",
            () -> new CompoundRiceMealArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_LEGGINGS = registerItem("compound_rice_meal_leggings",
            () -> new CompoundRiceMealArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_BOOTS = registerItem("compound_rice_meal_boots",
            () -> new CompoundRiceMealArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 清洁复合胚件与成品（修复材料清洁复合锭） ----
    // 胚件是锻打中间产物（普通材料，堆叠64）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_SWORD_BLANK = registerItem("cleansing_compound_sword_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 弓胚显示与正式弓相同的 GeckoLib 3D 模型，但仅为合成材料（堆叠64、无使用效果）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOW_BLANK = registerItem("cleansing_compound_bow_blank",
            () -> new MubangongBlankItem("cleansing_compound_bow_blank", new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_SWORD = registerItem("cleansing_compound_sword",
            () -> new SwordItem(ModItemTiers.CLEANSING_COMPOUND, 4, -2.4F, new Item.Properties().stacksTo(1)));

    // 清洁复合弓：耐久 1536 的弓，伤害倍率 ×20；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOW = registerItem("cleansing_compound_bow",
            () -> new MubangongBowItem(20.0D, "qingjiefuhegong", new Item.Properties().durability(1536)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_ARMOR_BLANK = registerItem("cleansing_compound_armor_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- 清洁米饭防具（下界合金同级华贵重甲，修复材料清洁复合锭）：GeckoLib 3D 紫晶甲（CleansingCompoundArmorItem） ----
    public static final RegistryObject<Item> CLEANSING_COMPOUND_HELMET = registerItem("cleansing_compound_helmet",
            () -> new CleansingCompoundArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_CHESTPLATE = registerItem("cleansing_compound_chestplate",
            () -> new CleansingCompoundArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_LEGGINGS = registerItem("cleansing_compound_leggings",
            () -> new CleansingCompoundArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOOTS = registerItem("cleansing_compound_boots",
            () -> new CleansingCompoundArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 超懒合金胚件与终极成品（修复材料超懒合金锭） ----
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_SWORD_BLANK = registerItem("super_lazy_alloy_sword_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 弓胚显示与正式弓相同的 GeckoLib 3D 模型，但仅为合成材料（堆叠64、无使用效果）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOW_BLANK = registerItem("super_lazy_alloy_bow_blank",
            () -> new MubangongBlankItem("super_lazy_alloy_bow_blank", new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_ARMOR_BLANK = registerItem("super_lazy_alloy_armor_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_SWORD = registerItem("super_lazy_alloy_sword",
            () -> new SwordItem(ModItemTiers.SUPER_LAZY_ALLOY, 5, -2.4F, new Item.Properties().stacksTo(1)));

    // 超懒合金弓：耐久 2048 的弓，伤害倍率 ×50；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOW = registerItem("super_lazy_alloy_bow",
            () -> new MubangongBowItem(50.0D, "chaolanhejingong", new Item.Properties().durability(2048)));

    // ---- 超懒合金防具（终极套总防 26/韧性 5/击退抗 0.15，修复材料超懒合金锭）：GeckoLib 3D 金蟒甲（SuperLazyAlloyArmorItem） ----
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_HELMET = registerItem("super_lazy_alloy_helmet",
            () -> new SuperLazyAlloyArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_CHESTPLATE = registerItem("super_lazy_alloy_chestplate",
            () -> new SuperLazyAlloyArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_LEGGINGS = registerItem("super_lazy_alloy_leggings",
            () -> new SuperLazyAlloyArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOOTS = registerItem("super_lazy_alloy_boots",
            () -> new SuperLazyAlloyArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ===== 装备锻造台系列方块（金属机器方块，钻石镐采集，破坏掉落本身） =====

    // 铸剑台，Sword Forging Table：放置后对应 ModBlocks.SWORD_FORGING_TABLE
    public static final RegistryObject<Item> SWORD_FORGING_TABLE = registerItem("sword_forging_table",
            () -> new BlockItem(ModBlocks.SWORD_FORGING_TABLE.get(), new Item.Properties()));

    // 铸甲台，Armor Forging Table：放置后对应 ModBlocks.ARMOR_FORGING_TABLE
    public static final RegistryObject<Item> ARMOR_FORGING_TABLE = registerItem("armor_forging_table",
            () -> new BlockItem(ModBlocks.ARMOR_FORGING_TABLE.get(), new Item.Properties()));

    // 铸弓台，Bow Forging Table：放置后对应 ModBlocks.BOW_FORGING_TABLE
    public static final RegistryObject<Item> BOW_FORGING_TABLE = registerItem("bow_forging_table",
            () -> new BlockItem(ModBlocks.BOW_FORGING_TABLE.get(), new Item.Properties()));

    // ===== 米米之眼/传送器/召唤器系列（普通物品，最大堆叠 64） =====

    // 米米之眼（Eye of Mimi）
    public static final RegistryObject<Item> MIMI_EYE = registerItem("mimi_eye",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米米图腾（Mimi Totem）：不死图腾，主手/副手持有受致命伤时免死一次；
    // 效果与原版一致（回 1 血+再生 II 45s+伤害吸收 II 5s+抗火 I 40s），
    // 触发逻辑见 mixin.LivingEntityMixin；默认堆叠 64 同原版图腾；暂无配方
    public static final RegistryObject<Item> MIMI_TOTEM = registerItem("mimi_totem",
            () -> new MimiTotemItem(new Item.Properties()));

    // 米米传送器（Mimi Teleporter）
    public static final RegistryObject<Item> MIMI_TELEPORTER = registerItem("mimi_teleporter",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 小型召唤器（Small Summoner）
    public static final RegistryObject<Item> SMALL_SUMMONER = registerItem("small_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 中型召唤器（Medium Summoner）
    public static final RegistryObject<Item> MEDIUM_SUMMONER = registerItem("medium_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 大型召唤器（Large Summoner）
    public static final RegistryObject<Item> LARGE_SUMMONER = registerItem("large_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 最懒召唤器（Laziest Summoner）
    public static final RegistryObject<Item> LAZIEST_SUMMONER = registerItem("laziest_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 召唤核心三级链（胚 → 粗制胚 → 核心），普通物品堆叠 64 =====

    // 召唤核心胚
    public static final RegistryObject<Item> SUMMON_CORE_BLANK = registerItem("summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制召唤核心胚
    public static final RegistryObject<Item> COARSE_SUMMON_CORE_BLANK = registerItem("coarse_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 召唤核心
    public static final RegistryObject<Item> SUMMON_CORE = registerItem("summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 高级召唤核心胚
    public static final RegistryObject<Item> ADVANCED_SUMMON_CORE_BLANK = registerItem("advanced_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制高级召唤核心胚
    public static final RegistryObject<Item> COARSE_ADVANCED_SUMMON_CORE_BLANK =
            registerItem("coarse_advanced_summon_core_blank",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 高级召唤核心
    public static final RegistryObject<Item> ADVANCED_SUMMON_CORE = registerItem("advanced_summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 混合结晶加工中间体 =====

    // 混合结晶（Mixed Crystal）
    public static final RegistryObject<Item> MIXED_CRYSTAL = registerItem("mixed_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 熔融混合结晶（Molten Mixed Crystal）
    public static final RegistryObject<Item> MOLTEN_MIXED_CRYSTAL = registerItem("molten_mixed_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 最懒召唤核心三级链 =====

    // 最懒召唤核心胚
    public static final RegistryObject<Item> LAZIEST_SUMMON_CORE_BLANK = registerItem("laziest_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制最懒召唤核心胚
    public static final RegistryObject<Item> COARSE_LAZIEST_SUMMON_CORE_BLANK =
            registerItem("coarse_laziest_summon_core_blank",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 最懒召唤核心（三级链的最终核心）
    public static final RegistryObject<Item> LAZIEST_SUMMON_CORE = registerItem("laziest_summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 七大 Boss 生物蛋（ForgeSpawnEggItem；实体当前为 PendingBossEntity 占位，后续替换真实 Boss） =====

    // 米米猴王生物蛋：棕底/米黄斑点
    public static final RegistryObject<Item> MIMI_MONKEY_KING_SPAWN_EGG =
            registerItem("mimi_monkey_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIMI_MONKEY_KING, 0x8C5A2B, 0xE8C26A, new Item.Properties()));

    // 饭晶虎王生物蛋：米白底/金斑
    public static final RegistryObject<Item> RICE_CRYSTAL_TIGER_KING_SPAWN_EGG =
            registerItem("rice_crystal_tiger_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_CRYSTAL_TIGER_KING, 0xEDE2C8, 0xC98A2E, new Item.Properties()));

    // 超晶牛王生物蛋：深蓝底/亮蓝斑
    public static final RegistryObject<Item> SUPER_CRYSTAL_COW_KING_SPAWN_EGG =
            registerItem("super_crystal_cow_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.SUPER_CRYSTAL_COW_KING, 0x2E4A80, 0x7FB6E8, new Item.Properties()));

    // 重晶兔王生物蛋：紫底/浅紫斑
    public static final RegistryObject<Item> HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG =
            registerItem("heavy_crystal_rabbit_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.HEAVY_CRYSTAL_RABBIT_KING, 0x6E4E96, 0xD3B8F0, new Item.Properties()));

    // 复晶天青蟒生物蛋：青底/薄荷斑
    public static final RegistryObject<Item> COMPOUND_AZURE_PYTHON_SPAWN_EGG =
            registerItem("compound_azure_python_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.COMPOUND_AZURE_PYTHON, 0x1F7A72, 0x8FE0D2, new Item.Properties()));

    // 清洁长命夔生物蛋：青白底/天青斑
    public static final RegistryObject<Item> CLEANSING_LONGEVITY_KUI_SPAWN_EGG =
            registerItem("cleansing_longevity_kui_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CLEANSING_LONGEVITY_KUI, 0xCFEAF2, 0x3FA0BC, new Item.Properties()));

    // 最懒终末龙生物蛋：暗紫底/幻紫斑
    public static final RegistryObject<Item> LAZIEST_FINAL_DRAGON_SPAWN_EGG =
            registerItem("laziest_final_dragon_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.LAZIEST_FINAL_DRAGON, 0x2E1E4A, 0xA974E0, new Item.Properties()));

    // ===== 米米系列生物蛋（ForgeSpawnEggItem；模型统一 parent 原版 item/template_spawn_egg，
    //        底色=蛋身、斑色=斑点，按各生物主题配色；占位实体转正后蛋无需改动） =====

    // —— 已转正 GeckoLib 友好/特殊生物 ——
    // 黏米之蛛生物蛋：糯米白底/蛛腿墨黑
    public static final RegistryObject<Item> STICKY_RICE_SPIDER_SPAWN_EGG =
            registerItem("sticky_rice_spider_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.STICKY_RICE_SPIDER, 0xEDE6D2, 0x3A332E, new Item.Properties()));
    // 米羊羊生物蛋：羊毛米白/稻金耳蹄
    public static final RegistryObject<Item> MIMI_SHEEP_SPAWN_EGG =
            registerItem("mimi_sheep_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIMI_SHEEP, 0xEDEDE6, 0xCBB892, new Item.Properties()));
    // 啄米鸡生物蛋：白羽米白/鸡冠红
    public static final RegistryObject<Item> ZHUOMI_CHICKEN_SPAWN_EGG =
            registerItem("zhuomi_chicken_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.ZHUOMI_CHICKEN, 0xF4F1E6, 0xC2472E, new Item.Properties()));
    // 米哞牛生物蛋：米白毛/稻金角
    public static final RegistryObject<Item> MIMOO_COW_SPAWN_EGG =
            registerItem("mimoo_cow_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIMOO_COW, 0xEDE2C8, 0xD9A84E, new Item.Properties()));
    // 米哼猪生物蛋：米白毛/米粉腮红
    public static final RegistryObject<Item> MIHENG_PIG_SPAWN_EGG =
            registerItem("miheng_pig_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIHENG_PIG, 0xF3E9DC, 0xF0A6B2, new Item.Properties()));

    // —— 友好占位生物 ——
    // 贪米鱼生物蛋：银蓝鱼鳞/贪金
    public static final RegistryObject<Item> TANMI_FISH_SPAWN_EGG =
            registerItem("tanmi_fish_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.TANMI_FISH, 0xA9C4DC, 0xE3C15A, new Item.Properties()));
    // 喷米鱼生物蛋：水蓝鱼身/喷米白
    public static final RegistryObject<Item> PENMI_FISH_SPAWN_EGG =
            registerItem("penmi_fish_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.PENMI_FISH, 0x7FB3D5, 0xF7F3E8, new Item.Properties()));
    // 米傀儡生物蛋：草垛黄/麻绳褐
    public static final RegistryObject<Item> RICE_GOLEM_SPAWN_EGG =
            registerItem("rice_golem_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_GOLEM, 0xCBB889, 0x8A764A, new Item.Properties()));
    // 嗥米狼生物蛋：狼灰/米白
    public static final RegistryObject<Item> HAOMI_WOLF_SPAWN_EGG =
            registerItem("haomi_wolf_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.HAOMI_WOLF, 0x8A8276, 0xEDE2C8, new Item.Properties()));
    // 米米骷髅生物蛋：骨白/旧骨灰
    public static final RegistryObject<Item> MIMI_SKELETON_SPAWN_EGG =
            registerItem("mimi_skeleton_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIMI_SKELETON, 0xE8E4D6, 0xB0A98F, new Item.Properties()));
    // 糊米古尸生物蛋：糊米褐/焦壳深褐
    public static final RegistryObject<Item> HUMI_CORPSE_SPAWN_EGG =
            registerItem("humi_corpse_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.HUMI_CORPSE, 0x7A6B52, 0x473C2B, new Item.Properties()));
    // 爪巴米者生物蛋：霉米黄褐/暗斑
    public static final RegistryObject<Item> PAMI_CRAWLER_SPAWN_EGG =
            registerItem("pami_crawler_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.PAMI_CRAWLER, 0x9A8E70, 0x5C5338, new Item.Properties()));
    // 米巫生物蛋：巫袍紫/魔雾浅紫
    public static final RegistryObject<Item> RICE_WITCH_SPAWN_EGG =
            registerItem("rice_witch_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_WITCH, 0x4A3A66, 0x9A86BC, new Item.Properties()));
    // 没米入生物蛋：暗灰/幽灰
    public static final RegistryObject<Item> MOMI_ENTRANT_SPAWN_EGG =
            registerItem("momi_entrant_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MOMI_ENTRANT, 0x38323E, 0x857B92, new Item.Properties()));
    // 爆烈米灵生物蛋：爆裂红/火星金
    public static final RegistryObject<Item> EXPLOSIVE_RICE_SPIRIT_SPAWN_EGG =
            registerItem("explosive_rice_spirit_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.EXPLOSIVE_RICE_SPIRIT, 0xB0472C, 0xEBC24A, new Item.Properties()));
    // 雕米骷髅生物蛋：雕骨米黄/刻纹褐
    public static final RegistryObject<Item> CARVED_RICE_SKELETON_SPAWN_EGG =
            registerItem("carved_rice_skeleton_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CARVED_RICE_SKELETON, 0xD8CFB6, 0x7A6E4F, new Item.Properties()));
    // 米莱马生物蛋：栗马褐/鬃毛深褐
    public static final RegistryObject<Item> MILAI_HORSE_SPAWN_EGG =
            registerItem("milai_horse_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MILAI_HORSE, 0x8A6B4A, 0x3A2C21, new Item.Properties()));
    // 米哼猪尸生物蛋：尸绿/腐斑暗绿
    public static final RegistryObject<Item> MIHENG_PIG_CORPSE_SPAWN_EGG =
            registerItem("miheng_pig_corpse_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIHENG_PIG_CORPSE, 0x7E8C66, 0x56603F, new Item.Properties()));
    // 米魂生物蛋：魂青白/幽青
    public static final RegistryObject<Item> RICE_SOUL_SPAWN_EGG =
            registerItem("rice_soul_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_SOUL, 0xCDEFF0, 0x74C6CE, new Item.Properties()));

    // —— 敌对占位生物 ——
    // 界天米生物蛋：帝王金/天权暗金
    public static final RegistryObject<Item> JIETIAN_RICE_SPAWN_EGG =
            registerItem("jietian_rice_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.JIETIAN_RICE, 0xC8B04A, 0x86651B, new Item.Properties()));
    // 混沌不可名状之米界虚无触生物蛋：虚无墨紫/混沌紫
    public static final RegistryObject<Item> CHAOTIC_VOID_TOUCHER_SPAWN_EGG =
            registerItem("chaotic_void_toucher_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CHAOTIC_VOID_TOUCHER, 0x181224, 0x8A3AB0, new Item.Properties()));
    // 污棘花生物蛋：枯萎绿/污染紫
    public static final RegistryObject<Item> BLIGHTED_THORN_FLOWER_SPAWN_EGG =
            registerItem("blighted_thorn_flower_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.BLIGHTED_THORN_FLOWER, 0x495A2E, 0x77478A, new Item.Properties()));
    // 堕落米凤生物蛋：堕落暗红/余烬橙
    public static final RegistryObject<Item> FALLEN_RICE_PHOENIX_SPAWN_EGG =
            registerItem("fallen_rice_phoenix_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.FALLEN_RICE_PHOENIX, 0x7A2A38, 0xE0873A, new Item.Properties()));
    // 化灵米生物蛋：化灵蓝/灵光白
    public static final RegistryObject<Item> HUALING_RICE_SPAWN_EGG =
            registerItem("hualing_rice_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.HUALING_RICE, 0x6A8AC8, 0xE6EEF8, new Item.Properties()));
    // 浅染鲛生物蛋：浅染苍/深海青
    public static final RegistryObject<Item> PALE_MERMAID_SPAWN_EGG =
            registerItem("pale_mermaid_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.PALE_MERMAID, 0xB9CAD9, 0x6689A6, new Item.Properties()));
    // 望米伤鲛生物蛋：伤灰紫/血痕红
    public static final RegistryObject<Item> WANGMI_WOUNDED_MERMAID_SPAWN_EGG =
            registerItem("wangmi_wounded_mermaid_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.WANGMI_WOUNDED_MERMAID, 0x886674, 0xB0485A, new Item.Properties()));
    // 米蜃生物蛋：蜃气青灰/幻光浅青
    public static final RegistryObject<Item> RICE_MIRAGE_SPAWN_EGG =
            registerItem("rice_mirage_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_MIRAGE, 0x88A89F, 0xC7E0D7, new Item.Properties()));
    // 懒精灵生物蛋：怠惰灰蓝/慵睡淡紫
    public static final RegistryObject<Item> LAZY_ELF_SPAWN_EGG =
            registerItem("lazy_elf_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.LAZY_ELF, 0x989EB6, 0xD6CFE6, new Item.Properties()));
    // 哮噬米狼鹫生物蛋：猛禽深褐/狼鹫金褐
    public static final RegistryObject<Item> RICE_WOLF_VULTURE_SPAWN_EGG =
            registerItem("rice_wolf_vulture_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_WOLF_VULTURE, 0x493B30, 0x9A7A4A, new Item.Properties()));

    // ===== 米米神翼系列 + 上古剑意 + 超懒神剑残片 =====

    // 米米神翼（Mimi Divine Wings）：胸甲装备，MIMI_WINGS 材质
    public static final RegistryObject<Item> MIMI_DIVINE_WINGS = registerItem("mimi_divine_wings",
            () -> new ArmorItem(ModArmorMaterials.MIMI_WINGS, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1)));

    // 米米幻翼膜（Mimi Phantom Membrane）：神翼修复材料，普通物品
    public static final RegistryObject<Item> MIMI_PHANTOM_MEMBRANE = registerItem("mimi_phantom_membrane",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒神翼胸甲（Super Lazy Divine Wings Chestplate）：终极翼甲，GeckoLib 3D 双翼循环扇动
    public static final RegistryObject<Item> SUPER_LAZY_WINGS_CHESTPLATE =
            registerItem("super_lazy_wings_chestplate",
                    () -> new SuperLazyWingsChestplateItem(ModArmorMaterials.SUPER_LAZY_WINGS, ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().stacksTo(1)));

    // 上古剑意（Ancient Sword Intent）：普通物品
    public static final RegistryObject<Item> ANCIENT_SWORD_INTENT = registerItem("ancient_sword_intent",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒神剑·残缺·壹（Super Lazy Divine Sword - Broken I）：普通物品【非装备】，
    // 最大堆叠 1；RainbowNameItem 覆盖显示名，逐字动态彩虹渐变
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_BROKEN_1 =
            registerItem("super_lazy_divine_sword_broken_1",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 淬剑台（Sword Quenching Table）：放置后对应 ModBlocks.SWORD_QUENCHING_TABLE
    public static final RegistryObject<Item> SWORD_QUENCHING_TABLE = registerItem("sword_quenching_table",
            () -> new BlockItem(ModBlocks.SWORD_QUENCHING_TABLE.get(), new Item.Properties()));

    // 超懒神剑·残缺·贰（Super Lazy Divine Sword - Broken II）：普通物品【非装备】，
    // 最大堆叠 1；RainbowNameItem 覆盖显示名，逐字动态彩虹渐变（与超懒神剑·残缺·壹同款）
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_BROKEN_2 =
            registerItem("super_lazy_divine_sword_broken_2",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 汲血台（Bloodletting Table）：放置后对应 ModBlocks.BLOODLETTING_TABLE
    public static final RegistryObject<Item> BLOODLETTING_TABLE = registerItem("bloodletting_table",
            () -> new BlockItem(ModBlocks.BLOODLETTING_TABLE.get(), new Item.Properties()));

    // ===== 血瓶 / 淬剑液（普通物品，最大堆叠 64） =====

    // 血瓶（Blood Vial）
    public static final RegistryObject<Item> BLOOD_VIAL = registerItem("blood_vial",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 淬剑液（Sword Quenching Liquid）
    public static final RegistryObject<Item> SWORD_QUENCHING_LIQUID = registerItem("sword_quenching_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 剑魄与超懒神剑·残次/赝品 =====

    // 远古剑魄（Ancient Sword Soul）
    public static final RegistryObject<Item> ANCIENT_SWORD_SOUL = registerItem("ancient_sword_soul",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒神剑·残次：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_DEFECTIVE =
            registerItem("super_lazy_divine_sword_defective",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 太古剑魄（Primordial Sword Soul）
    public static final RegistryObject<Item> PRIMORDIAL_SWORD_SOUL = registerItem("primordial_sword_soul",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒神剑·赝品：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT =
            registerItem("super_lazy_divine_sword_counterfeit",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 神玉系列与超懒神剑·瑕疵 =====

    // 神玉（Divine Jade）
    public static final RegistryObject<Item> DIVINE_JADE = registerItem("divine_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 金米玉（Golden Rice Jade）
    public static final RegistryObject<Item> GOLDEN_RICE_JADE = registerItem("golden_rice_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 神剑鉴玉（Divine Sword Appraisal Jade）
    public static final RegistryObject<Item> DIVINE_SWORD_APPRAISAL_JADE =
            registerItem("divine_sword_appraisal_jade",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒神剑·瑕疵：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_FLAWED =
            registerItem("super_lazy_divine_sword_flawed",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 剑谱系列（彩虹名物品堆叠 1） =====

    // 剑法禁卷（Forbidden Sword Manual）
    public static final RegistryObject<Item> FORBIDDEN_SWORD_MANUAL = registerItem("forbidden_sword_manual",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒剑法：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_SWORD_ART = registerItem("super_lazy_sword_art",
            () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 超懒神剑·完整：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_COMPLETE =
            registerItem("super_lazy_divine_sword_complete",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 结晶米玉（Crystallized Rice Jade）
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_JADE = registerItem("crystallized_rice_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 终末剑诀：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> FINAL_SWORD_CODEX = registerItem("final_sword_codex",
            () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 天堕/堕落系列 =====

    // 最懒天堕剑·赝品：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT =
            registerItem("laziest_heavenfall_sword_counterfeit",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 破败之心（Ruined Heart）
    public static final RegistryObject<Item> RUINED_HEART = registerItem("ruined_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 堕落之心（Fallen Heart）：HEAVENFALL 盔甲的修复材料
    public static final RegistryObject<Item> FALLEN_HEART = registerItem("fallen_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 堕落王冠（Fallen Crown）
    public static final RegistryObject<Item> FALLEN_CROWN = registerItem("fallen_crown",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 七宗堕落（Seven Deadly Falls）
    public static final RegistryObject<Item> SEVEN_DEADLY_FALLS = registerItem("seven_deadly_falls",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 最懒天堕剑·原罪：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN =
            registerItem("laziest_heavenfall_sword_original_sin",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 最懒天堕盔甲四件套（HEAVENFALL 材质，全部 stacksTo 1） =====

    // 天堕翼胸甲：CHESTPLATE
    public static final RegistryObject<Item> HEAVENFALL_WINGS_CHESTPLATE =
            registerItem("heavenfall_wings_chestplate",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().stacksTo(1)));

    // 最懒天堕头盔：HELMET
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_HELMET =
            registerItem("laziest_heavenfall_helmet",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.HELMET,
                            new Item.Properties().stacksTo(1)));

    // 最懒天堕护腿：LEGGINGS
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_LEGGINGS =
            registerItem("laziest_heavenfall_leggings",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.LEGGINGS,
                            new Item.Properties().stacksTo(1)));

    // 最懒天堕靴子：BOOTS
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_BOOTS =
            registerItem("laziest_heavenfall_boots",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.BOOTS,
                            new Item.Properties().stacksTo(1)));

    // ===== 米米系列肉鱼食物（全部食用时长 1.5s = 30t，nutrition 单位=半个鸡腿） =====

    // 熟啄米鸡肉（Cooked Zhuomi Chicken）：2.5 饱食度(nutrition=5)；1% 中毒 10s + 50% 反胃 5s
    public static final FoodProperties COOKED_ZHUOMI_CHICKEN_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 0.01F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 0.5F)
            .build();

    public static final RegistryObject<Item> COOKED_ZHUOMI_CHICKEN = registerItem("cooked_zhuomi_chicken",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_ZHUOMI_CHICKEN_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // 米吽牛肉（Raw Mimoo Beef）：1 饱食度(nutrition=2)；100% 反胃 20s + 100% 饥饿 30s + 5% 中毒 25s
    public static final FoodProperties RAW_MIMOO_BEEF_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 500, 0), 0.05F)
            .build();

    public static final RegistryObject<Item> RAW_MIMOO_BEEF = registerItem("raw_mimoo_beef",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIMOO_BEEF_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 熟米吽牛肉（Cooked Mimoo Beef）：4 饱食度(nutrition=8)；100% 缓慢 2s + 25% 反胃 10s
    public static final FoodProperties COOKED_MIMOO_BEEF_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.25F)
            .build();

    public static final RegistryObject<Item> COOKED_MIMOO_BEEF = registerItem("cooked_mimoo_beef",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIMOO_BEEF_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 米羊羊肉（Raw Mimi Mutton）：1 饱食度(nutrition=2)；50% 虚弱 10s + 70% 饥饿 20s + 100% 反胃 10s
    public static final FoodProperties RAW_MIMI_MUTTON_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400, 0), 0.7F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_MIMI_MUTTON = registerItem("raw_mimi_mutton",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIMI_MUTTON_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 熟米羊羊肉（Cooked Mimi Mutton）：3 饱食度(nutrition=6)；10% 反胃 5s + 1% 虚弱 2s
    public static final FoodProperties COOKED_MIMI_MUTTON_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 40, 0), 0.01F)
            .build();

    public static final RegistryObject<Item> COOKED_MIMI_MUTTON = registerItem("cooked_mimi_mutton",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIMI_MUTTON_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 米哼猪肉（Raw Miheng Pork）：2 饱食度(nutrition=4)；
    // 100% 失明 10s + 100% 虚弱 10s + 100% 缓慢 10s + 100% 中毒 10s + 100% 反胃 10s
    public static final FoodProperties RAW_MIHENG_PORK_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_MIHENG_PORK = registerItem("raw_miheng_pork",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIHENG_PORK_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 熟米哼猪肉（Cooked Miheng Pork）：6 饱食度(nutrition=12)；
    // 100% 失明 10s + 100% 虚弱 10s + 50% 缓慢 10s + 50% 反胃 10s
    public static final FoodProperties COOKED_MIHENG_PORK_FOOD = new FoodProperties.Builder()
            .nutrition(12)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
            .build();

    public static final RegistryObject<Item> COOKED_MIHENG_PORK = registerItem("cooked_miheng_pork",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIHENG_PORK_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 贪米鱼肉（Raw Tanmi Fish）：0.5 饱食度(nutrition=1)；100% 缓慢 10s
    public static final FoodProperties RAW_TANMI_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_TANMI_FISH = registerItem("raw_tanmi_fish",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_TANMI_FISH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 熟贪米鱼肉（Cooked Tanmi Fish）：3 饱食度(nutrition=6)；100% 缓慢 5s + 100% 虚弱 10s
    public static final FoodProperties COOKED_TANMI_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> COOKED_TANMI_FISH = registerItem("cooked_tanmi_fish",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_TANMI_FISH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 贪米鱼桶（Bucket of Tanmi Fish）：对水中贪米鱼使用水桶装起，倒出可还原活鱼（AbstractFish 原版交互）
    public static final RegistryObject<Item> TANMI_FISH_BUCKET = registerItem("tanmi_fish_bucket",
            () -> new MobBucketItem(ModEntities.TANMI_FISH,
                    () -> Fluids.WATER,
                    () -> SoundEvents.BUCKET_EMPTY_FISH,
                    new Item.Properties().stacksTo(1)));

    // 喷米鱼桶（Bucket of Penmi Fish）：对水中喷米鱼使用水桶装起，倒出可还原活鱼（AbstractFish 原版交互）
    public static final RegistryObject<Item> PENMI_FISH_BUCKET = registerItem("penmi_fish_bucket",
            () -> new MobBucketItem(ModEntities.PENMI_FISH,
                    () -> Fluids.WATER,
                    () -> SoundEvents.BUCKET_EMPTY_FISH,
                    new Item.Properties().stacksTo(1)));

    // ===== 米米生物掉落材料（均为普通物品，最大堆叠 64，无食用属性） =====

    // 啄米羽毛（Zhuomi Feather）
    public static final RegistryObject<Item> ZHUOMI_FEATHER = registerItem("zhuomi_feather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米哞皮革（Mimoo Leather）
    public static final RegistryObject<Item> MIMOO_LEATHER = registerItem("mimoo_leather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米羊羊毛（Mimi Wool）
    public static final RegistryObject<Item> MIMI_WOOL = registerItem("mimi_wool",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米哼猪皮（Miheng Hide）
    public static final RegistryObject<Item> MIHENG_HIDE = registerItem("miheng_hide",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 喷米鱼囊（Penmi Fish Sac）
    public static final RegistryObject<Item> PENMI_FISH_SAC = registerItem("penmi_fish_sac",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 嗥米狼心（Haomi Wolf Heart）
    public static final RegistryObject<Item> HAOMI_WOLF_HEART = registerItem("haomi_wolf_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米骨（Rice Bone）
    public static final RegistryObject<Item> RICE_BONE = registerItem("rice_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米箭（Rice Arrow）：可发射箭矢，命中固定 1 伤害、无附加效果
    public static final RegistryObject<Item> RICE_ARROW = registerItem("rice_arrow",
            () -> new MimiArrowItem(1.0D, 0, new Item.Properties().stacksTo(64)));

    // 火米药（Huomi Powder）
    public static final RegistryObject<Item> HUOMI_POWDER = registerItem("huomi_powder",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 眼米珍珠（Yanmi Pearl）
    public static final RegistryObject<Item> YANMI_PEARL = registerItem("yanmi_pearl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 爆烈米棒（Baoliemi Rod）
    public static final RegistryObject<Item> BAOLIEMI_ROD = registerItem("baoliemi_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 雕米骨（Carved Rice Bone）
    public static final RegistryObject<Item> CARVED_RICE_BONE = registerItem("carved_rice_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米莱马球（Milaima Ball）
    public static final RegistryObject<Item> MILAIMA_BALL = registerItem("milaima_ball",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 黏米丝线（Nianmi Silk Thread）
    public static final RegistryObject<Item> NIANMI_SILK_THREAD = registerItem("nianmi_silk_thread",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 腐米肉（Rotten Rice Flesh）：1 饱食度(nutrition=2)，食用时长 1.5s(30t)；
    // 100% 中毒 5s + 100% 饥饿 30s + 100% 缓慢 10s + 1% 中毒 60s（与 5s 中毒同等级，触发时按原版规则取较长 60s）
    public static final FoodProperties ROTTEN_RICE_FLESH_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 1200, 0), 0.01F)
            .build();

    public static final RegistryObject<Item> ROTTEN_RICE_FLESH = registerItem("rotten_rice_flesh",
            () -> new Item(new Item.Properties().stacksTo(64).food(ROTTEN_RICE_FLESH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // ===== 米米加工材料/液体（均为普通物品，最大堆叠 64；液体为药水瓶外形的非流体物品） =====

    // 米米大杂烩（Mimi Medley）
    public static final RegistryObject<Item> MIMI_MEDLEY = registerItem("mimi_medley",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米骨粉（Rice Bone Meal）
    public static final RegistryObject<Item> RICE_BONE_MEAL = registerItem("rice_bone_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米精（Rice Essence）
    public static final RegistryObject<Item> RICE_ESSENCE = registerItem("rice_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗米（Coarse Rice）
    public static final RegistryObject<Item> COARSE_RICE = registerItem("coarse_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米笔（Rice Brush Pen）
    public static final RegistryObject<Item> RICE_BRUSH_PEN = registerItem("rice_brush_pen",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 书米笔（Book Rice Pen）
    public static final RegistryObject<Item> BOOK_RICE_PEN = registerItem("book_rice_pen",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果黑混液（Rice Fruit Black Mixture）
    public static final RegistryObject<Item> RICE_FRUIT_BLACK_MIXTURE = registerItem("rice_fruit_black_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米果混黏液（Rice Fruit Viscous Mixture）
    public static final RegistryObject<Item> RICE_FRUIT_VISCOUS_MIXTURE = registerItem("rice_fruit_viscous_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 爆米绽放液（Poprice Bloom Liquid）
    public static final RegistryObject<Item> POPRICE_BLOOM_LIQUID = registerItem("poprice_bloom_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 眼米嘬心液（Yanmi Heart-Sip Liquid）
    public static final RegistryObject<Item> YANMI_HEART_SIP_LIQUID = registerItem("yanmi_heart_sip_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米米杂烩液（Mimi Medley Liquid）
    public static final RegistryObject<Item> MIMI_MEDLEY_LIQUID = registerItem("mimi_medley_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== BOSS/神兽掉落（均为普通物品，最大堆叠 64；"头/棍/爪/角/牙/鳞"仅为同名掉落物，无装备/武器功能） =====

    // 猴王如意棍（Monkey King Ruyi Staff）：近战武器，16 伤 / 攻速 0.8 / 触及 6 格，命中附加虚弱 I 2s
    public static final RegistryObject<Item> MONKEY_KING_RUYI_STAFF = registerItem("monkey_king_ruyi_staff",
            () -> new MonkeyKingRuyiStaffItem(new Item.Properties().stacksTo(1)));

    // 米米猴头（Mimi Monkey Head）：方块物品，放置后对应 ModBlocks.MIMI_MONKEY_HEAD
    // （GeckoLib 头颅摆件方块，猴头+金冠+双雉鸡翎）；仍可用于如意棍修复等既有引用
    public static final RegistryObject<Item> MIMI_MONKEY_HEAD = registerItem("mimi_monkey_head",
            () -> new BlockItem(ModBlocks.MIMI_MONKEY_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 虎王利爪（Tiger King Razor Claw）
    public static final RegistryObject<Item> TIGER_KING_RAZOR_CLAW = registerItem("tiger_king_razor_claw",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 饭晶虎王头（Rice Crystal Tiger King Head）：方块物品，放置后对应 ModBlocks.RICE_CRYSTAL_TIGER_KING_HEAD
    // （GeckoLib 头颅摆件方块，虎头+双耳+冠+下颌）
    public static final RegistryObject<Item> RICE_CRYSTAL_TIGER_KING_HEAD = registerItem("rice_crystal_tiger_king_head",
            () -> new BlockItem(ModBlocks.RICE_CRYSTAL_TIGER_KING_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 牛王晶角（Ox King Crystal Horn）
    public static final RegistryObject<Item> OX_KING_CRYSTAL_HORN = registerItem("ox_king_crystal_horn",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超晶牛头（Super Crystal Ox Head）：方块物品，放置后对应 ModBlocks.SUPER_CRYSTAL_OX_HEAD
    public static final RegistryObject<Item> SUPER_CRYSTAL_OX_HEAD = registerItem("super_crystal_ox_head",
            () -> new BlockItem(ModBlocks.SUPER_CRYSTAL_OX_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 兔王腥红眼（Rabbit King Bloody Red Eye）
    public static final RegistryObject<Item> RABBIT_KING_BLOODY_RED_EYE = registerItem("rabbit_king_bloody_red_eye",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 重晶兔王头（Heavy Crystal Rabbit King Head）：方块物品，放置后对应 ModBlocks.HEAVY_CRYSTAL_RABBIT_KING_HEAD
    public static final RegistryObject<Item> HEAVY_CRYSTAL_RABBIT_KING_HEAD = registerItem("heavy_crystal_rabbit_king_head",
            () -> new BlockItem(ModBlocks.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 极毒青蟒牙（Venom Azure Python Fang）
    public static final RegistryObject<Item> VENOM_AZURE_PYTHON_FANG = registerItem("venom_azure_python_fang",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 复晶天青蟒头（Compound Crystal Azure Python Head）：方块物品，放置后对应 ModBlocks.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD
    public static final RegistryObject<Item> COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD = registerItem("compound_crystal_azure_python_head",
            () -> new BlockItem(ModBlocks.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 恶夔长命角（Evil Kui Longevity Horn）
    public static final RegistryObject<Item> EVIL_KUI_LONGEVITY_HORN = registerItem("evil_kui_longevity_horn",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁长命夔头（Purified Longevity Kui Head）：方块物品，放置后对应 ModBlocks.PURIFIED_LONGEVITY_KUI_HEAD
    public static final RegistryObject<Item> PURIFIED_LONGEVITY_KUI_HEAD = registerItem("purified_longevity_kui_head",
            () -> new BlockItem(ModBlocks.PURIFIED_LONGEVITY_KUI_HEAD.get(), new Item.Properties().stacksTo(64)));

    // 终末龙逆鳞（End Dragon Reverse Scale）
    public static final RegistryObject<Item> END_DRAGON_REVERSE_SCALE = registerItem("end_dragon_reverse_scale",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 最懒终末龙头（Laziest End Dragon Head）：方块物品，放置后对应 ModBlocks.LAZIEST_END_DRAGON_HEAD
    public static final RegistryObject<Item> LAZIEST_END_DRAGON_HEAD = registerItem("laziest_end_dragon_head",
            () -> new BlockItem(ModBlocks.LAZIEST_END_DRAGON_HEAD.get(), new Item.Properties().stacksTo(64)));

    // ===== 爆米嘬心液 + 四档镐头部件（均为普通物品，最大堆叠 64） =====

    // 爆米嘬心液（Poprice Heart-Sip Liquid）：药水瓶外形的非流体物品
    public static final RegistryObject<Item> POPRICE_HEART_SIP_LIQUID = registerItem("poprice_heart_sip_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超重结晶镐头（Super Heavy Crystal Pickaxe Head）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD =
            registerItem("super_heavy_crystal_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 复合米饭镐头（Compound Rice Meal Pickaxe Head）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PICKAXE_HEAD =
            registerItem("compound_rice_meal_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁复合镐头（Cleansing Compound Pickaxe Head）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PICKAXE_HEAD =
            registerItem("cleansing_compound_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金镐头（Super Lazy Alloy Pickaxe Head）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_PICKAXE_HEAD =
            registerItem("super_lazy_alloy_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 啄米鸡肉（Zhuomi Chicken）：0.5 饱食度(nutrition=1)，食用时长 1.5s(30t)；
    // 10% 中毒 20s + 90% 饥饿 30s + 100% 反胃 15s
    public static final FoodProperties ZHUOMI_CHICKEN_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 400, 0), 0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.9F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> ZHUOMI_CHICKEN = registerItem("zhuomi_chicken",
            () -> new Item(new Item.Properties().stacksTo(64).food(ZHUOMI_CHICKEN_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // ===== 四档棒（手柄部件，均为普通物品，最大堆叠 64） =====

    // 超重结晶棒（Super Heavy Crystal Rod）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_ROD = registerItem("super_heavy_crystal_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 复合米饭棒（Compound Rice Meal Rod）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_ROD = registerItem("compound_rice_meal_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 清洁复合棒（Cleansing Compound Rod）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_ROD = registerItem("cleansing_compound_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金棒（Super Lazy Alloy Rod）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_ROD = registerItem("super_lazy_alloy_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 五档镐（PickaxeItem 工具类，最大堆叠 1；镐头+棒合成的成品） =====

    // 超重结晶镐（Super Heavy Crystal Pickaxe）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_PICKAXE =
            registerItem("super_heavy_crystal_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.SUPER_HEAVY_CRYSTAL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 复合米饭镐（Compound Rice Meal Pickaxe）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PICKAXE =
            registerItem("compound_rice_meal_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.COMPOUND_RICE_MEAL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 清洁复合镐（Cleansing Compound Pickaxe）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PICKAXE =
            registerItem("cleansing_compound_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.CLEANSING_COMPOUND, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 超懒合金镐（Super Lazy Alloy Pickaxe）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_PICKAXE =
            registerItem("super_lazy_alloy_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.SUPER_LAZY_ALLOY, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 最懒天堕镐（Laziest Heavenfall Pickaxe）：传说级，堕落之心修复
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_PICKAXE =
            registerItem("laziest_heavenfall_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.LAZIEST_HEAVENFALL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // ===== 盗米者系列 + 米魂残天（均为普通物品，最大堆叠 64；盗米者生物蛋除外） =====

    // 盗米之心（Rice Stealer Heart）
    public static final RegistryObject<Item> RICE_STEALER_HEART = registerItem("rice_stealer_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 盗米者生物蛋：米白底/贼墨黑斑（实体 rice_stealer 当前为 PendingBossEntity 占位，后续替换真实生物）
    public static final RegistryObject<Item> RICE_STEALER_SPAWN_EGG =
            registerItem("rice_stealer_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_STEALER, 0xEDE6D2, 0x3A3A4A, new Item.Properties()));

    // 米魂之泪（Rice Soul Tear）
    public static final RegistryObject<Item> RICE_SOUL_TEAR = registerItem("rice_soul_tear",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 残天翼（Remnant Heaven Wing）
    public static final RegistryObject<Item> REMNANT_HEAVEN_WING = registerItem("remnant_heaven_wing",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 超懒合金箭（Super Lazy Alloy Arrow）：可发射箭矢，命中 250 伤害 + 中毒 XX 10s
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_ARROW = registerItem("super_lazy_alloy_arrow",
            () -> new MimiArrowItem(250.0D, 20, new Item.Properties().stacksTo(64)));

    // ===== 残/仙/神兽系列掉落（均为普通物品，最大堆叠 64；精灵帽仅为同名物品不可穿戴） =====

    // 虚无之触（Void Touch）
    public static final RegistryObject<Item> VOID_TOUCH = registerItem("void_touch",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 残花枝（Faded Flower Branch）
    public static final RegistryObject<Item> FADED_FLOWER_BRANCH = registerItem("faded_flower_branch",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 天凤之喙（Celestial Phoenix Beak）
    public static final RegistryObject<Item> CELESTIAL_PHOENIX_BEAK = registerItem("celestial_phoenix_beak",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 残堕米精（Remnant Fallen Rice Essence）
    public static final RegistryObject<Item> REMNANT_FALLEN_RICE_ESSENCE =
            registerItem("remnant_fallen_rice_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 残堕锭（Remnant Fallen Ingot）
    public static final RegistryObject<Item> REMNANT_FALLEN_INGOT = registerItem("remnant_fallen_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 鲛泪（Mermaid Tear）
    public static final RegistryObject<Item> MERMAID_TEAR = registerItem("mermaid_tear",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 蛟龙爪（Jiaolong Talon）
    public static final RegistryObject<Item> JIAOLONG_TALON = registerItem("jiaolong_talon",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 蜃楼骨（Mirage Bone）
    public static final RegistryObject<Item> MIRAGE_BONE = registerItem("mirage_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 精灵帽（Elf Hat）：普通物品【非装备】，不可穿戴
    public static final RegistryObject<Item> ELF_HAT = registerItem("elf_hat",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 噬月狼爪（Moon Devourer Wolf Claw）
    public static final RegistryObject<Item> MOON_DEVOURER_WOLF_CLAW =
            registerItem("moon_devourer_wolf_claw",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 堕落台（Fallen Altar）：放置后对应 ModBlocks.FALLEN_ALTAR =====
    public static final RegistryObject<Item> FALLEN_ALTAR = registerItem("fallen_altar",
            () -> new BlockItem(ModBlocks.FALLEN_ALTAR.get(), new Item.Properties()));

    // ===== 堕落十字架 / 浸染液 / 深染系列（含待定占位，不进创造标签页） =====

    // 堕落十字架（Fallen Cross）：放置后对应 ModBlocks.FALLEN_CROSS（GeckoLib 装饰方块）
    public static final RegistryObject<Item> FALLEN_CROSS = registerItem("fallen_cross",
            () -> new BlockItem(ModBlocks.FALLEN_CROSS.get(), new Item.Properties()));

    // 残堕深渊岩（Remnant Fallen Abyss Rock）：深渊层原生岩石方块，深板岩质地，镐挖掘等级 3 = 钻石镐
    public static final RegistryObject<Item> REMNANT_FALLEN_ABYSS_ROCK =
            registerItem("remnant_fallen_abyss_rock",
                    () -> new BlockItem(ModBlocks.REMNANT_FALLEN_ABYSS_ROCK.get(), new Item.Properties()));

    // 残堕基岩（Remnant Fallen Bedrock）：深渊层致密基岩方块，镐挖掘等级 3 = 钻石镐
    public static final RegistryObject<Item> REMNANT_FALLEN_BEDROCK =
            registerItem("remnant_fallen_bedrock",
                    () -> new BlockItem(ModBlocks.REMNANT_FALLEN_BEDROCK.get(), new Item.Properties()));

    // 浸染液（Dyeing Solution）：深染系列材料的浸染原料
    public static final RegistryObject<Item> DYEING_SOLUTION = registerItem("dyeing_solution",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染残天翼（Deep-Dyed Remnant Heaven Wing）：鞘翅类装备，
    // 直接继承原版 ElytraItem——胸甲槽穿戴、空中跳跃即可滑翔，耐久 432（滑翔每秒损耗1点），
    // .durability(432) 自动锁定最大堆叠为 1，无需也不能再链 .stacksTo(1)
    public static final RegistryObject<Item> DEEP_DYED_REMNANT_HEAVEN_WING =
            registerItem("deep_dyed_remnant_heaven_wing",
                    () -> new ElytraItem(new Item.Properties().durability(432)));

    // 深染虚无之触（Deep-Dyed Void Touch）
    public static final RegistryObject<Item> DEEP_DYED_VOID_TOUCH =
            registerItem("deep_dyed_void_touch",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染残花枝（Deep-Dyed Faded Flower Branch）
    public static final RegistryObject<Item> DEEP_DYED_FADED_FLOWER_BRANCH =
            registerItem("deep_dyed_faded_flower_branch",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染天凤之喙（Deep-Dyed Celestial Phoenix Beak）
    public static final RegistryObject<Item> DEEP_DYED_CELESTIAL_PHOENIX_BEAK =
            registerItem("deep_dyed_celestial_phoenix_beak",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染残堕米精（Deep-Dyed Remnant Fallen Rice Essence）
    public static final RegistryObject<Item> DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE =
            registerItem("deep_dyed_remnant_fallen_rice_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染鲛泪（Deep-Dyed Mermaid Tear）
    public static final RegistryObject<Item> DEEP_DYED_MERMAID_TEAR =
            registerItem("deep_dyed_mermaid_tear",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染蛟龙爪（Deep-Dyed Jiaolong Talon）
    public static final RegistryObject<Item> DEEP_DYED_JIAOLONG_TALON =
            registerItem("deep_dyed_jiaolong_talon",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染蜃楼骨（Deep-Dyed Mirage Bone）
    public static final RegistryObject<Item> DEEP_DYED_MIRAGE_BONE =
            registerItem("deep_dyed_mirage_bone",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染精灵帽（Deep-Dyed Elf Hat）：普通物品【非装备】，不可穿戴
    public static final RegistryObject<Item> DEEP_DYED_ELF_HAT =
            registerItem("deep_dyed_elf_hat",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 深染噬月狼爪（Deep-Dyed Moon Devourer Wolf Claw）
    public static final RegistryObject<Item> DEEP_DYED_MOON_DEVOURER_WOLF_CLAW =
            registerItem("deep_dyed_moon_devourer_wolf_claw",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 粗制残堕世界精华（Coarse Remnant Fallen World Essence）
    public static final RegistryObject<Item> COARSE_REMNANT_FALLEN_WORLD_ESSENCE =
            registerItem("coarse_remnant_fallen_world_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 方块物品 BlockItem（破坏掉落本身，方块本体见 block.ModBlocks） =====

    // 碾磨台（Grinding Table）：石质工作台方块，镐挖掘等级 3 = 钻石镐
    public static final RegistryObject<Item> GRINDING_TABLE = registerItem("grinding_table",
            () -> new BlockItem(ModBlocks.GRINDING_TABLE.get(), new Item.Properties()));

    // 磨制残堕深渊岩（Polished Remnant Fallen Abyss Rock）：深板岩质地方块，镐挖掘等级 3 = 钻石镐
    public static final RegistryObject<Item> POLISHED_REMNANT_FALLEN_ABYSS_ROCK =
            registerItem("polished_remnant_fallen_abyss_rock",
                    () -> new BlockItem(ModBlocks.POLISHED_REMNANT_FALLEN_ABYSS_ROCK.get(), new Item.Properties()));

    // 杵界台（Pestle Realm Table）：金属仪式工作台方块，镐挖掘等级 3 = 钻石镐
    public static final RegistryObject<Item> PESTLE_REALM_TABLE = registerItem("pestle_realm_table",
            () -> new BlockItem(ModBlocks.PESTLE_REALM_TABLE.get(), new Item.Properties()));

    // 磨制残堕世界精华（Polished Remnant Fallen World Essence）：粗制精华的磨制成品，
    // 磨制残堕精华液的材料，普通物品，最大堆叠 64
    public static final RegistryObject<Item> POLISHED_REMNANT_FALLEN_WORLD_ESSENCE =
            registerItem("polished_remnant_fallen_world_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 磨制残堕精华液（Polished Remnant Fallen Essence Solution）：
    // 普通物品非流体、药水瓶造型（贴图/模型见资源文件），最大堆叠 64
    public static final RegistryObject<Item> POLISHED_REMNANT_FALLEN_ESSENCE_SOLUTION =
            registerItem("polished_remnant_fallen_essence_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 凝锭台（Ingot Condensation Table）：金属工艺机器方块，镐挖掘等级 2 = 铁镐
    public static final RegistryObject<Item> INGOT_CONDENSATION_TABLE = registerItem("ingot_condensation_table",
            () -> new BlockItem(ModBlocks.INGOT_CONDENSATION_TABLE.get(), new Item.Properties()));

    // ===== 残堕世界材料 / 令牌 / 生熟食材 / 香料（食材均为食物，最大堆叠 64） =====

    // 残堕世界精华锭（Remnant Fallen World Essence Ingot）：普通材料物品
    public static final RegistryObject<Item> REMNANT_FALLEN_WORLD_ESSENCE_INGOT =
            registerItem("remnant_fallen_world_essence_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 世界之泪（Tear of the World）：普通材料物品
    public static final RegistryObject<Item> WORLD_TEAR = registerItem("world_tear",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 界主令牌（Realm Lord Token）：普通材料物品
    public static final RegistryObject<Item> REALM_LORD_TOKEN = registerItem("realm_lord_token",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 界天脑花（Realm-Heaven Brain Flower）：界天米掉落的普通材料物品
    public static final RegistryObject<Item> REALM_HEAVEN_BRAIN_FLOWER = registerItem("realm_heaven_brain_flower",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 熟界天胎花（Cooked Realm-Heaven Embryo Flower）：熟食，2 饱食度
    public static final RegistryObject<Item> COOKED_REALM_HEAVEN_EMBRYO_FLOWER =
            registerItem("cooked_realm_heaven_embryo_flower",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(4, 0.4F))));

    // 虚无触足（Void Touch Tentacle）：生食，1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> VOID_TOUCH_TENTACLE = registerItem("void_touch_tentacle",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 熟虚无触足（Cooked Void Touch Tentacle）：熟食，2.5 饱食度
    public static final RegistryObject<Item> COOKED_VOID_TOUCH_TENTACLE =
            registerItem("cooked_void_touch_tentacle",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 棘花瓣（Thorn Petal）：生食，1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> THORN_PETAL = registerItem("thorn_petal",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.2F, 0.3F))));

    // 熟棘花瓣（Cooked Thorn Petal）：熟食，2 饱食度
    public static final RegistryObject<Item> COOKED_THORN_PETAL = registerItem("cooked_thorn_petal",
            () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(4, 0.4F))));

    // 米凤肉块（Rice Phoenix Meat Chunk）：生食，1.5 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> RICE_PHOENIX_MEAT_CHUNK =
            registerItem("rice_phoenix_meat_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(3, 0.3F, 0.3F))));

    // 熟米凤肉块（Cooked Rice Phoenix Meat Chunk）：熟食，3.5 饱食度
    public static final RegistryObject<Item> COOKED_RICE_PHOENIX_MEAT_CHUNK =
            registerItem("cooked_rice_phoenix_meat_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(7, 0.8F))));

    // 特色堕落米（Specialty Fallen Rice）：生食（紫米黑碗外观），1.5 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> SPECIALTY_FALLEN_RICE = registerItem("specialty_fallen_rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(3, 0.3F, 0.3F))));

    // 熟特色堕落米（Cooked Specialty Fallen Rice）：特色堕落米的熟食版（红米灰碗外观），3.5 饱食度
    public static final RegistryObject<Item> COOKED_SPECIALTY_FALLEN_RICE =
            registerItem("cooked_specialty_fallen_rice",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(7, 0.7F))));

    // 浅鲛尾（Pale Mermaid Tail）：生食，1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> PALE_MERMAID_TAIL = registerItem("pale_mermaid_tail",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.2F, 0.3F))));

    // 熟浅鲛尾（Cooked Pale Mermaid Tail）：熟食，2.5 饱食度
    public static final RegistryObject<Item> COOKED_PALE_MERMAID_TAIL =
            registerItem("cooked_pale_mermaid_tail",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 望米伤鲛足（Wangmi Wounded Mermaid Leg）：生食，1 饱食度，40% 饥饿 I 30s
    public static final RegistryObject<Item> WANGMI_WOUNDED_MERMAID_LEG =
            registerItem("wangmi_wounded_mermaid_leg",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.4F))));

    // 熟望米伤鲛足（Cooked Wangmi Wounded Mermaid Leg）：熟食，2.5 饱食度
    public static final RegistryObject<Item> COOKED_WANGMI_WOUNDED_MERMAID_LEG =
            registerItem("cooked_wangmi_wounded_mermaid_leg",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 半蜃肉片（Half Mirage Meat Slice）：生食，1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> HALF_MIRAGE_MEAT_SLICE =
            registerItem("half_mirage_meat_slice",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 熟半蜃肉片（Cooked Half Mirage Meat Slice）：熟食，2.5 饱食度
    public static final RegistryObject<Item> COOKED_HALF_MIRAGE_MEAT_SLICE =
            registerItem("cooked_half_mirage_meat_slice",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 香袋（Fragrant Pouch）：普通物品（香料袋），最大堆叠 64
    public static final RegistryObject<Item> FRAGRANT_POUCH = registerItem("fragrant_pouch",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 香粉（Fragrant Powder）：普通物品（粉末材料），最大堆叠 64
    public static final RegistryObject<Item> FRAGRANT_POWDER = registerItem("fragrant_powder",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 噬鹫翅（Devourer Vulture Wing）：生食，1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> DEVOURER_VULTURE_WING = registerItem("devourer_vulture_wing",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 熟噬鹫翅（Cooked Devourer Vulture Wing）：熟食，3 饱食度
    public static final RegistryObject<Item> COOKED_DEVOURER_VULTURE_WING =
            registerItem("cooked_devourer_vulture_wing",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(6, 0.7F))));

    // 大碗（Big Bowl）：米木制大碗餐具，普通物品，最大堆叠 64
    public static final RegistryObject<Item> BIG_BOWL = registerItem("big_bowl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 锅（Pot）：方块物品 BlockItem；基础铁制烹饪锅方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> POT = registerItem("pot",
            () -> new BlockItem(ModBlocks.POT.get(), new Item.Properties()));

    // 大锅（Big Pot）：方块物品 BlockItem；铜箍加固的大型烹饪锅方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> BIG_POT = registerItem("big_pot",
            () -> new BlockItem(ModBlocks.BIG_POT.get(), new Item.Properties()));

    // 天虚棘脑汤（Celestial Void Thorn Brain Soup）：熟食，饱食度 10；力量 20/1min、生命恢复 100/5min、迅捷 10/2min
    public static final RegistryObject<Item> CELESTIAL_VOID_THORN_BRAIN_SOUP =
            registerEffectFood("celestial_void_thorn_brain_soup", 10,
                    effect(() -> MobEffects.DAMAGE_BOOST, 20, MIN_1),
                    effect(() -> MobEffects.REGENERATION, 100, MIN_5),
                    effect(() -> MobEffects.MOVEMENT_SPEED, 10, MIN_2));

    // 香混肉片（Fragrant Mixed Meat Slices）：熟食，饱食度 10；虚弱 1/1min、生命恢复 255/1min
    public static final RegistryObject<Item> FRAGRANT_MIXED_MEAT_SLICE =
            registerEffectFood("fragrant_mixed_meat_slice", 10,
                    effect(() -> MobEffects.WEAKNESS, 1, MIN_1),
                    effect(() -> MobEffects.REGENERATION, 255, MIN_1));

    // 酱香双鲛尾（Sauce-Glazed Twin Mermaid Tails）：熟食，饱食度 10；迅捷 20/2min、跳跃提升 5/5min
    public static final RegistryObject<Item> SAUCE_GLAZED_TWIN_MERMAID_TAIL =
            registerEffectFood("sauce_glazed_twin_mermaid_tail", 10,
                    effect(() -> MobEffects.MOVEMENT_SPEED, 20, MIN_2),
                    effect(() -> MobEffects.JUMP, 5, MIN_5));

    // 天脑汁（Celestial Brain Juice）：熟食（饮品），2 饱食度
    public static final RegistryObject<Item> CELESTIAL_BRAIN_JUICE =
            registerItem("celestial_brain_juice",
                    () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(4, 0.4F))));

    // 酱脑（Sauced Brain）：熟食，2.5 饱食度
    public static final RegistryObject<Item> SAUCED_BRAIN = registerItem("sauced_brain",
            () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 米堕汁（Fallen Rice Juice）：熟食（饮品），2 饱食度
    public static final RegistryObject<Item> FALLEN_RICE_JUICE = registerItem("fallen_rice_juice",
            () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(4, 0.3F))));

    // 凤肉泥（Phoenix Meat Paste）：生食（肉泥原料），1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> PHOENIX_MEAT_PASTE = registerItem("phoenix_meat_paste",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 凤球（Phoenix Ball）：熟食（肉丸），2.5 饱食度
    public static final RegistryObject<Item> PHOENIX_BALL = registerItem("phoenix_ball",
            () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(5, 0.6F))));

    // 盘子（Plate）：普通物品（餐具），最大堆叠 64
    public static final RegistryObject<Item> PLATE = registerItem("plate",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 酱烤鹫翅（Sauce-Roasted Vulture Wing）：熟食，饱食度 10；创造飞行/1min、生命恢复 50/3min
    public static final RegistryObject<Item> SAUCE_ROASTED_VULTURE_WING =
            registerEffectFood("sauce_roasted_vulture_wing", 10,
                    effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_1),
                    effect(() -> MobEffects.REGENERATION, 50, MIN_3));

    // 米蜃油脂（Rice Mirage Grease）：米蜃掉落的油脂材料，普通物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_MIRAGE_GREASE = registerItem("rice_mirage_grease",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 染鲛油脂（Dyed Mermaid Grease）：浅染鲛掉落的油脂材料，普通物品，最大堆叠 64
    public static final RegistryObject<Item> DYED_MERMAID_GREASE = registerItem("dyed_mermaid_grease",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 油瓶（Oil Bottle）：普通物品（油料），最大堆叠 64
    public static final RegistryObject<Item> OIL_BOTTLE = registerItem("oil_bottle",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 米木纸盘（Rice Paper Plate）：米木薄板制成的一次性纸盘，普通物品，最大堆叠 64；烤箱的合成配料
    public static final RegistryObject<Item> RICE_PAPER_PLATE = registerItem("rice_paper_plate",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 腌料（Marinade）：普通物品（调料），最大堆叠 64
    public static final RegistryObject<Item> MARINADE = registerItem("marinade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 腌鹫翅（Marinated Vulture Wing）：生食（腌制），1.5 饱食度，20% 饥饿 I 30s
    public static final RegistryObject<Item> MARINATED_VULTURE_WING =
            registerItem("marinated_vulture_wing",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(3, 0.3F, 0.2F))));

    // 堕米肉丸汤（Fallen Rice Meatball Soup）：熟食，饱食度 10；缓慢 2/3min、生命恢复 255/1min、力量 50/1min
    public static final RegistryObject<Item> FALLEN_RICE_MEATBALL_SOUP =
            registerEffectFood("fallen_rice_meatball_soup", 10,
                    effect(() -> MobEffects.MOVEMENT_SLOWDOWN, 2, MIN_3),
                    effect(() -> MobEffects.REGENERATION, 255, MIN_1),
                    effect(() -> MobEffects.DAMAGE_BOOST, 50, MIN_1));

    // 蛟鲛肉泥（Jiao Mermaid Meat Paste）：生食（肉泥原料），1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> JIAO_MERMAID_MEAT_PASTE =
            registerItem("jiao_mermaid_meat_paste",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 凤蜃肉泥（Phoenix Mirage Meat Paste）：生食（肉泥原料），1 饱食度，30% 饥饿 I 30s
    public static final RegistryObject<Item> PHOENIX_MIRAGE_MEAT_PASTE =
            registerItem("phoenix_mirage_meat_paste",
                    () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.3F))));

    // 珍贵调料（Precious Seasoning）：普通物品（调料），最大堆叠 64
    public static final RegistryObject<Item> PRECIOUS_SEASONING = registerItem("precious_seasoning",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 油爆虚无触（Oil-Seared Void Tentacle）：熟食，饱食度 10；迅捷 10/2min、力量 30/3min
    public static final RegistryObject<Item> OIL_SEARED_VOID_TENTACLE =
            registerEffectFood("oil_seared_void_tentacle", 10,
                    effect(() -> MobEffects.MOVEMENT_SPEED, 10, MIN_2),
                    effect(() -> MobEffects.DAMAGE_BOOST, 30, MIN_3));

    // 烧烤粉（BBQ Powder）：普通物品（调料），最大堆叠 64
    public static final RegistryObject<Item> BBQ_POWDER = registerItem("bbq_powder",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 花香油（Floral Fragrance Oil）：普通物品（油料），最大堆叠 64
    public static final RegistryObject<Item> FLORAL_FRAGRANCE_OIL = registerItem("floral_fragrance_oil",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 复合油（Compound Oil）：普通物品（油料），最大堆叠 64
    public static final RegistryObject<Item> COMPOUND_OIL = registerItem("compound_oil",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 蛟足炒饭（Mermaid Leg Fried Rice）：熟食，饱食度 10；迅捷 20/1min、力量 100/1min
    public static final RegistryObject<Item> MERMAID_LEG_FRIED_RICE =
            registerEffectFood("mermaid_leg_fried_rice", 10,
                    effect(() -> MobEffects.MOVEMENT_SPEED, 20, MIN_1),
                    effect(() -> MobEffects.DAMAGE_BOOST, 100, MIN_1));

    // 龙凤花汤（Dragon-Phoenix Flower Soup）：熟食，饱食度 10；生命恢复 255/1min、生命提升 50/3min、虚弱 5/2min
    public static final RegistryObject<Item> DRAGON_PHOENIX_FLOWER_SOUP =
            registerEffectFood("dragon_phoenix_flower_soup", 10,
                    effect(() -> MobEffects.REGENERATION, 255, MIN_1),
                    effect(() -> MobEffects.HEALTH_BOOST, 50, MIN_3),
                    effect(() -> MobEffects.WEAKNESS, 5, MIN_2));

    // 凤伤汤（Phoenix Wound Soup）：熟食，饱食度 10；缓慢 1/1min、生命恢复 200/2min、力量 5/5min
    public static final RegistryObject<Item> PHOENIX_WOUND_SOUP = registerEffectFood("phoenix_wound_soup", 10,
            effect(() -> MobEffects.MOVEMENT_SLOWDOWN, 1, MIN_1),
            effect(() -> MobEffects.REGENERATION, 200, MIN_2),
            effect(() -> MobEffects.DAMAGE_BOOST, 5, MIN_5));

    // 蜃楼幻想（Mirage Illusion）：熟食，饱食度 10；反胃 255/30s、瞬间恢复 255
    public static final RegistryObject<Item> MIRAGE_ILLUSION = registerEffectFood("mirage_illusion", 10,
            effect(() -> MobEffects.CONFUSION, 255, SEC_30),
            effect(() -> MobEffects.HEAL, 255, MIN_5));

    // 花香蛟尾（Floral Mermaid Tail）：熟食，饱食度 10；生命恢复 255/1min、生命提升 50/1min
    public static final RegistryObject<Item> FLORAL_MERMAID_TAIL =
            registerEffectFood("floral_mermaid_tail", 10,
                    effect(() -> MobEffects.REGENERATION, 255, MIN_1),
                    effect(() -> MobEffects.HEALTH_BOOST, 50, MIN_1));

    // 海兽相交饭（Sea Beast Jiao Rice）：熟食，饱食度 10；反胃 255/1min、虚弱 255/30s、生命恢复 255/1min、瞬间治疗 255、生命提升 255/1min
    public static final RegistryObject<Item> SEA_BEAST_JIAO_RICE = registerEffectFood("sea_beast_jiao_rice", 10,
            effect(() -> MobEffects.CONFUSION, 255, MIN_1),
            effect(() -> MobEffects.WEAKNESS, 255, SEC_30),
            effect(() -> MobEffects.REGENERATION, 255, MIN_1),
            effect(() -> MobEffects.HEAL, 255, MIN_1),
            effect(() -> MobEffects.HEALTH_BOOST, 255, MIN_1));

    // 佛幻想（Buddha Illusion）：熟食（豪华料理），饱食度 10；反胃 255/1min、失明/1min、迅捷 50/1min、创造飞行/1min、生命恢复 255/10s
    public static final RegistryObject<Item> BUDDHA_ILLUSION = registerEffectFood("buddha_illusion", 10,
            effect(() -> MobEffects.CONFUSION, 255, MIN_1),
            effect(() -> MobEffects.BLINDNESS, 1, MIN_1),
            effect(() -> MobEffects.MOVEMENT_SPEED, 50, MIN_1),
            effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_1),
            effect(() -> MobEffects.REGENERATION, 255, SEC_10));

    // 米雪茶（Rice Snow Tea）：熟食（饮品），饱食度 10；生命恢复 255/30s、瞬间治疗 255、生命提升 255/1min
    public static final RegistryObject<Item> RICE_SNOW_TEA = registerEffectFood("rice_snow_tea", 10,
            effect(() -> MobEffects.REGENERATION, 255, SEC_30),
            effect(() -> MobEffects.HEAL, 255, SEC_30),
            effect(() -> MobEffects.HEALTH_BOOST, 255, MIN_1));

    // 煎龙凤饼（Pan-Fried Dragon-Phoenix Cake）：熟食，饱食度 10；生命恢复 255/3min、中毒 100/3min
    public static final RegistryObject<Item> PAN_FRIED_DRAGON_PHOENIX_CAKE =
            registerEffectFood("pan_fried_dragon_phoenix_cake", 10,
                    effect(() -> MobEffects.REGENERATION, 255, MIN_3),
                    effect(() -> MobEffects.POISON, 100, MIN_3));

    // 天堕茶（Celestial Fallen Tea）：熟食（饮品），饱食度 10；创造飞行/1min、缓慢 2/2min
    public static final RegistryObject<Item> CELESTIAL_FALLEN_TEA = registerEffectFood("celestial_fallen_tea", 10,
            effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_1),
            effect(() -> MobEffects.MOVEMENT_SLOWDOWN, 2, MIN_2));

    // 凤包翅（Phoenix-Wrapped Wing）：熟食，饱食度 10；创造飞行/1min、力量 10/1min
    public static final RegistryObject<Item> PHOENIX_WRAPPED_WING =
            registerEffectFood("phoenix_wrapped_wing", 10,
                    effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_1),
                    effect(() -> MobEffects.DAMAGE_BOOST, 10, MIN_1));

    // 腌翅包（Marinated Wing Wrap）：生食（腌制），1.5 饱食度，20% 饥饿 I 30s
    public static final RegistryObject<Item> MARINATED_WING_WRAP = registerItem("marinated_wing_wrap",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(3, 0.3F, 0.2F))));

    // 腌肉片（Marinated Meat Slices）：生食（腌制），1 饱食度，20% 饥饿 I 30s
    public static final RegistryObject<Item> MARINATED_MEAT_SLICE = registerItem("marinated_meat_slice",
            () -> new Item(new Item.Properties().stacksTo(64).food(rawFood(2, 0.3F, 0.2F))));

    // 肉片粥（Meat Slice Congee）：熟食，饱食度 10；力量 20/3min、迅捷 20/5min
    public static final RegistryObject<Item> MEAT_SLICE_CONGEE = registerEffectFood("meat_slice_congee", 10,
            effect(() -> MobEffects.DAMAGE_BOOST, 20, MIN_3),
            effect(() -> MobEffects.MOVEMENT_SPEED, 20, MIN_5));

    // 肢体烤串（Limb Skewer）：熟食，3 饱食度
    public static final RegistryObject<Item> LIMB_SKEWER = registerItem("limb_skewer",
            () -> new Item(new Item.Properties().stacksTo(64).food(cookedFood(6, 0.7F))));

    // 蜃虚龙凤烤串（Mirage Void Dragon-Phoenix Skewer）：熟食，饱食度 10；反胃 255/30s、创造飞行/5min、生命恢复 255/30s
    public static final RegistryObject<Item> MIRAGE_VOID_DRAGON_PHOENIX_SKEWER =
            registerEffectFood("mirage_void_dragon_phoenix_skewer", 10,
                    effect(() -> MobEffects.CONFUSION, 255, SEC_30),
                    effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_5),
                    effect(() -> MobEffects.REGENERATION, 255, SEC_30));

    // 饭团（Onigiri）：熟食，饱食度 666；生命恢复 255/2min、缓慢 5/1min（注意：rice_ball「米团」是另一个可投掷物品，故本物品注册名为 onigiri）
    public static final RegistryObject<Item> ONIGIRI = registerEffectFood("onigiri", 666,
            effect(() -> MobEffects.REGENERATION, 255, MIN_2),
            effect(() -> MobEffects.MOVEMENT_SLOWDOWN, 5, MIN_1));

    // 海带片（Kelp Slices）：普通物品（食材），最大堆叠 64
    public static final RegistryObject<Item> KELP_SLICE = registerItem("kelp_slice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 天脑饭团（Celestial Brain Rice Ball）：熟食，饱食度 10；反胃 255/1min、创造飞行/2min、迅捷 10/3min
    public static final RegistryObject<Item> CELESTIAL_BRAIN_RICE_BALL =
            registerEffectFood("celestial_brain_rice_ball", 10,
                    effect(() -> MobEffects.CONFUSION, 255, MIN_1),
                    effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_2),
                    effect(() -> MobEffects.MOVEMENT_SPEED, 10, MIN_3));

    // 浅鲛饭团（Pale Mermaid Rice Ball）：熟食，饱食度 10；虚弱 5/1min、迅捷 30/2min、力量 50/30s
    public static final RegistryObject<Item> PALE_MERMAID_RICE_BALL =
            registerEffectFood("pale_mermaid_rice_ball", 10,
                    effect(() -> MobEffects.WEAKNESS, 5, MIN_1),
                    effect(() -> MobEffects.MOVEMENT_SPEED, 30, MIN_2),
                    effect(() -> MobEffects.DAMAGE_BOOST, 50, SEC_30));

    // 蛟足饭团（Mermaid Leg Rice Ball）：熟食，饱食度 10；力量 20/2min、生命恢复 50/1min
    public static final RegistryObject<Item> MERMAID_LEG_RICE_BALL =
            registerEffectFood("mermaid_leg_rice_ball", 10,
                    effect(() -> MobEffects.DAMAGE_BOOST, 20, MIN_2),
                    effect(() -> MobEffects.REGENERATION, 50, MIN_1));

    // 鹫翅饭团（Vulture Wing Rice Ball）：熟食，饱食度 10；创造飞行/30s、力量 20/1min、迅捷 10/1min
    public static final RegistryObject<Item> VULTURE_WING_RICE_BALL =
            registerEffectFood("vulture_wing_rice_ball", 10,
                    effect(ModEffects.CREATIVE_FLIGHT::get, 1, SEC_30),
                    effect(() -> MobEffects.DAMAGE_BOOST, 20, MIN_1),
                    effect(() -> MobEffects.MOVEMENT_SPEED, 10, MIN_1));

    // 超级大乱炖（Super Grand Stew）：熟食，豪华大杂烩炖菜，饱食度 666；创造飞行 15min、力量 255/30min、急迫 255/15min、迅捷 50/15min、生命提升 255/30min、瞬间恢复 255、防火/1h、隐身/20min、夜视/1h
    public static final RegistryObject<Item> SUPER_GRAND_STEW = registerEffectFood("super_grand_stew", 666,
            effect(ModEffects.CREATIVE_FLIGHT::get, 1, MIN_15),
            effect(() -> MobEffects.DAMAGE_BOOST, 255, MIN_30),
            effect(() -> MobEffects.DIG_SPEED, 255, MIN_15),
            effect(() -> MobEffects.MOVEMENT_SPEED, 50, MIN_15),
            effect(() -> MobEffects.HEALTH_BOOST, 255, MIN_30),
            effect(() -> MobEffects.HEAL, 255, MIN_30),
            effect(() -> MobEffects.FIRE_RESISTANCE, 1, HOUR_1),
            effect(() -> MobEffects.INVISIBILITY, 1, MIN_20),
            effect(() -> MobEffects.NIGHT_VISION, 1, HOUR_1));

    // 搅碎器（Crusher）：方块物品 BlockItem；食材加工机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> CRUSHER = registerItem("crusher",
            () -> new BlockItem(ModBlocks.CRUSHER.get(), new Item.Properties()));

    // 搅拌器（Mixer）：方块物品 BlockItem；食材加工机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> MIXER = registerItem("mixer",
            () -> new BlockItem(ModBlocks.MIXER.get(), new Item.Properties()));

    // 小煮锅（Small Cooking Pot）：方块物品 BlockItem；带支脚的小型烹饪锅方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> SMALL_COOKING_POT = registerItem("small_cooking_pot",
            () -> new BlockItem(ModBlocks.SMALL_COOKING_POT.get(), new Item.Properties()));

    // 煮锅（Cooking Pot）：方块物品 BlockItem；铜箍加固的标准烹饪锅方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> COOKING_POT = registerItem("cooking_pot",
            () -> new BlockItem(ModBlocks.COOKING_POT.get(), new Item.Properties()));

    // 大煮锅（Large Cooking Pot）：方块物品 BlockItem；烹饪机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> LARGE_COOKING_POT = registerItem("large_cooking_pot",
            () -> new BlockItem(ModBlocks.LARGE_COOKING_POT.get(), new Item.Properties()));

    // 超大煮锅（Extra-Large Cooking Pot）：方块物品 BlockItem；烹饪机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> EXTRA_LARGE_COOKING_POT = registerItem("extra_large_cooking_pot",
            () -> new BlockItem(ModBlocks.EXTRA_LARGE_COOKING_POT.get(), new Item.Properties()));

    // 巨无霸大大大煮锅（Jumbo Mega Cooking Pot）：方块物品 BlockItem；在超大煮锅之上的特级烹饪方块，
    // 镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> JUMBO_MEGA_COOKING_POT = registerItem("jumbo_mega_cooking_pot",
            () -> new BlockItem(ModBlocks.JUMBO_MEGA_COOKING_POT.get(), new Item.Properties()));

    // 铁锅（Iron Wok）：方块物品 BlockItem；烹饪机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> IRON_WOK = registerItem("iron_wok",
            () -> new BlockItem(ModBlocks.IRON_WOK.get(), new Item.Properties()));

    // 烤箱（Oven）：方块物品 BlockItem；烹饪机器方块，镐挖掘等级 3 = 钻石镐，破坏掉落本身，方块本体见 block.ModBlocks
    public static final RegistryObject<Item> OVEN = registerItem("oven",
            () -> new BlockItem(ModBlocks.OVEN.get(), new Item.Properties()));

    // ===== 龙系素材/残堕宗罪装备/箭支批次 =====

    // 盖子（Pot Lid）：烹饪机器链公共部件（搅碎器/搅拌器/三级煮锅/铁锅都要用），
    // 木按钮 + 重型压力板合成，普通物品，最大堆叠 64
    public static final RegistryObject<Item> POT_LID = registerItem("pot_lid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 勺子（Spoon）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> SPOON = registerItem("spoon",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 骊珠（Li Pearl）：黑龙颔下之珠，普通物品，最大堆叠 64
    public static final RegistryObject<Item> LI_PEARL = registerItem("li_pearl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙血（Dragon Blood）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_BLOOD = registerItem("dragon_blood",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙鳞（Dragon Scale）：普通物品，最大堆叠 64；9 个可在工作台压缩成龙鳞块（配方待定）
    public static final RegistryObject<Item> DRAGON_SCALE = registerItem("dragon_scale",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 宇罪龙逆鳞（Yu Sin Dragon Reverse Scale）：普通物品，最大堆叠 64；残堕工具修复材料
    public static final RegistryObject<Item> YU_SIN_DRAGON_REVERSE_SCALE = registerItem("yu_sin_dragon_reverse_scale",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙骨（Dragon Bone）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_BONE = registerItem("dragon_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 界主龙心（Realm Lord Dragon Heart）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> REALM_LORD_DRAGON_HEART = registerItem("realm_lord_dragon_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 宇罪龙头（Yu Sin Dragon Head）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> YU_SIN_DRAGON_HEAD = registerItem("yu_sin_dragon_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 宗罪龙头冠（Cardinal Sin Dragon Crown）：普通物品，最大堆叠 64；残堕宗罪套装修复材料
    public static final RegistryObject<Item> CARDINAL_SIN_DRAGON_CROWN = registerItem("cardinal_sin_dragon_crown",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙骨架构（Dragon Bone Framework）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_BONE_FRAMEWORK = registerItem("dragon_bone_framework",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙血精华（Dragon Blood Essence）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_BLOOD_ESSENCE = registerItem("dragon_blood_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙鳞块（Block of Dragon Scale）：龙鳞的压缩存储块，
    // 放置后对应 ModBlocks.DRAGON_SCALE_BLOCK；钻石镐（挖掘等级3）采集，破坏掉落本身
    public static final RegistryObject<Item> DRAGON_SCALE_BLOCK = registerItem("dragon_scale_block",
            () -> new BlockItem(ModBlocks.DRAGON_SCALE_BLOCK.get(), new Item.Properties()));

    // 终罪骊珠（Final Sin Li Pearl）：终罪龙之力凝结的暗紫骊珠（骊珠的终罪形态），普通物品，最大堆叠 64
    public static final RegistryObject<Item> FINAL_SIN_LI_PEARL = registerItem("final_sin_li_pearl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 终罪逆鳞（Final Sin Reverse Scale）：终罪龙逆生之鳞（注册名去掉历史后缀 mmjm），普通物品，最大堆叠 64
    public static final RegistryObject<Item> FINAL_SIN_REVERSE_SCALE = registerItem("final_sin_reverse_scale",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 终罪龙心（Final Sin Dragon Heart）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> FINAL_SIN_DRAGON_HEART = registerItem("final_sin_dragon_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 终罪龙头（Final Sin Dragon Head）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> FINAL_SIN_DRAGON_HEAD = registerItem("final_sin_dragon_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- 残堕宗罪套装（GeckoLib 4 动态 3D 盔甲，最大堆叠 1；修复材料宗罪龙头冠，
    //      材质 REMNANT_FALLEN_CARDINAL_SIN；模型/动画见 RemnantFallenCardinalSinArmorItem） ----
    public static final RegistryObject<Item> REMNANT_FALLEN_CARDINAL_SIN_HELMET =
            registerItem("remnant_fallen_cardinal_sin_helmet",
                    () -> new RemnantFallenCardinalSinArmorItem(ModArmorMaterials.REMNANT_FALLEN_CARDINAL_SIN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> REMNANT_FALLEN_CARDINAL_SIN_CHESTPLATE =
            registerItem("remnant_fallen_cardinal_sin_chestplate",
                    () -> new RemnantFallenCardinalSinArmorItem(ModArmorMaterials.REMNANT_FALLEN_CARDINAL_SIN, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> REMNANT_FALLEN_CARDINAL_SIN_LEGGINGS =
            registerItem("remnant_fallen_cardinal_sin_leggings",
                    () -> new RemnantFallenCardinalSinArmorItem(ModArmorMaterials.REMNANT_FALLEN_CARDINAL_SIN, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> REMNANT_FALLEN_CARDINAL_SIN_BOOTS =
            registerItem("remnant_fallen_cardinal_sin_boots",
                    () -> new RemnantFallenCardinalSinArmorItem(ModArmorMaterials.REMNANT_FALLEN_CARDINAL_SIN, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // 残堕宗罪之弓（Remnant Fallen Cardinal Sin Bow）：耐久 2304（设耐久后自动堆叠 1），
    // 伤害倍率 ×100（全模组最高）；GeckoLib 3D 模型（MubangongBowItem，通用弓 mubangong 资源）
    public static final RegistryObject<Item> REMNANT_FALLEN_CARDINAL_SIN_BOW =
            registerItem("remnant_fallen_cardinal_sin_bow",
                    () -> new MubangongBowItem(100.0D, "canduozongzuizhigong", new Item.Properties().durability(2304)));

    // 残堕崩解之镐（Remnant Fallen Disintegration Pickaxe）：PickaxeItem，最大堆叠 1，
    // ModItemTiers.REMNANT_FALLEN（修复材料宇罪龙逆鳞），攻击伤害/攻速与已有各档镐一致
    public static final RegistryObject<Item> REMNANT_FALLEN_DISINTEGRATION_PICKAXE =
            registerItem("remnant_fallen_disintegration_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.REMNANT_FALLEN, 1, -2.8F, new Item.Properties().stacksTo(1)));

    // 残堕逆鳞之剑（Remnant Fallen Reverse Scale Sword）：SwordItem，最大堆叠 1，
    // ModItemTiers.REMNANT_FALLEN（修复材料宇罪龙逆鳞），慢速重剑（+5 伤害/-2.8 攻速）
    public static final RegistryObject<Item> REMNANT_FALLEN_REVERSE_SCALE_SWORD =
            registerItem("remnant_fallen_reverse_scale_sword",
                    () -> new SwordItem(ModItemTiers.REMNANT_FALLEN, 5, -2.8F, new Item.Properties().stacksTo(1)));

    // 龙鳞之威（Dragon Scale Might）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_SCALE_MIGHT = registerItem("dragon_scale_might",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙元（Dragon Yuan）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_YUAN = registerItem("dragon_yuan",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 龙首之怒（Dragon Head Wrath）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> DRAGON_HEAD_WRATH = registerItem("dragon_head_wrath",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 终罪界心（Final Sin Realm Heart）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> FINAL_SIN_REALM_HEART = registerItem("final_sin_realm_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 净化架构（Purification Framework）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> PURIFICATION_FRAMEWORK = registerItem("purification_framework",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 世界净化器（World Purifier）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> WORLD_PURIFIER = registerItem("world_purifier",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- 箭支：均为可发射箭矢（MimiArrowItem），命中结算固定伤害 + 中毒（等级 0 无中毒） ----
    // 饭晶箭（Rice Crystal Arrow）：5 伤害 + 中毒 I 10s
    public static final RegistryObject<Item> RICE_CRYSTAL_ARROW = registerItem("rice_crystal_arrow",
            () -> new MimiArrowItem(5.0D, 1, new Item.Properties().stacksTo(64)));

    // 超结晶箭（Super Crystal Arrow）：10 伤害 + 中毒 II 10s
    public static final RegistryObject<Item> SUPER_CRYSTAL_ARROW = registerItem("super_crystal_arrow",
            () -> new MimiArrowItem(10.0D, 2, new Item.Properties().stacksTo(64)));

    // 饭晶合金箭（Rice Crystal Alloy Arrow）：20 伤害 + 中毒 III 10s
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_ARROW = registerItem("rice_crystal_alloy_arrow",
            () -> new MimiArrowItem(20.0D, 3, new Item.Properties().stacksTo(64)));

    // 超重结晶箭（Super Heavy Crystal Arrow）：25 伤害 + 中毒 V 10s
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_ARROW = registerItem("super_heavy_crystal_arrow",
            () -> new MimiArrowItem(25.0D, 5, new Item.Properties().stacksTo(64)));

    // 复合米饭箭（Compound Rice Meal Arrow）：100 伤害 + 中毒 VII 10s
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_ARROW = registerItem("compound_rice_meal_arrow",
            () -> new MimiArrowItem(100.0D, 7, new Item.Properties().stacksTo(64)));

    // 清洁复合箭（Cleansing Compound Arrow）：100 伤害 + 中毒 X 10s
    public static final RegistryObject<Item> CLEANSING_COMPOUND_ARROW = registerItem("cleansing_compound_arrow",
            () -> new MimiArrowItem(100.0D, 10, new Item.Properties().stacksTo(64)));

    // ============================ 物品注册区结束 ============================

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
