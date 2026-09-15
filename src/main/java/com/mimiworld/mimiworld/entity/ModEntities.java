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

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
