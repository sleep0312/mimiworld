package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 实体属性注册（mod 事件总线）。
 *
 * <p>151-157 待定 Boss 共用 {@link PendingBossEntity} 占位属性；
 * 用户实体编号 1-30 统一从 {@link ModEntities#SPECS} 读血量/攻击力逐项注册；
 * 17 黏米之蛛、19 盗米者为真实实体类，属性已按用户数据（48 血 / 1500 血）写在各自 createAttributes()。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MIMI_MONKEY_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.RICE_CRYSTAL_TIGER_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.SUPER_CRYSTAL_COW_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.COMPOUND_AZURE_PYTHON.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.CLEANSING_LONGEVITY_KUI.get(), PendingBossEntity.createAttributes().build());
        // 157 最懒终末龙：真实飞行 Boss（属性沿用原占位数值 + FLYING_SPEED/满击退抗性）
        event.put(ModEntities.LAZIEST_FINAL_DRAGON.get(), LaziestFinalDragonEntity.createAttributes().build());
        // 19 盗米者：GeckoLib 微型鸭实体（血量已按用户数据改为 1500）
        event.put(ModEntities.RICE_STEALER.get(), MicroDuckEntity.createAttributes().build());
        // 17 黏米之蛛：真实蜘蛛实体（血量已按用户数据改为 48）
        event.put(ModEntities.STICKY_RICE_SPIDER.get(), StickyRiceSpiderEntity.createAttributes().build());

        // 用户实体编号 1-30（占位实体）：逐项按 SPECS 注册最大血量/攻击力/移速/追击范围
        for (int code = 1; code <= 30; code++) {
            if (code == 17 || code == 19) {
                continue; // 两个真实实体已在上方注册
            }
            ModEntities.MobSpec spec = ModEntities.SPECS[code];
            var builder = Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, spec.health())
                    .add(Attributes.MOVEMENT_SPEED, 0.3D)
                    .add(Attributes.FOLLOW_RANGE, 32.0D);
            if (spec.attack() > 0.0D) {
                builder.add(Attributes.ATTACK_DAMAGE, spec.attack());
            }
            event.put(spec.type().get(), builder.build());
        }
    }
}
