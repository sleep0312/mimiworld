package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 米米世界物品注册类。所有物品类新内容统一在此注册。
 * 注册的物品会被 ModCreativeTabs 按命名空间自动收录进「米米世界」创造标签页。
 *
 * ================== 物品编码备忘录（仅代码层可见，游戏内不显示） ==================
 * 每个物品按【注册顺序】自动获得从 1 开始的连续编号，配方等代码用 byCode(n) 引用，
 * 不需要写物品注册名。编号由 registerItem() 内的计数器自动分配，严禁手工指定。
 *
 * 铁律：新物品只能【追加在本类注册区末尾】，禁止插入到已有物品之间或调整顺序，
 * 否则历史编号会整体错位，已写好的配方引用将全部错乱。
 *
 * 编码表（每新增一个物品，在此补一行注释）：
 *   1 = rice（米）
 *   2 = cooked_rice（熟米）
 *   3 = rice_ball（米团，可投掷）
 *   4 = cooked_rice_ball（熟米团，可投掷）
 *   5 = rice_log（米原木，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   6 = rice_planks（米木板，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   7 = rice_stick（米木棍，可燃烧 300t = 1.5 个物品）
 *   8 = rice_leaves（米树叶，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   9 = rice_bucket（米桶，堆叠 1，BucketItem 绑定米汤流体 mimiworld:rice_soup，见 fluid.ModFluids/RiceBucketItem）
 *   10 = rice_sapling（米树苗，方块物品 BlockItem；骨粉催熟长成米树，方块本体见 block.ModBlocks）
 *   11 = hardened_rice（硬化米，方块物品 BlockItem；石头类方块，铁镐挖掘等级2，方块本体见 block.ModBlocks）
 *   12 = deepslate_hardened_rice（深层硬化米，方块物品 BlockItem；石头类方块，铁镐挖掘等级2，方块本体见 block.ModBlocks）
 *   13 = rice_bedrock（基米，方块物品 BlockItem；基岩类方块，生存不可破坏，方块本体见 block.ModBlocks）
 *   14 = rice_grass（米草，方块物品 BlockItem，自定义 RiceGrassItem；可放置的草类植物，剪刀采集；可食用，方块本体见 block.ModBlocks）
 *   15 = rice_fruit（米果，类似原版苹果的食物；漂浮I/反胃I/虚弱I，食用2s）
 *   16 = crystallized_rice（结晶米，类似原版锭的矿石材料）
 *   17 = crystallized_rice_ore（结晶米矿石，方块物品 BlockItem；下界合金镐挖掘等级4，掉经验3-7，方块本体见 block.ModBlocks）
 *   18 = recrystallized_rice_ore（重结晶米矿石，方块物品 BlockItem；深板岩质地，下界合金镐，掉经验3-7，烧炼得19号；原名 deepslate_crystallized_rice_ore 已按中文名整体改名，方块本体见 block.ModBlocks）
 *   19 = recrystallized_rice（重结晶米，精炼锭类材料；由18号重结晶米矿石烧炼得到）
 *   20 = combustible_rice（可燃米，普通材料；熔炉燃料 6400t = 可烧 32 个物品，见 event.ModFuelEvents）
 *   21 = combustible_rice_ore（可燃米矿石，方块物品 BlockItem；石头类矿石，下界合金镐，掉经验3-7，烧炼得20号，方块本体见 block.ModBlocks）
 *   22 = mud_rice（泥米，方块物品 BlockItem；泥巴质地，铲挖掘等级2=铁铲，方块本体见 block.ModBlocks）
 *   23 = reinforced_rice_stick（强化米棍，普通材料物品，最大堆叠 64）
 *   24 = rice_baton（米棍，普通材料物品，最大堆叠 64；英文名 Rice Baton，避开 7 号米木棍已占用的 Rice Stick）
 *   25 = rice_flour（米粉，普通材料物品，最大堆叠 64；英文名 Rice Flour）
 *   26 = coarse_rice_block（粗米块，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   27 = rice_maker（造米机，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   28 = rice_block（米块，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   29 = rice_pounding_table（锤米台，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 * ================================================================================
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MimiWorld.MODID);

    // 自动编号计数器与双向映射表
    private static int nextItemCode = 0;
    private static final Map<Integer, RegistryObject<Item>> ITEM_BY_CODE = new HashMap<>();
    private static final Map<ResourceLocation, Integer> CODE_BY_ITEM = new HashMap<>();

    /**
     * 注册物品并自动分配下一个连续编号（1, 2, 3 ...）。
     * 物品字段必须按声明顺序从上到下排列，编号即注册顺序。
     */
    private static RegistryObject<Item> registerItem(String name, Supplier<Item> factory) {
        int code = ++nextItemCode;
        RegistryObject<Item> item = ITEMS.register(name, factory);
        ITEM_BY_CODE.put(code, item);
        CODE_BY_ITEM.put(item.getId(), code);
        return item;
    }

    /** 按编码获取物品实例（配方代码里用编码表达需求时使用），编码不存在则抛异常 */
    public static Item byCode(int code) {
        RegistryObject<Item> item = ITEM_BY_CODE.get(code);
        if (item == null) {
            throw new IllegalArgumentException("不存在的物品编码: " + code);
        }
        return item.get();
    }

    /** 按编码获取物品注册名（生成配方 JSON / 数据包内容时使用） */
    public static ResourceLocation idOfCode(int code) {
        RegistryObject<Item> item = ITEM_BY_CODE.get(code);
        if (item == null) {
            throw new IllegalArgumentException("不存在的物品编码: " + code);
        }
        return item.getId();
    }

    /** 查询某物品对应的编码（反向查询），未注册物品抛异常 */
    public static int codeOf(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        Integer code = key == null ? null : CODE_BY_ITEM.get(key);
        if (code == null) {
            throw new IllegalArgumentException("未登记编码的物品: " + key);
        }
        return code;
    }

    // ============================ 物品注册区（只能末尾追加） ============================

    // 米（Rice）：可食用，最大堆叠 64；食用后 100% 触发四种负面效果（20 tick = 1 秒，amplifier 从 0 起算）
    public static final FoodProperties RICE_FOOD = new FoodProperties.Builder()
            .nutrition(1)            // 饱食度回复（饥饿栏鸡腿数，后续可调）
            .saturationMod(0.1F)     // 饱和度，后续可调
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 2), 1.0F)   // 反胃 III 10s
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)       // 中毒 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F) // 缓慢 I 10s
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)       // 饥饿 I 30s
            .build();

    // 编码 1 = rice（米）
    public static final RegistryObject<Item> RICE = registerItem("rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FOOD)));

    // 熟米（Cooked Rice）：食用回复 2.5 个鸡腿（nutrition 单位=半个鸡腿 → 5）；
    // 实际饱和度 = 2 × nutrition × saturationMod = 2×5×0.3 = 3；100% 反胃 I 5s；食用耗时 1s（默认食物为 32 tick）
    public static final FoodProperties COOKED_RICE_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F) // 反胃 I 5s
            .build();

    // 编码 2 = cooked_rice（熟米）
    public static final RegistryObject<Item> COOKED_RICE = registerItem("cooked_rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_RICE_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 20; // 食用时间 1 秒
                }
            });

    // 编码 3 = rice_ball（米团）：可投掷，命中造成 1 伤害 + 反胃 I/缓慢 I 各 10s，投掷冷却 2s（逻辑见 RiceBallItem / RiceBallProjectile）
    public static final RegistryObject<Item> RICE_BALL = registerItem("rice_ball",
            () -> new RiceBallItem(new Item.Properties().stacksTo(64)));

    // 编码 4 = cooked_rice_ball（熟米团）：可投掷，命中 2.5 伤害 + 缓慢 III/失明 II 各 10s，投掷冷却 2s（逻辑见 CookedRiceBallItem / CookedRiceBallProjectile）
    public static final RegistryObject<Item> COOKED_RICE_BALL = registerItem("cooked_rice_ball",
            () -> new CookedRiceBallItem(new Item.Properties().stacksTo(64)));

    // 编码 5 = rice_log（米原木）：方块物品，放置后对应 ModBlocks.RICE_LOG；破坏掉落自身（loot table），斧/挖掘等级 1 由方块标签控制
    public static final RegistryObject<Item> RICE_LOG = registerItem("rice_log",
            () -> new BlockItem(ModBlocks.RICE_LOG.get(), new Item.Properties()));

    // 编码 6 = rice_planks（米木板）：方块物品，放置后对应 ModBlocks.RICE_PLANKS；破坏掉落自身，斧可挖、挖掘等级 0（无 needs_*_tool 标签）
    public static final RegistryObject<Item> RICE_PLANKS = registerItem("rice_planks",
            () -> new BlockItem(ModBlocks.RICE_PLANKS.get(), new Item.Properties()));

    // 编码 7 = rice_stick（米木棍）：普通材料物品，最大堆叠 64；熔炉燃料 300t（可烧 1.5 个物品，见 event.ModFuelEvents）
    public static final RegistryObject<Item> RICE_STICK = registerItem("rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 8 = rice_leaves（米树叶）：方块物品，放置后对应 ModBlocks.RICE_LEAVES；破坏掉落自身，剪刀可快速采集、挖掘等级 0，燃料 100t
    public static final RegistryObject<Item> RICE_LEAVES = registerItem("rice_leaves",
            () -> new BlockItem(ModBlocks.RICE_LEAVES.get(), new Item.Properties()));

    // 编码 9 = rice_bucket（米桶）：米汤桶，最大堆叠 1；RiceBucketItem 继承 BucketItem，
    // 构造器内部绑定 fluid.ModFluids.RICE_SOUP（米汤源流体），行为同原版水桶
    public static final RegistryObject<Item> RICE_BUCKET = registerItem("rice_bucket",
            () -> new RiceBucketItem(new Item.Properties().stacksTo(1)));

    // 编码 10 = rice_sapling（米树苗）：方块物品，放置后对应 ModBlocks.RICE_SAPLING；可骨粉催熟/随机刻长成米树（白桦形态，米原木+米树叶）
    public static final RegistryObject<Item> RICE_SAPLING = registerItem("rice_sapling",
            () -> new BlockItem(ModBlocks.RICE_SAPLING.get(), new Item.Properties()));

    // 编码 11 = hardened_rice（硬化米）：石头类方块物品，放置后对应 ModBlocks.HARDENED_RICE
    public static final RegistryObject<Item> HARDENED_RICE = registerItem("hardened_rice",
            () -> new BlockItem(ModBlocks.HARDENED_RICE.get(), new Item.Properties()));

    // 编码 12 = deepslate_hardened_rice（深层硬化米）：石头类方块物品，放置后对应 ModBlocks.DEEPSLATE_HARDENED_RICE
    public static final RegistryObject<Item> DEEPSLATE_HARDENED_RICE = registerItem("deepslate_hardened_rice",
            () -> new BlockItem(ModBlocks.DEEPSLATE_HARDENED_RICE.get(), new Item.Properties()));

    // 编码 13 = rice_bedrock（基米）：基岩类方块物品，放置后对应 ModBlocks.RICE_BEDROCK；仅创造模式获取
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

    // 编码 14 = rice_grass（米草）：可放置的草类植物方块物品（自定义 RiceGrassItem，右键可种草也可食用）；
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

    // 编码 15 = rice_fruit（米果）：普通食物物品，最大堆叠 64；食用耗时 2s（默认食物 32t ≈ 1.6s，这里覆盖为 40t）
    public static final RegistryObject<Item> RICE_FRUIT = registerItem("rice_fruit",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FRUIT_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 40; // 食用时间 2 秒
                }
            });

    // 编码 16 = crystallized_rice（结晶米）：类似原版锭的普通矿石材料，最大堆叠 64
    public static final RegistryObject<Item> CRYSTALLIZED_RICE = registerItem("crystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 17 = crystallized_rice_ore（结晶米矿石）：石头类矿石方块物品，放置后对应 ModBlocks.CRYSTALLIZED_RICE_ORE；
    // 下界合金镐才能采集，破坏掉 3~7 经验，可烧炼成 16 号结晶米锭
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_ORE = registerItem("crystallized_rice_ore",
            () -> new BlockItem(ModBlocks.CRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 编码 18 = recrystallized_rice_ore（重结晶米矿石）：深板岩质地矿石，
    // 放置后对应 ModBlocks.RECRYSTALLIZED_RICE_ORE；下界合金镐采集，破坏掉 3~7 经验，烧炼得 19 号重结晶米
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE_ORE = registerItem("recrystallized_rice_ore",
            () -> new BlockItem(ModBlocks.RECRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 编码 19 = recrystallized_rice（重结晶米）：精炼锭类普通材料，最大堆叠 64；由 18 号重结晶米矿石熔炉/高炉烧炼得到
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE = registerItem("recrystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 20 = combustible_rice（可燃米）：普通材料，最大堆叠 64；
    // 作为熔炉燃料可烧 32 个物品（6400 tick），燃料值在 event.ModFuelEvents 中赋予
    public static final RegistryObject<Item> COMBUSTIBLE_RICE = registerItem("combustible_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 21 = combustible_rice_ore（可燃米矿石）：石头类矿石方块物品，放置后对应 ModBlocks.COMBUSTIBLE_RICE_ORE；
    // 下界合金镐采集，破坏掉 3~7 经验，熔炉/高炉烧炼得 20 号可燃米
    public static final RegistryObject<Item> COMBUSTIBLE_RICE_ORE = registerItem("combustible_rice_ore",
            () -> new BlockItem(ModBlocks.COMBUSTIBLE_RICE_ORE.get(), new Item.Properties()));

    // 编码 22 = mud_rice（泥米）：泥巴质地方块物品，放置后对应 ModBlocks.MUD_RICE；铁铲（挖掘等级2）采集
    public static final RegistryObject<Item> MUD_RICE = registerItem("mud_rice",
            () -> new BlockItem(ModBlocks.MUD_RICE.get(), new Item.Properties()));

    // 编码 23 = reinforced_rice_stick（强化米棍，Reinforced Rice Stick）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> REINFORCED_RICE_STICK = registerItem("reinforced_rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 24 = rice_baton（米棍，Rice Baton）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_BATON = registerItem("rice_baton",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 25 = rice_flour（米粉，Rice Flour）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_FLOUR = registerItem("rice_flour",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 26 = coarse_rice_block（粗米块，Coarse Rice Block）：石头类方块物品，放置后对应 ModBlocks.COARSE_RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> COARSE_RICE_BLOCK = registerItem("coarse_rice_block",
            () -> new BlockItem(ModBlocks.COARSE_RICE_BLOCK.get(), new Item.Properties()));

    // 编码 27 = rice_maker（造米机，Rice Maker）：石头类方块物品，放置后对应 ModBlocks.RICE_MAKER；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_MAKER = registerItem("rice_maker",
            () -> new BlockItem(ModBlocks.RICE_MAKER.get(), new Item.Properties()));

    // 编码 28 = rice_block（米块，Rice Block）：石头类方块物品，放置后对应 ModBlocks.RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_BLOCK = registerItem("rice_block",
            () -> new BlockItem(ModBlocks.RICE_BLOCK.get(), new Item.Properties()));

    // 编码 29 = rice_pounding_table（锤米台，Rice Pounding Table）：石头类方块物品，放置后对应 ModBlocks.RICE_POUNDING_TABLE；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_POUNDING_TABLE = registerItem("rice_pounding_table",
            () -> new BlockItem(ModBlocks.RICE_POUNDING_TABLE.get(), new Item.Properties()));

    // ============================ 新物品写在这行以上（末尾追加） ============================

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
