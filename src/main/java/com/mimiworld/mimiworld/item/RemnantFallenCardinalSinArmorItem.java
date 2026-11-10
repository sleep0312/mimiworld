package com.mimiworld.mimiworld.item;

import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.client.renderer.RemnantFallenCardinalSinArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.effect.MobEffect;
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
 * 残堕宗罪套装：GeckoLib 4 动态 3D 盔甲。
 * 四件共用同一个 geo/animation/texture（geo 内用 armor* 占位骨按装备槽控制显隐，
 * 翅膀与尾巴挂在 armorBody 下，仅胸甲穿戴时显示）。
 *
 * 动画（assets/mimiworld/animations/remnant_fallen_cardinal_sin_armor.animation.json）：
 *   animation.walk2 —— 尾巴持续摆动（双翼保持静态，不参与动画）
 */
public class RemnantFallenCardinalSinArmorItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RemnantFallenCardinalSinArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 残堕宗罪套（T7·终极）单件最大生命值：头盔+400、胸甲+1000、护腿+700、靴子+700，整套合计 +2800。
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return switch (this.getType()) {
            case HELMET -> 400.0F;
            case CHESTPLATE -> 1000.0F;
            case LEGGINGS -> 700.0F;
            case BOOTS -> 700.0F;
        };
    }

    /** 本套无单件药水效果（翅膀静态、尾巴仅作外观动画）。 */
    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        return Map.of();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return withMaxHealthBonus(super.getAttributeModifiers(slot, stack), slot, this.getEquipmentSlot(), "remnant_fallen_cardinal_sin");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private RemnantFallenCardinalSinArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new RemnantFallenCardinalSinArmorRenderer();
                }
                // 每帧把穿戴者姿态/当前槽位同步给 GeckoLib 盔甲渲染器
                this.renderer.prepForRender(entity, stack, slot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 尾巴摆动（tail；翅膀不加控制器，保持静态）
        controllers.add(new AnimationController<>(this, "tail", 0, state -> {
            state.setAndContinue(RawAnimation.begin().thenLoop("animation.walk2"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
