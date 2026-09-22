package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界实体注册类（投掷物等）。实体不属于「物品编码」体系。
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

    // ===== 151-157 生物蛋绑定的七大 Boss（实体待定，当前统一用 PendingBossEntity 占位） =====
    // 真实 Boss 实现后仅需替换工厂引用 PendingBossEntity::new，生物蛋（ModItems）无需改动。

    // 151 米米猴王：灵巧中型
    public static final RegistryObject<EntityType<PendingBossEntity>> MIMI_MONKEY_KING =
            registerBoss("mimi_monkey_king", 0.7F, 1.9F);
    // 152 饭晶虎王：四足猛兽
    public static final RegistryObject<EntityType<PendingBossEntity>> RICE_CRYSTAL_TIGER_KING =
            registerBoss("rice_crystal_tiger_king", 1.2F, 1.3F);
    // 153 超晶牛王：壮硕大体型
    public static final RegistryObject<EntityType<PendingBossEntity>> SUPER_CRYSTAL_COW_KING =
            registerBoss("super_crystal_cow_king", 1.4F, 1.7F);
    // 154 重晶兔王：小型
    public static final RegistryObject<EntityType<PendingBossEntity>> HEAVY_CRYSTAL_RABBIT_KING =
            registerBoss("heavy_crystal_rabbit_king", 0.7F, 0.9F);
    // 155 复晶天青蟒：长形低伏
    public static final RegistryObject<EntityType<PendingBossEntity>> COMPOUND_AZURE_PYTHON =
            registerBoss("compound_azure_python", 1.0F, 0.8F);
    // 156 清洁长命夔：巨型走兽
    public static final RegistryObject<EntityType<PendingBossEntity>> CLEANSING_LONGEVITY_KUI =
            registerBoss("cleansing_longevity_kui", 1.6F, 2.2F);
    // 157 最懒终末龙：超巨型飞行 Boss（已转正：GeckoLib 宗罪龙模型，末影龙式盘旋/俯冲 AI，火焰免疫）
    public static final RegistryObject<EntityType<LaziestFinalDragonEntity>> LAZIEST_FINAL_DRAGON =
            ENTITY_TYPES.register("laziest_final_dragon", () ->
                    EntityType.Builder.<LaziestFinalDragonEntity>of(LaziestFinalDragonEntity::new, MobCategory.MONSTER)
                            .sized(2.5F, 3.5F)
                            .clientTrackingRange(12)
                            .fireImmune()
                            .build("laziest_final_dragon"));

    // ===== 257 盗米者生物蛋绑定的实体：MicroDuckEntity（GeckoLib microduck 模型，小型 0.5×1.0） =====
    // 注册名仍为 rice_stealer，因此 257 号生物蛋（ForgeSpawnEggItem 绑定 RICE_STEALER）无需任何改动
    public static final RegistryObject<EntityType<MicroDuckEntity>> RICE_STEALER =
            ENTITY_TYPES.register("rice_stealer", () ->
                    EntityType.Builder.<MicroDuckEntity>of(MicroDuckEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 1.0F)
                            .clientTrackingRange(8)
                            .build("rice_stealer"));

    // ===== 黏米之蛛：GeckoLib spider 模型，行为同原版蜘蛛但全天敌对（1.4×0.9 中型怪物） =====
    // 暂不注册刷怪蛋，可用 /summon mimiworld:sticky_rice_spider 召唤测试；自然生成规则后续再加
    public static final RegistryObject<EntityType<StickyRiceSpiderEntity>> STICKY_RICE_SPIDER =
            ENTITY_TYPES.register("sticky_rice_spider", () ->
                    EntityType.Builder.<StickyRiceSpiderEntity>of(StickyRiceSpiderEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .clientTrackingRange(8)
                            .build("sticky_rice_spider"));

    // ============================================================================
    // 【用户实体编号 1-30】（2026-09-21 批次）
    // 1-20 = 友好生物（MobCategory.CREATURE）；21-30 = 敌对生物（MobCategory.MONSTER）。
    // 除 17 黏米之蛛（STICKY_RICE_SPIDER）、19 盗米者（RICE_STEALER）已有真实实体类外，
    // 其余 28 个统一用 PlaceholderRiceMobEntity 占位；真实类实现后只替换工厂，编号/生物蛋不变。
    // 每个实体的血量(×2=心/血)与攻击力记录在 SPECS[编号]，由 ModEntityAttributes 逐项注册。
    // ============================================================================

    // SPECS 必须在上方实体常量「之前」完成初始化：registerPlaceholder 在常量初始化阶段即写入数组。
    // 17/19 两个真实实体的血量以本批用户数据为准（蛛 48、盗米者 1500）。
    public static final MobSpec[] SPECS = new MobSpec[31];
    static {
        SPECS[17] = new MobSpec(STICKY_RICE_SPIDER, 48.0D, 2.0D);
        SPECS[19] = new MobSpec(RICE_STEALER, 1500.0D, 0.0D);
    }

    // —— 友好生物 1-20 ——
    // 1 啄米鸡 4 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> ZHUOMI_CHICKEN =
            registerPlaceholder(1, "zhuomi_chicken", MobCategory.CREATURE, 0.6F, 0.7F, 8.0D, 0.0D);
    // 2 米哞牛 10 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIMOO_COW =
            registerPlaceholder(2, "mimoo_cow", MobCategory.CREATURE, 0.9F, 1.4F, 20.0D, 0.0D);
    // 3 米羊羊 8 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIMI_SHEEP =
            registerPlaceholder(3, "mimi_sheep", MobCategory.CREATURE, 0.9F, 1.3F, 16.0D, 0.0D);
    // 4 米哼猪 10 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIHENG_PIG =
            registerPlaceholder(4, "miheng_pig", MobCategory.CREATURE, 0.9F, 1.3F, 20.0D, 0.0D);
    // 5 贪米鱼 4 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> TANMI_FISH =
            registerPlaceholder(5, "tanmi_fish", MobCategory.CREATURE, 0.5F, 0.4F, 8.0D, 0.0D);
    // 6 喷米鱼 4 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PENMI_FISH =
            registerPlaceholder(6, "penmi_fish", MobCategory.CREATURE, 0.5F, 0.4F, 8.0D, 0.0D);
    // 7 米傀儡 100 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_GOLEM =
            registerPlaceholder(7, "rice_golem", MobCategory.CREATURE, 1.4F, 2.7F, 200.0D, 0.0D);
    // 8 嗥米狼 20 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HAOMI_WOLF =
            registerPlaceholder(8, "haomi_wolf", MobCategory.CREATURE, 0.6F, 0.85F, 40.0D, 0.0D);
    // 9 米米骷髅 24 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIMI_SKELETON =
            registerPlaceholder(9, "mimi_skeleton", MobCategory.CREATURE, 0.6F, 1.99F, 48.0D, 0.0D);
    // 10 糊米古尸 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HUMI_CORPSE =
            registerPlaceholder(10, "humi_corpse", MobCategory.CREATURE, 0.7F, 1.9F, 60.0D, 0.0D);
    // 11 爪巴米者 12 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PAMI_CRAWLER =
            registerPlaceholder(11, "pami_crawler", MobCategory.CREATURE, 0.7F, 0.8F, 24.0D, 0.0D);
    // 12 米巫 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_WITCH =
            registerPlaceholder(12, "rice_witch", MobCategory.CREATURE, 0.6F, 1.95F, 60.0D, 0.0D);
    // 13 没米入 75 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MOMI_ENTRANT =
            registerPlaceholder(13, "momi_entrant", MobCategory.CREATURE, 0.9F, 1.9F, 150.0D, 0.0D);
    // 14 爆烈米灵 60 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> EXPLOSIVE_RICE_SPIRIT =
            registerPlaceholder(14, "explosive_rice_spirit", MobCategory.CREATURE, 0.8F, 1.6F, 120.0D, 0.0D);
    // 15 雕米骷髅 60 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> CARVED_RICE_SKELETON =
            registerPlaceholder(15, "carved_rice_skeleton", MobCategory.CREATURE, 0.6F, 2.1F, 120.0D, 0.0D);
    // 16 米莱马 12 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MILAI_HORSE =
            registerPlaceholder(16, "milai_horse", MobCategory.CREATURE, 1.2F, 1.6F, 24.0D, 0.0D);
    // 17 黏米之蛛 24 心（真实类 StickyRiceSpiderEntity，已在上方注册）
    // 18 米哼猪尸 30 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> MIHENG_PIG_CORPSE =
            registerPlaceholder(18, "miheng_pig_corpse", MobCategory.CREATURE, 0.9F, 0.9F, 60.0D, 0.0D);
    // 19 盗米者 750 心（真实类 MicroDuckEntity，即上方 RICE_STEALER）
    // 20 米魂 9 心
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_SOUL =
            registerPlaceholder(20, "rice_soul", MobCategory.CREATURE, 0.6F, 1.0F, 18.0D, 0.0D);

    // —— 敌对生物 21-30 ——
    // 21 界天米 200 心 / 攻击 200
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> JIETIAN_RICE =
            registerPlaceholder(21, "jietian_rice", MobCategory.MONSTER, 1.0F, 2.2F, 400.0D, 200.0D);
    // 22 混沌不可名状之米界虚无触 1500 血 / 攻击 50
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> CHAOTIC_VOID_TOUCHER =
            registerPlaceholder(22, "chaotic_void_toucher", MobCategory.MONSTER, 2.0F, 3.2F, 1500.0D, 50.0D);
    // 23 污棘花 500 心 / 攻击 50
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> BLIGHTED_THORN_FLOWER =
            registerPlaceholder(23, "blighted_thorn_flower", MobCategory.MONSTER, 0.9F, 1.8F, 1000.0D, 50.0D);
    // 24 堕落米凤 1000 心 / 攻击 70
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> FALLEN_RICE_PHOENIX =
            registerPlaceholder(24, "fallen_rice_phoenix", MobCategory.MONSTER, 1.4F, 2.2F, 2000.0D, 70.0D);
    // 25 化灵米 5000 心 / 攻击 10
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> HUALING_RICE =
            registerPlaceholder(25, "hualing_rice", MobCategory.MONSTER, 1.0F, 2.0F, 10000.0D, 10.0D);
    // 26 浅染鲛 750 心 / 攻击 1000
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> PALE_MERMAID =
            registerPlaceholder(26, "pale_mermaid", MobCategory.MONSTER, 0.8F, 1.6F, 1500.0D, 1000.0D);
    // 27 望米伤鲛 1000 心 / 攻击 300
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> WANGMI_WOUNDED_MERMAID =
            registerPlaceholder(27, "wangmi_wounded_mermaid", MobCategory.MONSTER, 0.8F, 1.6F, 2000.0D, 300.0D);
    // 28 米蜃 750 心 / 攻击 400
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_MIRAGE =
            registerPlaceholder(28, "rice_mirage", MobCategory.MONSTER, 1.0F, 1.9F, 1500.0D, 400.0D);
    // 29 懒精灵 400 心 / 攻击 100
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> LAZY_ELF =
            registerPlaceholder(29, "lazy_elf", MobCategory.MONSTER, 0.6F, 1.2F, 800.0D, 100.0D);
    // 30 哮噬米狼鹫 500 心 / 攻击 500
    public static final RegistryObject<EntityType<PlaceholderRiceMobEntity>> RICE_WOLF_VULTURE =
            registerPlaceholder(30, "rice_wolf_vulture", MobCategory.MONSTER, 1.2F, 1.9F, 1000.0D, 500.0D);

    /**
     * 实体编号属性表：SPECS[1..30]，index 即用户实体编号。
     * health=最大血量（心/血 ×2），attack=近战攻击力（友好生物为 0，不带攻击属性）。
     * 17/19 两个真实实体的血量以本批用户数据为准（蛛 48、盗米者 1500）。
     */
    public record MobSpec(RegistryObject<? extends EntityType<? extends LivingEntity>> type, double health, double attack) {
    }

    private static RegistryObject<EntityType<PlaceholderRiceMobEntity>> registerPlaceholder(
            int code, String id, MobCategory category, float width, float height,
            double health, double attack) {
        RegistryObject<EntityType<PlaceholderRiceMobEntity>> result =
                ENTITY_TYPES.register(id, () ->
                        EntityType.Builder.<PlaceholderRiceMobEntity>of(PlaceholderRiceMobEntity::new, category)
                                .sized(width, height)
                                .clientTrackingRange(category == MobCategory.MONSTER ? 12 : 8)
                                .build(id));
        SPECS[code] = new MobSpec(result, health, attack);
        return result;
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
