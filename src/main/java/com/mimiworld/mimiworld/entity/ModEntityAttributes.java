package com.mimiworld.mimiworld.entity;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 实体属性注册（mod 事件总线）。
 *
 * <p>七大待定 Boss 共用 {@link PendingBossEntity} 占位属性；
 * 米米系列占位生物（{@link PlaceholderRiceMobEntity}）逐项注册最大血量/近战攻击力/移速/追击范围；
 * 啄米鸡、米羊羊、黏米之蛛、盗米者、最懒终末龙为真实实体类，使用各自 createAttributes()。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // —— 七大待定 Boss：共用占位属性 ——
        event.put(ModEntities.MIMI_MONKEY_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.RICE_CRYSTAL_TIGER_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.SUPER_CRYSTAL_COW_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.COMPOUND_AZURE_PYTHON.get(), PendingBossEntity.createAttributes().build());
        event.put(ModEntities.CLEANSING_LONGEVITY_KUI.get(), PendingBossEntity.createAttributes().build());

        // —— 真实实体 ——
        // 最懒终末龙：真实飞行 Boss（属性沿用原占位数值 + FLYING_SPEED/满击退抗性）
        event.put(ModEntities.LAZIEST_FINAL_DRAGON.get(), LaziestFinalDragonEntity.createAttributes().build());
        // 盗米者：GeckoLib 微型鸭实体（1500 血）
        event.put(ModEntities.RICE_STEALER.get(), MicroDuckEntity.createAttributes().build());
        // 黏米之蛛：真实蜘蛛实体（48 血）
        event.put(ModEntities.STICKY_RICE_SPIDER.get(), StickyRiceSpiderEntity.createAttributes().build());
        // 米羊羊：真实 GeckoLib 羊实体（8 心、移速 0.23，对齐原版羊）
        event.put(ModEntities.MIMI_SHEEP.get(), MimiSheepEntity.createAttributes().build());
        // 啄米鸡：真实 GeckoLib 鸡实体（4 心、移速 0.25，对齐原版鸡）
        event.put(ModEntities.ZHUOMI_CHICKEN.get(), ZhuomiChickenEntity.createAttributes().build());

        // —— 米米系列占位生物：友好（无攻击力）——
        // 米哞牛：真实 GeckoLib 牛实体（10 心、移速 0.2，对齐原版牛）
        event.put(ModEntities.MIMOO_COW.get(), MimouniuEntity.createAttributes().build());
        // 米哼猪：真实 GeckoLib 猪实体（10 心、移速 0.25，对齐原版猪）
        event.put(ModEntities.MIHENG_PIG.get(), MihengPigEntity.createAttributes().build());
        // 贪米鱼：真实水生 GeckoLib 实体（4 心、游泳速度 1.0，对齐原版鳕鱼）
        event.put(ModEntities.TANMI_FISH.get(), TanmiFishEntity.createAttributes().build());
        // 喷米鱼：真实水生 GeckoLib 实体（4 心、游泳速度 1.0，对齐原版鳕鱼）
        event.put(ModEntities.PENMI_FISH.get(), PenmiFishEntity.createAttributes().build());
        // 米傀儡：真实 GeckoLib 铁傀儡型实体（100 心、移速 0.25、满击退抗性，对齐原版铁傀儡）
        event.put(ModEntities.RICE_GOLEM.get(), RiceGolemEntity.createAttributes().build());
        // 嗥米狼：真实 GeckoLib 狼实体（20 心、移速 0.3，对齐原版狼）
        event.put(ModEntities.HAOMI_WOLF.get(), HaomiWolfEntity.createAttributes().build());
        // 米米骷髅：真实 GeckoLib 骷髅实体（24 心、移速 0.25，对齐原版骷髅）
        event.put(ModEntities.MIMI_SKELETON.get(), MimiSkeletonEntity.createAttributes().build());
        // 糊米古尸：真实 GeckoLib 敌对亡灵实体（30 心、移速 0.23、攻击 3，对齐原版僵尸）
        event.put(ModEntities.HUMI_CORPSE.get(), HumiCorpseEntity.createAttributes().build());
        // 爪巴米者：真实 GeckoLib 自爆怪物实体（12 心、移速 0.25，对齐原版苦力怕；无近战攻击，攻击方式为自爆）
        event.put(ModEntities.PAMI_CRAWLER.get(), PamiCrawlerEntity.createAttributes().build());
        // 米巫：真实 GeckoLib 敌对女巫实体（30 心、移速 0.25、近战攻击 4；远程丢溅射药水）
        event.put(ModEntities.RICE_WITCH.get(), RiceWitchEntity.createAttributes().build());
        // 没米入：真实 GeckoLib 中立末影系实体（75 心；攻击 7/移速 0.3 等其余属性继承原版末影人）
        event.put(ModEntities.MOMI_ENTRANT.get(), MeimiruEntity.createAttributes().build());
        // 爆烈米灵：真实烈焰人式飞行敌对 GeckoLib 实体（60 心、移速 0.23/飞行 1.0、近战 6、追击 48，火焰免疫）
        event.put(ModEntities.EXPLOSIVE_RICE_SPIRIT.get(), BaoliemilingEntity.createAttributes().build());
        // 雕米骷髅：真实 GeckoLib 敌对亡灵实体（60 心、移速 0.25 对齐原版骷髅、近战攻击 5）
        event.put(ModEntities.CARVED_RICE_SKELETON.get(), DiaomikulouEntity.createAttributes().build());
        // 米莱马：真实史莱姆型敌对 GeckoLib 实体（12 心、移速 0.28、接触伤害 4，继承原版史莱姆蹦跳扑咬）
        event.put(ModEntities.MILAI_HORSE.get(), MilaimaEntity.createAttributes().build());
        // 米哼猪尸：真实 GeckoLib 敌对亡灵实体（30 心、移速 0.23、近战 5，陆地扑咬）
        event.put(ModEntities.MIHENG_PIG_CORPSE.get(), MihengPigCorpseEntity.createAttributes().build());
        // 米魂：真实 GeckoLib 敌对飞行实体（9 心、移速 0.25/飞行 1.0、近战 3，空中悬浮触须扑击）
        event.put(ModEntities.RICE_SOUL.get(), RiceSoulEntity.createAttributes().build());

        // —— 米米系列占位生物：敌对（带近战攻击力）——
        event.put(ModEntities.JIETIAN_RICE.get(), placeholder(400.0D, 200.0D));             // 界天米 200 心
        event.put(ModEntities.CHAOTIC_VOID_TOUCHER.get(), placeholder(1500.0D, 50.0D));     // 混沌虚无触 1500 血
        // 污棘花：真实扎根不动 GeckoLib 敌对植物（500 心、近战 50、移速 0、满击退抗性，原地啃咬）
        event.put(ModEntities.BLIGHTED_THORN_FLOWER.get(), WujihuaEntity.createAttributes().build());
        event.put(ModEntities.FALLEN_RICE_PHOENIX.get(), placeholder(2000.0D, 70.0D));      // 堕落米凤 1000 心
        event.put(ModEntities.HUALING_RICE.get(), placeholder(10000.0D, 10.0D));            // 化灵米 5000 心
        event.put(ModEntities.PALE_MERMAID.get(), placeholder(1500.0D, 1000.0D));           // 浅染鲛 750 心
        event.put(ModEntities.WANGMI_WOUNDED_MERMAID.get(), placeholder(2000.0D, 300.0D));  // 望米伤鲛 1000 心
        event.put(ModEntities.RICE_MIRAGE.get(), placeholder(1500.0D, 400.0D));             // 米蜃 750 心
        event.put(ModEntities.LAZY_ELF.get(), placeholder(800.0D, 100.0D));                 // 懒精灵 400 心
        event.put(ModEntities.RICE_WOLF_VULTURE.get(), placeholder(1000.0D, 500.0D));       // 哮噬米狼鹫 500 心
    }

    /**
     * 占位生物属性：指定最大血量与近战攻击力，并附统一移速 0.3、追击范围 32。
     * attack=0 视为友好生物，不挂载 ATTACK_DAMAGE 属性。
     */
    private static AttributeSupplier placeholder(double health, double attack) {
        var builder = Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
        if (attack > 0.0D) {
            builder.add(Attributes.ATTACK_DAMAGE, attack);
        }
        return builder.build();
    }
}
