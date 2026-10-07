package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 米米图腾的免死逻辑。
 *
 * <p>原版 {@code LivingEntity#checkTotemDeathProtection} 在生物受到致命伤害时被调用，
 * 但循环内硬编码了 {@link Items#TOTEM_OF_UNDYING}，Forge 的 LivingUseTotemEvent
 * 也只在「手持物已经是原版图腾」时才触发，无法让自定义物品获得图腾能力，只能 HEAD 注入。
 *
 * <p>触发效果逐行复刻 1.20.1 原版：无视无敌的伤害类型（/kill、虚空等）不免死；
 * 依次检查主手、副手；免死时回 1 点血、清空原有效果、附加再生 II 45s / 吸收 II 5s /
 * 抗火 I 40s，广播实体事件 35（客户端播放图腾粒子、音效与第一人称激活动画）。
 * 若双手持有原版图腾则交给原版逻辑优先处理，保证原版行为不变。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void mimiworld$useMimiTotem(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        // 任一手持有原版图腾时不介入，原版图腾按主手→副手顺序优先
        for (InteractionHand hand : InteractionHand.values()) {
            if (self.getItemInHand(hand).is(Items.TOTEM_OF_UNDYING)) {
                return;
            }
        }

        ItemStack usedTotem = null;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = self.getItemInHand(hand);
            if (held.is(ModItems.MIMI_TOTEM.get())
                    && ForgeHooks.onLivingUseTotem(self, damageSource, held, hand)) {
                usedTotem = held.copy();
                held.shrink(1);
                break;
            }
        }

        if (usedTotem != null) {
            if (self instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, usedTotem);
            }

            self.setHealth(1.0F);
            self.removeAllEffects();
            self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            self.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            self.level().broadcastEntityEvent(self, (byte) 35);
            cir.setReturnValue(true);
        }
    }
}
