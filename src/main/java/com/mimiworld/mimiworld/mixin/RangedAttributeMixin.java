package com.mimiworld.mimiworld.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 解除原版最大生命值 1024 的属性上限。
 *
 * <p>原版 {@code Attributes.MAX_HEALTH} 是 {@code RangedAttribute(默认 20、最小 1、最大 1024)}，
 * {@code AttributeInstance#calculateValue()} 最终会调用 {@link RangedAttribute#sanitizeValue(double)}
 * 把结果夹在 [1, 1024] 区间内，因此 {@code setBaseValue(2500)} 会在取值时被静默压回 1024，
 * 表现为「血条 / 生命值被重置为 1024」。</p>
 *
 * <p>这里仅对 MAX_HEALTH 放宽上限到 10,000,000，其余属性保持原版范围；
 * 本 mixin 位于通用列表，服务端与客户端都会加载，属性同步到客户端后同样不会被夹紧。</p>
 */
@Mixin(RangedAttribute.class)
public abstract class RangedAttributeMixin {

    @Inject(method = "sanitizeValue", at = @At("HEAD"), cancellable = true)
    private void mimiworld$raiseMaxHealthLimit(double value, CallbackInfoReturnable<Double> cir) {
        RangedAttribute self = (RangedAttribute) (Object) this;
        if (self == Attributes.MAX_HEALTH && value > 1024.0D) {
            cir.setReturnValue(Mth.clamp(value, 1.0D, 10_000_000.0D));
        }
    }
}
