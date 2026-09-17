package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 实体属性注册（mod 事件总线）。七个待定 Boss 当前共用 {@link PendingBossEntity} 的占位属性。
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
        event.put(ModEntities.LAZIEST_FINAL_DRAGON.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.RICE_STEALER.get(), PendingBossEntity.createAttributes().build());
    }
}
