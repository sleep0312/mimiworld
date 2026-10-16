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

    // 虚无触毒液：小型抛物线投掷物（史莱姆球贴图），命中造成伤害+中毒 II
    public static final RegistryObject<EntityType<VoidPoisonSpitEntity>> VOID_POISON_SPIT =
            ENTITY_TYPES.register("void_poison_spit", () ->
                    EntityType.Builder.<VoidPoisonSpitEntity>of(VoidPoisonSpitEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("void_poison_spit"));

    // 米米世界箭矢实体：8 种箭共用，携带固定伤害+命中中毒，视觉沿用原版箭模型/贴图
    public static final RegistryObject<EntityType<MimiArrowEntity>> MIMI_ARROW =
            ENTITY_TYPES.register("mimi_arrow", () ->
                    EntityType.Builder.<MimiArrowEntity>of(MimiArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("mimi_arrow"));

    // 最懒光束渲染载体：无碰撞/无AI/不存档的纯视觉实体，在最懒终末龙与锁定玩家之间渲染紫色光束
    public static final RegistryObject<EntityType<DragonBeamEntity>> DRAGON_BEAM =
            ENTITY_TYPES.register("dragon_beam", () ->
                    EntityType.Builder.<DragonBeamEntity>of(DragonBeamEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(16)
                            .updateInterval(Integer.MAX_VALUE)
                            .fireImmune()
                            .build("dragon_beam"));

    // ===== 生物蛋绑定的七大 Boss（实体待定，当前统一用 PendingBossEntity 占位） =====
    // 真实 Boss 实现后仅需替换工厂引用 PendingBossEntity::new，生物蛋（ModItems）无需改动。

    // 米米猴王扔出的金箍棒投掷物：无重力直线飞行 1 秒后追踪飞回，路径 20 伤害（MobCategory.MISC）
    public static final RegistryObject<EntityType<ThrownMonkeyStaffEntity>> THROWN_MONKEY_STAFF =
            ENTITY_TYPES.register("thrown_monkey_staff", () ->
                    EntityType.Builder.<ThrownMonkeyStaffEntity>of(ThrownMonkeyStaffEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("thrown_monkey_staff"));

    // 米米猴王：已转正，GeckoLib mimihouwang 模型（idle/walk/attack/attack_slam/throw 动画），
    // 灵巧中型陆地 Boss（普攻 5 + 劈地 7 CD5.5s + 扔棒 20 CD3s），碰撞箱 0.7×1.9，火焰免疫
    public static final RegistryObject<EntityType<MimiMonkeyKingEntity>> MIMI_MONKEY_KING =
            ENTITY_TYPES.register("mimi_monkey_king", () ->
                    EntityType.Builder.<MimiMonkeyKingEntity>of(MimiMonkeyKingEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.9F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("mimi_monkey_king"));
    // 饭晶虎王：已转正，GeckoLib fanjinghuwang 模型（idle/walk/attack/feast/slam 动画），
    // 直立型陆地猛兽 Boss（普攻 8 + 大吃一顿撕咬 20 CD3s + 对爪重击 25 CD6s），
    // 模型直立约 47px≈2.9 格，碰撞箱 1.2×2.9，火焰免疫
    public static final RegistryObject<EntityType<FanjinghuwangEntity>> RICE_CRYSTAL_TIGER_KING =
            ENTITY_TYPES.register("rice_crystal_tiger_king", () ->
                    EntityType.Builder.<FanjinghuwangEntity>of(FanjinghuwangEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 2.9F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("rice_crystal_tiger_king"));
    // 超晶牛王：已转正，GeckoLib super_crystal_cow_king 模型（idle/walk/attack/axe_combo/
    // charge_start/charge 动画，右臂 45° 斜擎巨斧），壮硕持斧陆地 Boss
    // （普攻 12 + 猛牛冲撞 20 CD30s + 三段巨斧 10/20/30 CD45s），
    // 模型直立约 41px≈2.6 格、肩甲宽约 24px，碰撞箱 1.5×2.6，火焰免疫
    public static final RegistryObject<EntityType<ChaojingniuwangEntity>> SUPER_CRYSTAL_COW_KING =
            ENTITY_TYPES.register("super_crystal_cow_king", () ->
                    EntityType.Builder.<ChaojingniuwangEntity>of(ChaojingniuwangEntity::new, MobCategory.MONSTER)
                            .sized(1.5F, 2.6F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("super_crystal_cow_king"));
    // 重晶兔王：已转正，GeckoLib heavy_crystal_rabbit_king 模型（idle/walk/attack/skill_slam/skill_enrage
    // 动画，米白+晶青晶刺+血红眼巨兔），敏捷型陆地 Boss（普攻 10 双爪连击 + 出其不意跃砸
    // 45 无视护甲 CD60s + 腥红烛照近战 ×3 持续 30s CD180s），模型直立约 54px≈3.4 格（耳尖），
    // 碰撞箱 1.4×3.2，火焰免疫
    public static final RegistryObject<EntityType<HeavyCrystalRabbitKingEntity>> HEAVY_CRYSTAL_RABBIT_KING =
            ENTITY_TYPES.register("heavy_crystal_rabbit_king", () ->
                    EntityType.Builder.<HeavyCrystalRabbitKingEntity>of(HeavyCrystalRabbitKingEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 3.2F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("heavy_crystal_rabbit_king"));
    // 复晶天青蟒：已转正，GeckoLib compound_azure_python 模型（idle/walk/attack/bite/kiss/tail_sweep
    // 动画，深玉蟒鳞+米白腹甲+青碧复晶+金边兜帽），低伏长身陆地 Boss（普攻 10 + 咬你命三千 16
    // 附中毒I/缠绕I CD180s + 羽蛇神之吻两连咬共 50 附中毒II CD15s + 扫尾 70 CD30s；缠绕期间玩家
    // 对其伤害 ×1.5），昂首展兜高约 3.2 格，碰撞箱 1.4×3.2，火焰免疫
    public static final RegistryObject<EntityType<CompoundAzurePythonEntity>> COMPOUND_AZURE_PYTHON =
            ENTITY_TYPES.register("compound_azure_python", () ->
                    EntityType.Builder.<CompoundAzurePythonEntity>of(CompoundAzurePythonEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 3.2F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("compound_azure_python"));
    // 清洁长命夔：已转正，GeckoLib qingjiechangmingkui 模型（idle/walk/attack/roar/stomp/triple_bite
    // 动画，苍青黑厚皮+青铜夔龙纹+雷电裂隙+发光青眼，单足无角牛形雷兽），巨型陆地 Boss
    // （普攻 15 + 巨吼音波 60 CD30s + 夔之践踏 100 CD45s + 三连咬 70×3 CD120s），
    // 模型高约 55px≈3.4 格、巨蹄宽约 22px，碰撞箱 2.2×3.4，火焰免疫
    public static final RegistryObject<EntityType<QingjiechangmingkuiEntity>> CLEANSING_LONGEVITY_KUI =
            ENTITY_TYPES.register("cleansing_longevity_kui", () ->
                    EntityType.Builder.<QingjiechangmingkuiEntity>of(QingjiechangmingkuiEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 3.4F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("cleansing_longevity_kui"));
    // 最懒终末龙：末影龙 Promax 狂暴强化版，超巨型陆空 Boss（GeckoLib：龙息/冲撞/最懒光束/撕咬/盘旋/嘶吼，火焰免疫）
    public static final RegistryObject<EntityType<LaziestFinalDragonEntity>> LAZIEST_FINAL_DRAGON =
            ENTITY_TYPES.register("laziest_final_dragon", () ->
                    EntityType.Builder.<LaziestFinalDragonEntity>of(LaziestFinalDragonEntity::new, MobCategory.MONSTER)
                            .sized(3.5F, 4.0F)
                            .clientTrackingRange(16)
                            .fireImmune()
                            .build("laziest_final_dragon"));

    // ===== 盗米者：已转正，GeckoLib daomizhe 狼人模型（idle/walk/attack 动画，
    // 米白绒毛+稻叶绿+稻穗金大米主题贴图），陆地敌对 0.8×2.1；近战 6 + 60s 偷快捷栏食物后逃走；
    // 生物蛋（ForgeSpawnEggItem 绑定 RICE_STEALER）与战利品表沿用 rice_stealer 注册名 =====
    public static final RegistryObject<EntityType<DaomizheEntity>> RICE_STEALER =
            ENTITY_TYPES.register("rice_stealer", () ->
                    EntityType.Builder.<DaomizheEntity>of(DaomizheEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 2.1F)
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
    // 碰撞箱 0.6×1.99（模型实测 33px 高，骨冠略高出箱体）、24 心，属性/音效对齐原版骷髅；
    // 类别 MONSTER：按本文件约定「敌对生物 = MobCategory.MONSTER」，在米米维度按原版骷髅的黑暗规律刷怪。
    // 注意实体自身仍继承 PathfinderMob，且因无攻击动画未注册攻击目标 AI，如需真正主动攻击需另加 AI；
    // 生物蛋 ModItems.MIMI_SKELETON_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MimiSkeletonEntity>> MIMI_SKELETON =
            ENTITY_TYPES.register("mimi_skeleton", () ->
                    EntityType.Builder.<MimiSkeletonEntity>of(MimiSkeletonEntity::new, MobCategory.MONSTER)
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
    // 爆烈米灵：已转正，敌对飞行 GeckoLib baoliemiling 模型（animation.baoliemiling.idle/walk/attack 动画，
    // 白米怒脸+金米棒+爆裂火红贴图），烈焰人式常驻悬浮飞行、火焰免疫、近战+三连发火米弹；
    // 碰撞箱 0.8×1.8（模型实测约 26.5px 高，含头顶火焰冠）、60 心；生物蛋 ModItems.EXPLOSIVE_RICE_SPIRIT_SPAWN_EGG
    public static final RegistryObject<EntityType<BaoliemilingEntity>> EXPLOSIVE_RICE_SPIRIT =
            ENTITY_TYPES.register("explosive_rice_spirit", () ->
                    EntityType.Builder.<BaoliemilingEntity>of(BaoliemilingEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.8F)
                            .clientTrackingRange(8)
                            .fireImmune()
                            .build("explosive_rice_spirit"));
    // 雕米骷髅：已转正，敌对亡灵 GeckoLib diaomikulou 模型（animation.diaomikulou.idle/walk/attack 动画，
    // 米白骨体+米金稻穗腰带+凋零黑布破裙+黑骨角骨爪+魂火眼贴图，双爪扑击），碰撞箱 0.6×2.2
    // （模型实测约 36.2px 高，骨冠略高出箱体）、60 心，属性/音效对齐原版骷髅；生物蛋 ModItems.CARVED_RICE_SKELETON_SPAWN_EGG
    public static final RegistryObject<EntityType<DiaomikulouEntity>> CARVED_RICE_SKELETON =
            ENTITY_TYPES.register("carved_rice_skeleton", () ->
                    EntityType.Builder.<DiaomikulouEntity>of(DiaomikulouEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.2F)
                            .clientTrackingRange(8)
                            .build("carved_rice_skeleton"));
    // 米莱马：已转正，敌对史莱姆型 GeckoLib 实体（animation.milaima.idle/walk/attack 动画，
    // 暖白米浆凝胶+悬浮米粒+酱油大眼+稻绿嫩芽贴图），继承原版史莱姆：随机蹦跳+朝目标扑跳近战+squish 音效，
    // 固定 2 号体型（碰撞箱 1.02×1.02）、12 心、接触伤害 4，死亡不分裂不掉黏液球；
    // 生物蛋 ModItems.MILAI_HORSE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MilaimaEntity>> MILAI_HORSE =
            ENTITY_TYPES.register("milai_horse", () ->
                    EntityType.Builder.<MilaimaEntity>of(MilaimaEntity::new, MobCategory.MONSTER)
                            .sized(1.02F, 1.02F)
                            .clientTrackingRange(8)
                            .build("milai_horse"));
    // 米哼猪尸：已转正，敌对亡灵 GeckoLib mihengzhushi 模型（animation.mihengzhushi.idle/walk/attack 动画，
    // 腐烂米猪尸体造型），陆地近战扑咬，碰撞箱 0.9×1.6（模型实测约 25.6px 高）、30 心；
    // 生物蛋 ModItems.MIHENG_PIG_CORPSE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MihengPigCorpseEntity>> MIHENG_PIG_CORPSE =
            ENTITY_TYPES.register("miheng_pig_corpse", () ->
                    EntityType.Builder.<MihengPigCorpseEntity>of(MihengPigCorpseEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 1.6F)
                            .clientTrackingRange(12)
                            .build("miheng_pig_corpse"));
    // 米魂：已转正，敌对飞行 GeckoLib mihun 模型（animation.mihun.idle/walk/attack 动画，
    // 伞状魂体+8 触须+魂火飘带），常驻悬浮飞行、触须近战扑击，碰撞箱 1.1×1.6（含触须约 24px 高）、9 心；
    // 生物蛋 ModItems.RICE_SOUL_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<RiceSoulEntity>> RICE_SOUL =
            ENTITY_TYPES.register("rice_soul", () ->
                    EntityType.Builder.<RiceSoulEntity>of(RiceSoulEntity::new, MobCategory.MONSTER)
                            .sized(1.1F, 1.6F)
                            .clientTrackingRange(12)
                            .build("rice_soul"));

    // —— 敌对生物 ——
    // 界天米 200 心 / 光束攻击 200：已转正，独眼空中悬浮 GeckoLib 飞行敌对实体
    // （jietianmi idle/walk/attack 动画），守卫者式凝视蓄力激光，渲染放大 1.2 倍，碰撞箱 1.3×1.9；
    // 生物蛋 ModItems.JIETIAN_RICE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<JietianmiEntity>> JIETIAN_RICE =
            ENTITY_TYPES.register("jietian_rice", () ->
                    EntityType.Builder.<JietianmiEntity>of(JietianmiEntity::new, MobCategory.MONSTER)
                            .sized(1.3F, 1.9F)
                            .clientTrackingRange(12)
                            .build("jietian_rice"));
    // 混沌不可名状之米界虚无触：已转正，扎根不动触手巢 Boss GeckoLib 模型
    // （idle/attack/attack_spit 动画），不会移动，近身触手抽击（50 伤害）+ 远程喷毒（中毒 II），
    // 模型静止约 1.2 格高、放大 1.5 倍渲染，碰撞箱 2.2×1.6；1500 血；生物蛋 ModItems.CHAOTIC_VOID_TOUCHER_SPAWN_EGG
    public static final RegistryObject<EntityType<ChaoticVoidToucherEntity>> CHAOTIC_VOID_TOUCHER =
            ENTITY_TYPES.register("chaotic_void_toucher", () ->
                    EntityType.Builder.<ChaoticVoidToucherEntity>of(ChaoticVoidToucherEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 1.6F)
                            .clientTrackingRange(12)
                            .build("chaotic_void_toucher"));
    // 污棘花：已转正，扎根不动敌对植物 GeckoLib wujihua 模型（animation.wujihua.idle/attack 动画，
    // 米白鳞茎+稻叶+厚唇米牙大嘴花造型），不会移动、原地张嘴啃咬进入范围的玩家，
    // 碰撞箱 0.9×1.8、500 心 / 攻击 50（沿用占位数值）；生物蛋 ModItems.BLIGHTED_THORN_FLOWER_SPAWN_EGG
    public static final RegistryObject<EntityType<WujihuaEntity>> BLIGHTED_THORN_FLOWER =
            ENTITY_TYPES.register("blighted_thorn_flower", () ->
                    EntityType.Builder.<WujihuaEntity>of(WujihuaEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 1.8F)
                            .clientTrackingRange(12)
                            .build("blighted_thorn_flower"));
    // 堕落米凤：已转正，陆空敌对 GeckoLib duoluomifeng 模型（animation.idle/walk/attack/flying 动画，
    // 米白堕落羽+黑翅尖+金冠贴图），幻翼式空中水平巡航/俯冲啄击，周期性落地休息+地面走动；
    // 碰撞箱 1.4×2.2（直立约 43px 高，冠顶略高出箱体）、1000 心 / 攻击 70；生物蛋 ModItems.FALLEN_RICE_PHOENIX_SPAWN_EGG
    public static final RegistryObject<EntityType<FallenRicePhoenixEntity>> FALLEN_RICE_PHOENIX =
            ENTITY_TYPES.register("fallen_rice_phoenix", () ->
                    EntityType.Builder.<FallenRicePhoenixEntity>of(FallenRicePhoenixEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 2.2F)
                            .clientTrackingRange(12)
                            .build("fallen_rice_phoenix"));
    // 化灵米：已转正，敌对飞行 GeckoLib hualingmi 模型（animation.hualingmi.idle/fly/attack 动画，
    // 米胎白脸+稻穗耳羽+灵青魂晶+雾蓝幽灵尾贴图），恼鬼式常驻无重力悬浮追击但不能穿墙、永不落地，近战扑击；
    // 碰撞箱 1.0×2.0（模型直立约 26px 高）、5000 心 / 攻击 10；生物蛋 ModItems.HUALING_RICE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<HualingmiEntity>> HUALING_RICE =
            ENTITY_TYPES.register("hualing_rice", () ->
                    EntityType.Builder.<HualingmiEntity>of(HualingmiEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 2.0F)
                            .clientTrackingRange(12)
                            .build("hualing_rice"));
    // 浅染鲛：已转正，水生敌对 GeckoLib qianranjiao 模型（idle/walk/attack 动画，
    // 生米白+红曲斑块大米主题贴图），虎鲸式纵向摆尾巡游+突进啃咬，搁浅会扑腾回水中；
    // 模型长约 5 格，碰撞箱 2.6×2.0；60 心 / 攻击 10；生物蛋 ModItems.PALE_MERMAID_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<QianranjiaoEntity>> PALE_MERMAID =
            ENTITY_TYPES.register("pale_mermaid", () ->
                    EntityType.Builder.<QianranjiaoEntity>of(QianranjiaoEntity::new, MobCategory.MONSTER)
                            .sized(2.6F, 2.0F)
                            .clientTrackingRange(12)
                            .build("pale_mermaid"));
    // 望米伤鲛：已转正，水生敌对巨型古鲸 GeckoLib wangmishangjiao 模型（idle/walk/attack 动画，
    // 陈米重甲+折断长牙大米主题贴图），缓慢深海巡游+巨兽冲刺前冲张口啃咬、咬住甩头，搁浅会笨重扑腾回水中；
    // 模型躯干宽约 2.9 格、高约 4.2 格（全长约 15 格），碰撞箱 3.0×4.2（鳍展超出箱体）；
    // 1000 心 / 攻击 300；生物蛋 ModItems.WANGMI_WOUNDED_MERMAID_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<WangmishangjiaoEntity>> WANGMI_WOUNDED_MERMAID =
            ENTITY_TYPES.register("wangmi_wounded_mermaid", () ->
                    EntityType.Builder.<WangmishangjiaoEntity>of(WangmishangjiaoEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 4.2F)
                            .clientTrackingRange(12)
                            .build("wangmi_wounded_mermaid"));
    // 米蜃：已转正，陆地敌对 GeckoLib mishen 爬虫模型（idle/walk/attack 动画，
    // 珍珠米白+稻壳背甲+红曲斑大米主题贴图），六足贴地疾行+猛冲大颚啃咬；
    // 贴地低伏，碰撞箱 1.0×0.8；750 心 / 攻击 400；生物蛋 ModItems.RICE_MIRAGE_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<MishenEntity>> RICE_MIRAGE =
            ENTITY_TYPES.register("rice_mirage", () ->
                    EntityType.Builder.<MishenEntity>of(MishenEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 0.8F)
                            .clientTrackingRange(12)
                            .build("rice_mirage"));
    // 懒精灵：已转正，陆地敌对 GeckoLib lanjingling 小人型模型（idle/walk/attack 动画，
    // 稻草歪帽+米袋破袍+红曲腮斑大米主题贴图），佝偻碎步游荡+蹲身蓄力扑击双爪撕咬；
    // 碰撞箱 0.6×1.2；400 心 / 攻击 100；生物蛋 ModItems.LAZY_ELF_SPAWN_EGG 无需改动
    public static final RegistryObject<EntityType<LazyElfEntity>> LAZY_ELF =
            ENTITY_TYPES.register("lazy_elf", () ->
                    EntityType.Builder.<LazyElfEntity>of(LazyElfEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(12)
                            .build("lazy_elf"));
    // 哮噬米狼鹫：已转正，陆空敌对 GeckoLib xiaoshimilangjiu 模型（idle/walk/attack_bite/attack_dive 动画，
    // 米白狼毛+金绿稻羽+金竖瞳贴图），四足行走+前扑啃咬+盘旋俯冲撞击；模型脚底贴地实测约 22.15px（≈1.38 格）高、
    // 折翼全宽约 2.37 格，碰撞箱 1.2×1.5（躯干宽，翼尖超出箱体）；500 心 / 攻击 500，生物蛋无需改动
    public static final RegistryObject<EntityType<RiceWolfVultureEntity>> RICE_WOLF_VULTURE =
            ENTITY_TYPES.register("rice_wolf_vulture", () ->
                    EntityType.Builder.<RiceWolfVultureEntity>of(RiceWolfVultureEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.5F)
                            .clientTrackingRange(12)
                            .build("rice_wolf_vulture"));

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
