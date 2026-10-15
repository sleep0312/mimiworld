package com.mimiworld.mimiworld.item;

import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.client.renderer.SuperLazyWingsChestplateRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 超懒神翼胸甲（T5·终极翼甲）：GeckoLib 3D 翼胸甲。
 * geo 内含 bipedBody/armorBody 及挂在其下的 wingRight/wingLeft 双翼骨，
 * 穿戴后双翼一直循环播放 idle 扇翅动画（上抬+下扇）。
 *
 * <p>穿戴单件效果（仅胸甲位）：防火 I + 生命恢复 L + 力量 X + 急迫 V，
 * +1300 最大生命值，并给予创造飞行能力（mayfly）。</p>
 */
public class SuperLazyWingsChestplateItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private static final RawAnimation IDLE = RawAnimation.begin()
            .thenLoop("animation.super_lazy_wings_chestplate.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SuperLazyWingsChestplateItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 超懒神翼胸甲单件效果：+1300 最大生命值
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? 1300.0F : 0.0F;
    }

    // 防火 I、生命恢复 L（50级）、力量 X（10级）、急迫 V（5级）
    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        if (slot != EquipmentSlot.CHEST) {
            return Map.of();
        }
        return Map.of(
                MobEffects.FIRE_RESISTANCE, 0,   // 防火 I
                MobEffects.REGENERATION, 49,     // 生命恢复 L
                MobEffects.DAMAGE_BOOST, 9,      // 力量 X
                MobEffects.DIG_SPEED, 4);        // 急迫 V
    }

    // 穿戴时给予创造飞行能力
    @Override
    public boolean grantsFlight(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return withMaxHealthBonus(super.getAttributeModifiers(slot, stack), slot,
                this.getEquipmentSlot(), "super_lazy_wings");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SuperLazyWingsChestplateRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new SuperLazyWingsChestplateRenderer();
                }
                // 每帧把穿戴者姿态/当前槽位同步给 GeckoLib 盔甲渲染器
                this.renderer.prepForRender(entity, stack, slot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 双翼一直循环扇动
        controllers.add(new AnimationController<>(this, "wings", 0, state -> {
            state.setAndContinue(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
