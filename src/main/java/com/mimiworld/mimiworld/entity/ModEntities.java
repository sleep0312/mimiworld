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
    // 喷米鱼 4 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PENMI_FISH =
            registerPlaceholder("penmi_fish", MobCategory.CREATURE, 0.5F, 0.4F);
    // 米傀儡 100 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_GOLEM =
            registerPlaceholder("rice_golem", MobCategory.CREATURE, 1.4F, 2.7F);
    // 嗥米狼 20 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HAOMI_WOLF =
            registerPlaceholder("haomi_wolf", MobCategory.CREATURE, 0.6F, 0.85F);
    // 米米骷髅 24 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIMI_SKELETON =
            registerPlaceholder("mimi_skeleton", MobCategory.CREATURE, 0.6F, 1.99F);
    // 糊米古尸 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HUMI_CORPSE =
            registerPlaceholder("humi_corpse", MobCategory.CREATURE, 0.7F, 1.9F);
    // 爪巴米者 12 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PAMI_CRAWLER =
            registerPlaceholder("pami_crawler", MobCategory.CREATURE, 0.7F, 0.8F);
    // 米巫 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_WITCH =
            registerPlaceholder("rice_witch", MobCategory.CREATURE, 0.6F, 1.95F);
    // 没米入 75 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MOMI_ENTRANT =
            registerPlaceholder("momi_entrant", MobCategory.CREATURE, 0.9F, 1.9F);
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
