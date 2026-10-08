package com.mimiworld.mimiworld.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.client.model.MonkeyKingRuyiStaffModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 猴王如意棍：GeckoLib 3D 模型的近战武器（米米猴王 Boss 掉落）。
 *
 * <p>战斗数值：攻击伤害 16（tier 加成 0 + 修饰 15 + 玩家基础 1）、
 * 攻速 0.8（修饰 -3.2）、实体触及 6 格（在玩家基础 3 格上 +3）；
 * 命中目标时附加虚弱 I（2 秒 = 40 tick）。</p>
 *
 * <p>3D 渲染通过 IClientItemExtensions#getCustomRenderer 挂载 GeoItemRenderer，
 * 各视角变换读取 models/item/monkey_king_ruyi_staff.json 的 display 节点；无动画。</p>
 */
public class MonkeyKingRuyiStaffItem extends SwordItem implements GeoItem {

    /** 武器触及修饰符 UUID：实体触及 +3（3 格 → 6 格）。 */
    private static final UUID ENTITY_REACH_UUID = UUID.fromString("b2f6c4a1-7d3e-4b8a-9f1c-2e6a5d8c7b01");

    /** 猴王如意棍专属材料等级（伤害加成为 0，使最终伤害精确等于 16；修复材料为米米猴头）。 */
    public static final Tier MONKEY_KING_RUYI_STAFF_TIER = new Tier() {
        @Override
        public int getUses() {
            return 2000;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 22;
        }

        @Override
        public Ingredient getRepairIngredient() {
            // 运行期才调用，此时 ModItems 已完成注册
            return Ingredient.of(ModItems.MIMI_MONKEY_HEAD.get());
        }
    };

    private final Multimap<Attribute, AttributeModifier> staffModifiers;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MonkeyKingRuyiStaffItem(Properties properties) {
        // 伤害修饰 15 → 15 + tier 加成 0 + 玩家基础 1 = 16；攻速修饰 -3.2 → 显示 0.8
        super(MONKEY_KING_RUYI_STAFF_TIER, 15, -3.2F, properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                ENTITY_REACH_UUID, "Weapon modifier", 3.0D, AttributeModifier.Operation.ADDITION));
        this.staffModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.staffModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 近战命中：虚弱 I，持续 2 秒（40 tick）
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<MonkeyKingRuyiStaffItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new MonkeyKingRuyiStaffModel());
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 静态模型，无动画控制器
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
