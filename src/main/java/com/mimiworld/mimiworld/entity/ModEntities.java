package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.entity.EntityType;
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
    // 157 最懒终末龙：超巨型
    public static final RegistryObject<EntityType<PendingBossEntity>> LAZIEST_FINAL_DRAGON =
            registerBoss("laziest_final_dragon", 2.5F, 3.5F);

    // ===== 257 盗米者生物蛋绑定的实体（待定，当前用 PendingBossEntity 占位；人形 0.6×1.8） =====
    public static final RegistryObject<EntityType<PendingBossEntity>> RICE_STEALER =
            registerBoss("rice_stealer", 0.6F, 1.8F);

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
