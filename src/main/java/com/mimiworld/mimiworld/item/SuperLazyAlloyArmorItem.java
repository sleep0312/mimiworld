package com.mimiworld.mimiworld.item;

import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.client.renderer.SuperLazyAlloyArmorRenderer;
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
 * 超懒合金套（T5·终极套）：GeckoLib 3D 金蟒重甲（总防 26，韧性 5，击退抗 0.15）。
 * 四件共用同一个 geo/texture（geo 内用 armor* 占位骨按装备槽控制显隐）。
 * 本期为静态模型（无动画，模型类返回空的 cooking_pots.animation.json）。
 */
public class SuperLazyAlloyArmorItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SuperLazyAlloyArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 超懒合金套（终极）单件效果：
    // 头盔夜视+力量VI+200、胸甲防火+水下呼吸I+生命恢复XX+500、
    // 护腿急迫V+跳跃提升III+250、靴子迅捷III+250。
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> 200.0F;
            case CHESTPLATE -> 500.0F;
            case LEGGINGS -> 250.0F;
            case BOOTS -> 250.0F;
        };
    }

    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> Map.of(
                    MobEffects.NIGHT_VISION, 0,     // 夜视 I
                    MobEffects.DAMAGE_BOOST, 5);    // 力量 VI
            case CHESTPLATE -> Map.of(
                    MobEffects.FIRE_RESISTANCE, 0,  // 防火 I
                    MobEffects.WATER_BREATHING, 0,  // 水下呼吸 I
                    MobEffects.REGENERATION, 19);   // 生命恢复 XX
            case LEGGINGS -> Map.of(
                    MobEffects.DIG_SPEED, 4,        // 急迫 V
                    MobEffects.JUMP, 2);            // 跳跃提升 III
            case BOOTS -> Map.of(MobEffects.MOVEMENT_SPEED, 2); // 迅捷 III
            default -> Map.of();
        };
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return withMaxHealthBonus(super.getAttributeModifiers(slot, stack), slot, this.getEquipmentSlot(), "super_lazy_alloy");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SuperLazyAlloyArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new SuperLazyAlloyArmorRenderer();
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
