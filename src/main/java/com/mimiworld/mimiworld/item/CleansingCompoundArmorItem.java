package com.mimiworld.mimiworld.item;

import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.client.renderer.CleansingCompoundArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 清洁米饭套（T4）：GeckoLib 3D 华贵重甲（下界合金同级）。
 * 四件共用同一个 geo/texture（geo 内用 armor* 占位骨按装备槽控制显隐）。
 * 本期为静态模型（无动画，模型类返回空的 cooking_pots.animation.json）。
 */
public class CleansingCompoundArmorItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CleansingCompoundArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 清洁米饭套单件效果：
    // 头盔夜视+力量III+150、胸甲防火+水下呼吸I+生命恢复III+200、
    // 护腿急迫II+跳跃提升I+175、靴子迅捷II+175。
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> 150.0F;
            case CHESTPLATE -> 200.0F;
            case LEGGINGS -> 175.0F;
            case BOOTS -> 175.0F;
        };
    }

    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> Map.of(
                    MobEffects.NIGHT_VISION, 0,     // 夜视 I
                    MobEffects.DAMAGE_BOOST, 2);    // 力量 III
            case CHESTPLATE -> Map.of(
                    MobEffects.FIRE_RESISTANCE, 0,  // 防火 I
                    MobEffects.WATER_BREATHING, 0,  // 水下呼吸 I
                    MobEffects.REGENERATION, 2);    // 生命恢复 III
            case LEGGINGS -> Map.of(
                    MobEffects.DIG_SPEED, 1,        // 急迫 II
                    MobEffects.JUMP, 0);            // 跳跃提升 I
            case BOOTS -> Map.of(MobEffects.MOVEMENT_SPEED, 1); // 迅捷 II
            default -> Map.of();
        };
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return withMaxHealthBonus(super.getAttributeModifiers(slot, stack), slot, this.getEquipmentSlot(), "cleansing_compound");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private CleansingCompoundArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new CleansingCompoundArmorRenderer();
                }
                // 每帧把穿戴者姿态/当前槽位同步给 GeckoLib 盔甲渲染器
                this.renderer.prepForRender(entity, stack, slot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 本期无动画
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
