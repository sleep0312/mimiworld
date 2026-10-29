package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.client.renderer.LaziestHeavenfallArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
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

import java.util.function.Consumer;

/**
 * 最懒天堕套：GeckoLib 4 动态 3D 盔甲。
 * 四件共用同一个 geo/animation/texture（geo 内用 armor* 占位骨按装备槽控制显隐，
 * 纯白双翼挂在 armorBody 下，仅胸甲穿戴时显示）。
 *
 * <p>动画（assets/mimiworld/animations/laziest_heavenfall_armor.animation.json）：
 *   animation.laziest_heavenfall_armor.wing_flap —— 双翼常驻循环扇动（catmullrom，3.2s/循环）。</p>
 */
public class LaziestHeavenfallArmorItem extends ArmorItem implements GeoItem {

    private static final RawAnimation WING_FLAP = RawAnimation.begin()
            .thenLoop("animation.laziest_heavenfall_armor.wing_flap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LaziestHeavenfallArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private LaziestHeavenfallArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new LaziestHeavenfallArmorRenderer();
                }
                // 每帧把穿戴者姿态/当前槽位同步给 GeckoLib 盔甲渲染器
                this.renderer.prepForRender(entity, stack, slot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 双翼常驻循环扇动
        controllers.add(new AnimationController<>(this, "wings", 0, state -> {
            state.setAndContinue(WING_FLAP);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
