package com.mimiworld.mimiworld.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.addon.vanilla.StatusEffectsProvider;

/**
 * Jade 实体效果条目的等级显示：等级高于 10 时改用阿拉伯数字。
 *
 * <p>Jade 的 {@code getEffectName} 与原版一致，只对 amplifier 1~9 追加
 * {@code enchantment.level.N} 的罗马数字，更高等级不显示等级。
 * 这里在返回值处补上 {@code " " + (amplifier + 1)}，指向 Boss 时即可看到
 * 例如「残堕化 12」这样的数字等级。</p>
 *
 * <p>该 mixin 放在公共列表：Jade 的效果名称是在 {@code appendServerData}
 * 服务端侧生成后同步给客户端的，单人与专用服务器都需要生效。</p>
 */
@Mixin(value = StatusEffectsProvider.class, remap = false)
public abstract class JadeStatusEffectsProviderMixin {

    @Inject(method = "getEffectName", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mimiworld$numericHighLevel(MobEffectInstance effect, CallbackInfoReturnable<Component> cir) {
        if (effect.getAmplifier() < 10) {
            return;
        }
        cir.setReturnValue(effect.getEffect().getDisplayName().copy()
                .append(" ")
                .append(Component.literal(String.valueOf(effect.getAmplifier() + 1))));
    }
}
