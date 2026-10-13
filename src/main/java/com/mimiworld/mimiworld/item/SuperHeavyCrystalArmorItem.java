package com.mimiworld.mimiworld.item;

import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.client.renderer.SuperHeavyCrystalArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
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
 * 超重结晶套（T2）：GeckoLib 3D 重甲。
 * 四件共用同一个 geo/texture（geo 内用 armor* 占位骨按装备槽控制显隐）。
 * 本期为静态模型（无动画，模型类返回空的 cooking_pots.animation.json）。
 */
public class SuperHeavyCrystalArmorItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SuperHeavyCrystalArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 超重结晶套单件效果：头盔+20、胸甲+35、护腿+30、靴子+15 最大生命值（纯生命加成，无药水效果）。
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> 20.0F;
            case CHESTPLATE -> 35.0F;
            case LEGGINGS -> 30.0F;
            case BOOTS -> 15.0F;
        };
    }

    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        return Map.of();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return withMaxHealthBonus(super.getAttributeModifiers(slot, stack), slot, this.getEquipmentSlot(), "super_heavy_crystal");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SuperHeavyCrystalArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new SuperHeavyCrystalArmorRenderer();
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
