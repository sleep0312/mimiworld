package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界实体注册类（投掷物、友好/敌对生物、Boss 等）。
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MimiWorld.MODID);

    // 米团投掷物：小型可投掷实体，参数参照雪球
    public static final RegistryObject<EntityType<RiceBallProjectile>> RICE_BALL =
            ENTITY_TYPES.register("rice_ball", () ->
                    EntityType.Builder.<RiceBallProjectile>of(RiceBallProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("rice_ball"));

    // 熟米团投掷物：小型可投掷实体，参数参照雪球
    public static final RegistryObject<EntityType<CookedRiceBallProjectile>> COOKED_RICE_BALL =
            ENTITY_TYPES.register("cooked_rice_ball", () ->
                    EntityType.Builder.<CookedRiceBallProjectile>of(CookedRiceBallProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("cooked_rice_ball"));

    // ===== 生物蛋绑定的七大 Boss（实体待定，当前统一用 PendingBossEntity 占位） =====
    // 真实 Boss 实现后仅需替换工厂引用 PendingBossEntity::new，生物蛋（ModItems）无需改动。

    // 米米猴王：灵巧中型
    public static final RegistryObject<EntityType<PendingBossEntity>> MIMI_MONKEY_KING =
            registerBoss("mimi_monkey_king", 0.7F, 1.9F);
    // 饭晶虎王：四足猛兽
    public static final RegistryObject<EntityType<PendingBossEntity>> RICE_CRYSTAL_TIGER_KING =
            registerBoss("rice_crystal_tiger_king", 1.2F, 1.3F);
    // 超晶牛王：壮硕大体型
    public static final RegistryObject<EntityType<PendingBossEntity>> SUPER_CRYSTAL_COW_KING =
            registerBoss("super_crystal_cow_king", 1.4F, 1.7F);
    // 重晶兔王：小型
    public static final RegistryObject<EntityType<PendingBossEntity>> HEAVY_CRYSTAL_RABBIT_KING =
            registerBoss("heavy_crystal_rabbit_king", 0.7F, 0.9F);
    // 复晶天青蟒：长形低伏
    public static final RegistryObject<EntityType<PendingBossEntity>> COMPOUND_AZURE_PYTHON =
            registerBoss("compound_azure_python", 1.0F, 0.8F);
    // 清洁长命夔：巨型走兽
    public static final RegistryObject<EntityType<PendingBossEntity>> CLEANSING_LONGEVITY_KUI =
            registerBoss("cleansing_longevity_kui", 1.6F, 2.2F);
    // 最懒终末龙：超巨型飞行 Boss（已转正：GeckoLib 宗罪龙模型，末影龙式盘旋/俯冲 AI，火焰免疫）
    public static final RegistryObject<EntityType<LaziestFinalDragonEntity>> LAZIEST_FINAL_DRAGON =
            ENTITY_TYPES.register("laziest_final_dragon", () ->
                    EntityType.Builder.<LaziestFinalDragonEntity>of(LaziestFinalDragonEntity::new, MobCategory.MONSTER)
                            .sized(2.5F, 3.5F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("laziest_final_dragon"));

    // ===== 盗米者生物蛋绑定的实体：MicroDuckEntity（GeckoLib microduck 模型，小型 0.5×1.0） =====
    // 注册名为 rice_stealer，生物蛋（ForgeSpawnEggItem 绑定 RICE_STEALER）与之同名对应
    public static final RegistryObject<EntityType<MicroDuckEntity>> RICE_STEALER =
            ENTITY_TYPES.register("rice_stealer", () ->
                    EntityType.Builder.<MicroDuckEntity>of(MicroDuckEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 1.0F)
                            .clientTrackingRange(8)
                            .build("rice_stealer"));

    // ===== 黏米之蛛：GeckoLib spider 模型，行为同原版蜘蛛但全天敌对（1.4×0.9 中型怪物） =====
    // 生物蛋已注册（ModItems.STICKY_RICE_SPIDER_SPAWN_EGG）；自然生成规则后续再加
    public static final RegistryObject<EntityType<StickyRiceSpiderEntity>> STICKY_RICE_SPIDER =
            ENTITY_TYPES.register("sticky_rice_spider", () ->
                    EntityType.Builder.<StickyRiceSpiderEntity>of(StickyRiceSpiderEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .clientTrackingRange(8)
                            .build("sticky_rice_spider"));

    // ===== 米羊羊：GeckoLib mimi_sheep 模型，友好生物 0.9×1.3（对齐原版羊碰撞箱） =====
    // 生物逻辑同原版羊（漂浮/受击惊慌逃跑/避水游荡/张望，羊叫声），但不注册吃草 AI。
    // 生物蛋已注册（ModItems.MIMI_SHEEP_SPAWN_EGG）。
    public static final RegistryObject<EntityType<MimiSheepEntity>> MIMI_SHEEP =
            ENTITY_TYPES.register("mimi_sheep", () ->
                    EntityType.Builder.<MimiSheepEntity>of(MimiSheepEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(8)
                            .build("mimi_sheep"));

    // ===== 啄米鸡：GeckoLib zhuomi_chicken 模型，友好生物 0.6×0.7（对齐原版鸡碰撞箱） =====
    // 生物逻辑同原版鸡（漂浮/受击惊慌逃跑/避水游荡/张望，鸡叫声），暂不注册繁殖/下蛋 AI。
    // 可用 /summon mimiworld:zhuomi_chicken 召唤测试。
    public static final RegistryObject<EntityType<ZhuomiChickenEntity>> ZHUOMI_CHICKEN =
            ENTITY_TYPES.register("zhuomi_chicken", () ->
                    EntityType.Builder.<ZhuomiChickenEntity>of(ZhuomiChickenEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(8)
                            .build("zhuomi_chicken"));

    // ============================================================================
    // 米米系列生物：当前统一用 PlaceholderRiceMobEntity 占位，真实 AI/模型实现后只替换工厂。
    // 友好生物 = MobCategory.CREATURE；敌对生物 = MobCategory.MONSTER。
    // 每个实体的最大血量/攻击力在 ModEntityAttributes 中逐项注册。
    // ============================================================================

    // —— 友好生物 ——
    // 米哞牛 10 心：已转正，GeckoLib mimouniu 模型（idle/walk 动画，大米主题贴图），碰撞箱对齐原版牛
    public static final RegistryObject<EntityType<MimouniuEntity>> MIMOO_COW =
            ENTITY_TYPES.register("mimoo_cow", () ->
                    EntityType.Builder.<MimouniuEntity>of(MimouniuEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .clientTrackingRange(8)
                            .build("mimoo_cow"));
    // 米哼猪：已转正，GeckoLib mihengzhu 模型（idle/walk 动画，大米主题贴图），碰撞箱对齐原版猪
    public static final RegistryObject<EntityType<MihengPigEntity>> MIHENG_PIG =
            ENTITY_TYPES.register("miheng_pig", () ->
                    EntityType.Builder.<MihengPigEntity>of(MihengPigEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(8)
                            .build("miheng_pig"));
    // 贪米鱼：已转正，GeckoLib tanmiyu 模型（swim/idle 动画）。
    // 首个水生生物：类别 WATER_AMBIENT（同原版鳕鱼群），碰撞箱 0.5×0.4，水下生成/游泳 AI/可用水桶装起。
    // 注册名 tanmi_fish 保持不变，生物蛋/战利品表继续按此名引用。
    public static final RegistryObject<EntityType<TanmiFishEntity>> TANMI_FISH =
            ENTITY_TYPES.register("tanmi_fish", () ->
                    EntityType.Builder.<TanmiFishEntity>of(TanmiFishEntity::new, MobCategory.WATER_AMBIENT)
                            .sized(0.5F, 0.4F)
                            .clientTrackingRange(4)
                            .build("tanmi_fish"));
    // 喷米鱼：已转正，GeckoLib penmiyu 模型（swim/idle 动画）。
    // 第二个水生生物：类别 WATER_AMBIENT（同原版鳕鱼群），碰撞箱 0.5×0.4，水下生成/游泳 AI/可用水桶装起。
    // 注册名 penmi_fish 保持不变，生物蛋/喷米鱼囊掉落继续按此名引用。
    public static final RegistryObject<EntityType<PenmiFishEntity>> PENMI_FISH =
            ENTITY_TYPES.register("penmi_fish", () ->
                    EntityType.Builder.<PenmiFishEntity>of(PenmiFishEntity::new, MobCategory.WATER_AMBIENT)
                            .sized(0.5F, 0.4F)
                            .clientTrackingRange(4)
                            .build("penmi_fish"));
    // 米傀儡：已转正，GeckoLib rice_golem 模型（idle/walk 动画，稻草+麻绳大米主题贴图），
    // 碰撞箱 1.4×2.7、100 心，属性/音效对齐原版铁傀儡；生物蛋 ModItems.RICE_GOLEM_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<RiceGolemEntity>> RICE_GOLEM =
            ENTITY_TYPES.register("rice_golem", () ->
                    EntityType.Builder.<RiceGolemEntity>of(RiceGolemEntity::new, MobCategory.CREATURE)
                            .sized(1.4F, 2.7F)
                            .clientTrackingRange(8)
                            .build("rice_golem"));
    // 嗥米狼：已转正，GeckoLib haomi_wolf 模型（idle/walk 动画，狼灰+米白+稻草金大米主题贴图），
    // 碰撞箱 0.6×1.7（模型实测约 27px 高）、20 心，属性/音效对齐原版狼；生物蛋 ModItems.HAOMI_WOLF_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<HaomiWolfEntity>> HAOMI_WOLF =
            ENTITY_TYPES.register("haomi_wolf", () ->
                    EntityType.Builder.<HaomiWolfEntity>of(HaomiWolfEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.7F)
                            .clientTrackingRange(8)
                            .build("haomi_wolf"));
    // 米米骷髅：已转正，GeckoLib mimi_skeleton 模型（animation.idle/walk 动画，陈米骨色+稻绿鬼火眼+米袋腰裙贴图），
    // 碰撞箱 0.6×1.99（模型实测 33px 高，骨冠略高出箱体）、24 心，属性/音效对齐原版骷髅；生物蛋 ModItems.MIMI_SKELETON_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MimiSkeletonEntity>> MIMI_SKELETON =
            ENTITY_TYPES.register("mimi_skeleton", () ->
                    EntityType.Builder.<MimiSkeletonEntity>of(MimiSkeletonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("mimi_skeleton"));
    // 糊米古尸：已转正，敌对亡灵 GeckoLib humi_corpse 模型（animation.idle/walk/attack 动画，
    // 陈米尸肤+糊米痂+青灰官袍+额头符纸贴图，僵尸跳步态+双爪扑击），碰撞箱 0.7×1.9（模型实测 31.1px 高）、
    // 30 心，属性/音效对齐原版僵尸；生物蛋 ModItems.HUMI_CORPSE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<HumiCorpseEntity>> HUMI_CORPSE =
            ENTITY_TYPES.register("humi_corpse", () ->
                    EntityType.Builder.<HumiCorpseEntity>of(HumiCorpseEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.9F)
                            .clientTrackingRange(8)
                            .build("humi_corpse"));
    // 爪巴米者：已转正，敌对 GeckoLib pami_crawler 模型（animation.idle/walk/attack 动画，
    // 旧米麻袋+苦脸+稻芽贴图，四足爬行），碰撞箱 0.7×0.8、12 心；
    // 行为同原版苦力怕——不近战，靠近 3 格点燃 30tick 引信后自爆；生物蛋 ModItems.PAMI_CRAWLER_SPAWN_EGG
    public static final RegistryObject<EntityType<PamiCrawlerEntity>> PAMI_CRAWLER =
            ENTITY_TYPES.register("pami_crawler", () ->
                    EntityType.Builder.<PamiCrawlerEntity>of(PamiCrawlerEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 0.8F)
                            .clientTrackingRange(8)
                            .build("pami_crawler"));
    // 米巫：已转正，敌对 GeckoLib rice_witch 模型（animation.idle/walk/attack/throw 动画，
    // 黑紫丝绒+金边+米饰贴图，弯帽尖+后腰药袋药瓶），碰撞箱 0.6×1.95（帽尖略高出箱体）、
    // 30 心，远程丢溅射药水（中毒/缓慢/瞬间伤害）+近身魔法推击；生物蛋 ModItems.RICE_WITCH_SPAWN_EGG
    public static final RegistryObject<EntityType<RiceWitchEntity>> RICE_WITCH =
            ENTITY_TYPES.register("rice_witch", () ->
                    EntityType.Builder.<RiceWitchEntity>of(RiceWitchEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("rice_witch"));
    // 没米入：已转正，中立末影系 GeckoLib meimiru 模型（animation.idle/walk/attack 动画，
    // 末影暗紫+米白骨角爪+金稻穗+发光紫眼贴图，继承原版末影人：盯视/受击激怒+瞬移+怕水），
    // 模型实测约 34.5px（≈2.16 格）渲染放大 1.15 撑满 2.5 格箱体、碰撞箱 0.6×2.5、
    // 75 心（攻击 7/移速 0.3 对齐原版末影人）；生物蛋 ModItems.MOMI_ENTRANT_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MeimiruEntity>> MOMI_ENTRANT =
            ENTITY_TYPES.register("momi_entrant", () ->
                    EntityType.Builder.<MeimiruEntity>of(MeimiruEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.5F)
                            .clientTrackingRange(8)
                            .build("momi_entrant"));
    // 爆烈米灵 60 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> EXPLOSIVE_RICE_SPIRIT =
            registerPlaceholder("explosive_rice_spirit", MobCategory.CREATURE, 0.8F, 1.6F);
    // 雕米骷髅 60 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> CARVED_RICE_SKELETON =
            registerPlaceholder("carved_rice_skeleton", MobCategory.CREATURE, 0.6F, 2.1F);
    // 米莱马 12 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MILAI_HORSE =
            registerPlaceholder("milai_horse", MobCategory.CREATURE, 1.2F, 1.6F);
    // 米哼猪尸 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIHENG_PIG_CORPSE =
            registerPlaceholder("miheng_pig_corpse", MobCategory.CREATURE, 0.9F, 0.9F);
    // 米魂 9 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_SOUL =
            registerPlaceholder("rice_soul", MobCategory.CREATURE, 0.6F, 1.0F);

    // —— 敌对生物 ——
    // 界天米 200 心 / 攻击 200
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> JIETIAN_RICE =
            registerPlaceholder("jietian_rice", MobCategory.MONSTER, 1.0F, 2.2F);
    // 混沌不可名状之米界虚无触 1500 血 / 攻击 50
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> CHAOTIC_VOID_TOUCHER =
            registerPlaceholder("chaotic_void_toucher", MobCategory.MONSTER, 2.0F, 3.2F);
    // 污棘花 500 心 / 攻击 50
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> BLIGHTED_THORN_FLOWER =
            registerPlaceholder("blighted_thorn_flower", MobCategory.MONSTER, 0.9F, 1.8F);
    // 堕落米凤 1000 心 / 攻击 70
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> FALLEN_RICE_PHOENIX =
            registerPlaceholder("fallen_rice_phoenix", MobCategory.MONSTER, 1.4F, 2.2F);
    // 化灵米 5000 心 / 攻击 10
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HUALING_RICE =
            registerPlaceholder("hualing_rice", MobCategory.MONSTER, 1.0F, 2.0F);
    // 浅染鲛 750 心 / 攻击 1000
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PALE_MERMAID =
            registerPlaceholder("pale_mermaid", MobCategory.MONSTER, 0.8F, 1.6F);
    // 望米伤鲛 1000 心 / 攻击 300
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> WANGMI_WOUNDED_MERMAID =
            registerPlaceholder("wangmi_wounded_mermaid", MobCategory.MONSTER, 0.8F, 1.6F);
    // 米蜃 750 心 / 攻击 400
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_MIRAGE =
            registerPlaceholder("rice_mirage", MobCategory.MONSTER, 1.0F, 1.9F);
    // 懒精灵 400 心 / 攻击 100
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> LAZY_ELF =
            registerPlaceholder("lazy_elf", MobCategory.MONSTER, 0.6F, 1.2F);
    // 哮噬米狼鹫 500 心 / 攻击 500
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_WOLF_VULTURE =
            registerPlaceholder("rice_wolf_vulture", MobCategory.MONSTER, 1.2F, 1.9F);

    /** 占位生物注册：碰撞箱与生物类别随实体给定；属性在 ModEntityAttributes 单独注册。 */
    private static RegistryObject<EntityType<PlaceholderRiceMobEntity>> registerPlaceholder(
            String id, MobCategory category, float width, float height) {
        return ENTITY_TYPES.register(id, () ->
                EntityType.Builder.<PlaceholderRiceMobEntity>of(PlaceholderRiceMobEntity::new, category)
                        .sized(width, height)
                        .clientTrackingRange(category == MobCategory.MONSTER ? 12 : 8)
                        .build(id));
    }

    private static RegistryObject<EntityType<PendingBossEntity>> registerBoss(String id, float width, float height) {
        return ENTITY_TYPES.register(id, () ->
                EntityType.Builder.<PendingBossEntity>of(PendingBossEntity::new, MobCategory.MONSTER)
                        .sized(width, height)
                        .clientTrackingRange(12)
                        .fireImmune()
                        .build(id));
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
