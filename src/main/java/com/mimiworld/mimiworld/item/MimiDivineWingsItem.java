package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.client.renderer.MimiDivineWingsRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
 * 米米神翼（Mimi Divine Wings）：GeckoLib 3D 翼胸甲。
 * geo 仅含 bipedBody 下的 wingRight/wingLeft 双翼骨（胸甲本体已删除），
 * 穿戴后双翼循环播放 idle 扇翅动画。
 *
 * <p>穿戴单件效果（仅胸甲位）：给予创造飞行能力（mayfly，由
 * {@code ModArmorSetBonusEvents} 逐 tick 维持，脱下自动撤销）。</p>
 */
public class MimiDivineWingsItem extends ArmorItem implements GeoItem, ArmorPieceEffects {

    private static final RawAnimation IDLE = RawAnimation.begin()
            .thenLoop("animation.mimi_divine_wings.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MimiDivineWingsItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    // 米米神翼只给飞行，不额外加最大生命值
    @Override
    public float getMaxHealthBonus(EquipmentSlot slot) {
        return 0.0F;
    }

    // 米米神翼不给药水效果
    @Override
    public Map<MobEffect, Integer> getWornEffects(EquipmentSlot slot) {
        return Map.of();
    }

    // 穿戴在胸甲位时给予创造飞行能力（mayfly）
    @Override
    public boolean grantsFlight(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MimiDivineWingsRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new MimiDivineWingsRenderer();
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
